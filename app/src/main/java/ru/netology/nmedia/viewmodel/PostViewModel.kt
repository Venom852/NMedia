package ru.netology.nmedia.viewmodel

import android.annotation.SuppressLint
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.map
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.flatMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ErrorCode400And500
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject
import kotlin.concurrent.thread
import androidx.paging.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.enumeration.AttachmentType

@SuppressLint("CheckResult")
@OptIn(ExperimentalCoroutinesApi::class)
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

    private val cached: Flow<PagingData<Post>> = repository
        .data
        .cachedIn(viewModelScope)

    val data: Flow<PagingData<Post>> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.map { post ->
                    post.copy(ownedByMe = post.authorId == myId)
                }
            }
        }

    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState>
        get() = _dataState
    var newerCount: Flow<Int> = emptyFlow()
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
//            oldPosts = dao.getAll().stateIn(viewModelScope).value.toDto()
            CoroutineScope(Dispatchers.Default).launch {
                oldPosts = dao.getAll().toDto()
            }
            newerCount = repository.getNewerCount(oldPosts.first().id)
            dao.browse()
        }
    }

    fun loadPosts() {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
//                repository.getAll()
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
//                repository.getAll()
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
//            oldPosts = dao.getAll().stateIn(viewModelScope).value.toDto()
            CoroutineScope(Dispatchers.Default).launch {
                oldPosts = dao.getAll().toDto()
            }
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
//            oldPosts = dao.getAll().stateIn(viewModelScope).value.toDto()
            CoroutineScope(Dispatchers.Default).launch {
                oldPosts = dao.getAll().toDto()
            }
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
//                oldPosts = dao.getAll().stateIn(viewModelScope).value.toDto()
                CoroutineScope(Dispatchers.Default).launch {
                    oldPosts = dao.getAll().toDto()
                }

                var post = it.copy(content = content)
                var postServer = empty

                if (_photo.value?.uri != null) {
                    _photo.value?.uri?.let { uri ->
                        post = post.copy(
                            attachment = Attachment(
                                url = "null",
                                type = AttachmentType.IMAGE,
                                uri = uri.toString()
                            )
                        )
                    }
                    dao.save(PostEntity.fromDto(post))
                } else {
                    dao.save(PostEntity.fromDto(post))
                }
                _postCreated.value = Unit
                try {
                    when(_photo.value) {
                        noPhoto -> postServer = repository.save(post)
                        else -> _photo.value?.file?.let { file ->
                            postServer = repository.saveWithAttachment(post, MediaUpload(file))
                        }
                    }

                    if (post.id == 0L) {
                        oldPost = oldPosts.first()
                        dao.changeIdPostById(oldPost.id, postServer.id, savedOnTheServer = true)
                        _photo.value = noPhoto
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
    }

    fun editById(post: Post) {
        edited.value = post
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }
}
