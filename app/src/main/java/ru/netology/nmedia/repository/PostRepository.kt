package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
//    fun getAll(): List<Post>
//    fun likeById(id: Long, postLikedByMe: Boolean): Post
//    fun toShareById(id: Long)
//    fun removeById(id: Long)
//    fun save(post: Post)

    fun getAllAsync(callback: Callback<List<Post>>)
    fun likeByIdAsync(id: Long, postLikedByMe: Boolean, callback: Callback<Post>)
    fun removeByIdAsync(id: Long, callback: Callback<String>)
    fun saveAsync(post: Post, callback: Callback<String>)

    interface Callback<T> {
        fun onSuccess(data: T) {}
        fun onError(e: Exception) {}
    }
}