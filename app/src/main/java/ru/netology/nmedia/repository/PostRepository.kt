package ru.netology.nmedia.repository

import android.view.ViewGroup
import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun toShareById(id: Long)

    fun getAllAsync(callback: Callback<List<Post>>)
    fun getAllAvatars(posts: List<Post>)
    fun likeByIdAsync(id: Long, postLikedByMe: Boolean, callback: Callback<Unit>)
    fun removeByIdAsync(id: Long, callback: Callback<Unit>)
    fun saveAsync(post: Post, callback: Callback<Post>)

    interface Callback<T> {
        fun onSuccess(data: T) {}
        fun onError(e: Exception) {}
    }
}