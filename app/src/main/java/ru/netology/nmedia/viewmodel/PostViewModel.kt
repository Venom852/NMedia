package ru.netology.nmedia.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryInMemory

class PostViewModel : ViewModel() {
    val empty = Post(
        id = 0,
        author = "",
        content = "",
        published = "",
        likedByMe = false,
        toShare = false,
        numberLikes = 0,
        shared = 0,
        numberViews = 0
    )
    private val repository: PostRepository = PostRepositoryInMemory()
    val data = repository.get()
    val edited = MutableLiveData(empty)

    fun likeById(id: Long) = repository.likeById(id)
    fun toShareById(id: Long) = repository.toShareById(id)
    fun removeById(id: Long) = repository.removeById(id)

    fun saveContent(content: String) {
        edited.value?.let {editPost ->
            repository.save(editPost.copy(content = content))
        }
        edited.value = empty
    }

    fun editById(post: Post) {
        edited.value = post
    }

}