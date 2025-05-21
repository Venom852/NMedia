package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
//    fun getAll(): List<Post>
//    fun likeById(id: Long): Post
    fun toShareById(id: Long)
    fun removeById(id: Long)
    fun save(post: Post)

    fun getAllAsync(callback: Callback<List<Post>>)
    fun likeByIdAsync(id: Long, callback: Callback<Post>)

    interface Callback<T> {
        fun onSuccess(data: T) {}
        fun onError(e: Exception) {}
    }
}