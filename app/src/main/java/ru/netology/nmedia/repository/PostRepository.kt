package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(id: Long, postLikedByMe: Boolean): Post
    fun toShareById(id: Long)
    fun removeById(id: Long)
    fun save(post: Post)
}