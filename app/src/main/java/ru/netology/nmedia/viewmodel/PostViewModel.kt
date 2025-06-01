package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
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
        numberViews = 0
    )

    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated
    private var nextId = 1L
    private var posts = emptyList<Post>()

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
        repository.getAllAsync(object : PostRepository.Callback<List<Post>> {
            override fun onSuccess(posts: List<Post>) {
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })

    }

    fun likeById(id: Long) {
        val oldPosts = _data.value?.posts
        var postLikedByMe = oldPosts?.find { it.id == id }?.likedByMe ?: return
        _data.postValue(_data.value?.copy(posts = _data.value?.posts?.map {
            if (id == it.id) {
                it.copy(
                    likedByMe = !it.likedByMe,
                    likes = if (it.likedByMe) {
                        it.likes - 1
                    } else {
                        it.likes + 1
                    }
                )
            } else {
                it
            }
        } ?: return))
        repository.likeByIdAsync(id, postLikedByMe, object : PostRepository.Callback<Unit> {
            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(posts = oldPosts))
            }
        })
    }

    fun toShareById(id: Long) = thread { repository.toShareById(id) }
    fun removeById(id: Long) {
        val oldPosts = _data.value?.posts.orEmpty()
        _data.postValue(
            _data.value?.copy(
                posts = _data.value?.posts.orEmpty()
                    .filter { it.id != id }
            )
        )
        repository.removeByIdAsync(id, object : PostRepository.Callback<Unit> {
            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(posts = oldPosts))
            }
        })
    }

    fun saveContent(content: String) {
        edited.value?.let {
            repository.saveAsync(it.copy(content = content), object : PostRepository.Callback<Post> {
                override fun onSuccess(post: Post) {
                    posts = _data.value?.posts.orEmpty()
                    nextId = (posts.maxOfOrNull { it.id } ?: 1)
                    posts = if (post.id > nextId) {
                        nextId = post.id
                        listOf(post) + posts
                    } else {
                        posts.map {
                            if (it.id != post.id) it else post.copy(content = post.content)
                        }
                    }
                    _data.postValue(_data.value?.copy(posts = posts))
                    _postCreated.postValue(Unit)
                }

                override fun onError(e: Exception) {
                    _data.postValue(FeedModel(error = true))
                }
            })
        }
        edited.value = empty
    }

    fun editById(post: Post) {
        edited.value = post
    }
}
