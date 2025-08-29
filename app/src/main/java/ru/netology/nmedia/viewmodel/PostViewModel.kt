package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import kotlinx.coroutines.flow.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.error.ErrorCode400And500
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject
import kotlin.collections.orEmpty
import kotlin.concurrent.thread

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val dao: PostDao,
    auth: AppAuth
) : ViewModel() {
    val empty = Post(
        id = 0,
        author = "Me",
        authorId = 0,
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
        viewed = true,
        ownedByMe = false
    )

    private val noPhoto = PhotoModel()

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: LiveData<FeedModel> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.data
                .map { posts ->
                    FeedModel(
                        posts.map { it.copy(ownedByMe = it.authorId == myId) },
                        posts.isEmpty()
                    )
                }
        }.asLiveData(Dispatchers.Default)
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
    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo
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
                var postServer = empty

//                if (_photo.value?.uri != null) {
//                    _photo.value?.uri?.let { uri ->
//                        post = post.copy(
//                            attachment = Attachment(
//                                url = "null",
//                                type = AttachmentType.IMAGE,
//                                uri = uri.toString()
//                            )
//                        )
//                    }
//                    dao.save(PostEntity.fromDto(post))
//                } else {
//                    dao.save(PostEntity.fromDto(post))
//                }
//                _postCreated.value = Unit
                try {
                    when(_photo.value) {
                        noPhoto -> postServer = repository.save(post)
                        else -> _photo.value?.file?.let { file ->
                            postServer = repository.saveWithAttachment(post, MediaUpload(file))
                        }
                    }

//                    print(postServer)
                    _postCreated.value = Unit
                    if (post.id == 0L) {
                        oldPost = data.value?.posts.orEmpty().first()
                        dao.save(PostEntity.fromDto(postServer))
//                        dao.changeIdPostById(oldPost.id, postServer.id, savedOnTheServer = true)
                    }
                } catch (e: ErrorCode400And500) {
                    _bottomSheet.value = Unit
                    if (post.id == 0L && _photo.value == noPhoto) {
                        dao.removeById(oldPost.id)
                        return@launch
                    } else dao.insertPosts(oldPosts.toEntity())
                } catch (e: UnknownError) {
                    _dataState.value = FeedModelState(errorCode300 = true)
                    if (post.id == 0L && _photo.value == noPhoto) {
                        dao.removeById(oldPost.id)
                        return@launch
                    } else dao.insertPosts(oldPosts.toEntity())
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                    if (post.id == 0L && _photo.value == noPhoto) {
                        dao.removeById(oldPost.id)
                        return@launch
                    } else dao.insertPosts(oldPosts.toEntity())
                }
            }
        }
        edited.value = empty
        _photo.value = noPhoto
    }

    fun editById(post: Post) {
        edited.value = post
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

}
