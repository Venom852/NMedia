package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import kotlinx.coroutines.flow.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ErrorCode400And500
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import kotlin.collections.orEmpty
import kotlin.concurrent.thread

class PostViewModel(application: Application) : AndroidViewModel(application) {
    val empty = Post(
        id = 0,
        author = "Me",
        authorAvatar = "netology",
        video = null,
        content = "",
        published = 0,
        likedByMe = false,
        toShare = false,
        likes = 0,
        attachment = null,
        shared = 0,
        numberViews = 0,
        savedOnTheServer = false,
        viewed = true
    )

    private val dao: PostDao = AppDb.getInstance(application).postDao
    private val repository: PostRepository = PostRepositoryImpl(dao)
    val data: LiveData<FeedModel> = repository.data.map(::FeedModel).asLiveData(Dispatchers.Default)
    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    val newerCount: LiveData<Int> = data.switchMap {
        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
            .catch { e -> e.printStackTrace() }
            .asLiveData(Dispatchers.Default)
    }
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated
    private val _bottomSheet = SingleLiveEvent<Unit>()
    val bottomSheet: LiveData<Unit>
        get() = _bottomSheet
    private var oldPost = empty
    private var oldPosts = emptyList<Post>()

    init {
        loadPosts()
    }

    fun browse() {
        viewModelScope.launch {
            dao.browse()
            dao.getAll().asLiveData(Dispatchers.Default)
        }
    }

    fun loadPosts() {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.getAll()
                _dataState.value = FeedModelState()
            } catch (e: ErrorCode400And500) {
                dao.insertPosts(oldPosts.toEntity())
                _bottomSheet.value = Unit
            } catch (e: UnknownError) {
                _dataState.value = FeedModelState(errorCode300 = true)
            } catch (e: Exception) {
                print(e)
                dao.insertPosts(oldPosts.toEntity())
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun refreshPosts() {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(refreshing = true)
                repository.getAll()
                _dataState.value = FeedModelState()
            } catch (e: ErrorCode400And500) {
                dao.insertPosts(oldPosts.toEntity())
                _bottomSheet.value = Unit
            } catch (e: UnknownError) {
                _dataState.value = FeedModelState(errorCode300 = true)
            } catch (e: Exception) {
                dao.insertPosts(oldPosts.toEntity())
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun loadPostsWithoutServer() {
        _dataState.value = _dataState.value?.copy(errorCode300 = false)
    }

    fun likeById(id: Long) {
        viewModelScope.launch {
            oldPosts = data.value?.posts.orEmpty()
            val postLikedByMe = oldPosts.find { it.id == id }?.likedByMe
            dao.likeById(id)
            try {
                repository.likeById(id, postLikedByMe)
            } catch (e: ErrorCode400And500) {
                dao.insertPosts(oldPosts.toEntity())
                _bottomSheet.value = Unit
            } catch (e: UnknownError) {
                _dataState.value = FeedModelState(errorCode300 = true)
            } catch (e: Exception) {
                dao.insertPosts(oldPosts.toEntity())
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun toShareById(id: Long) = thread { repository.toShareById(id) }

    fun removeById(id: Long) {
        viewModelScope.launch {
            oldPosts = data.value?.posts.orEmpty()
            dao.removeById(id)
            try {
                repository.removeById(id)
            } catch (e: ErrorCode400And500) {
                dao.insertPosts(oldPosts.toEntity())
                _bottomSheet.value = Unit
            } catch (e: UnknownError) {
                _dataState.value = FeedModelState(errorCode300 = true)
            } catch (e: Exception) {
                dao.insertPosts(oldPosts.toEntity())
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun saveContent(content: String) {
        edited.value?.let {
            viewModelScope.launch {
                oldPosts = data.value?.posts.orEmpty()
                val post = it.copy(content = content)
                dao.save(PostEntity.fromDto(post))
                _postCreated.value = Unit
                try {
                    val postServer = repository.save(post)
                    if (post.id == 0L) {
                        oldPost = data.value?.posts.orEmpty().first()
                        dao.changeIdPostById(oldPost.id, postServer.id, savedOnTheServer = true)
                    }
                } catch (e: ErrorCode400And500) {
                    _bottomSheet.value = Unit
                    if (post.id == 0L) {
                        dao.removeById(oldPost.id)
                        return@launch
                    }
                    dao.insertPosts(oldPosts.toEntity())
                } catch (e: UnknownError) {
                    _dataState.value = FeedModelState(errorCode300 = true)
                    if (post.id == 0L) {
                        dao.removeById(oldPost.id)
                        return@launch
                    }
                    dao.insertPosts(oldPosts.toEntity())
                } catch (e: Exception) {
                    print(e)
                    dao.removeById(oldPost.id)
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
    }

    fun editById(post: Post) {
        edited.value = post
    }

}
