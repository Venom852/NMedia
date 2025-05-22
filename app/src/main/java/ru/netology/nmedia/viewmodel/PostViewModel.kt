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
import java.io.IOException
import kotlin.concurrent.thread

class PostViewModel(application: Application) : AndroidViewModel(application) {
    val empty = Post(
        id = 0,
        author = "Me",
        video = null,
        content = "",
        published = 0,
        likedByMe = false,
        toShare = false,
        likes = 0,
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

    init {
        loadPosts()
    }

    fun loadPosts() {
        thread {
            _data.postValue(FeedModel(loading = true))
            try {
                val posts = repository.getAll()
                FeedModel(posts = posts, empty = posts.isEmpty())
            } catch (e: IOException) {
                FeedModel(error = true)
            }.also(_data::postValue)
        }
    }

    fun likeById(id: Long) = thread {
        var postLikedByMe = false
        _data.value?.posts.orEmpty().forEach { if (it.id == id) postLikedByMe = it.likedByMe }
        try {
            val post = repository.likeById(id, postLikedByMe)
            _data.postValue(_data.value?.copy(posts = _data.value?.posts.orEmpty().map { if (it.id != post.id) it else post }))
        } catch (e: IOException) {
            _data.postValue(FeedModel(error = true))
        }
    }
    fun toShareById(id: Long) = thread { repository.toShareById(id) }
    fun removeById(id: Long) {
        thread {
            val old = _data.value?.posts.orEmpty()
            _data.postValue(
                _data.value?.copy(posts = _data.value?.posts.orEmpty()
                    .filter { it.id != id }
                )
            )
            try {
                repository.removeById(id)
            } catch (e: IOException) {
                _data.postValue(_data.value?.copy(posts = old))
            }
        }
    }

    fun saveContent(content: String) {
        edited.value?.let { editPost ->
            thread {
                repository.save(editPost.copy(content = content))
                _postCreated.postValue(Unit)
            }
        }
        edited.value = empty
    }

    fun editById(post: Post) {
        edited.value = post
    }
}
