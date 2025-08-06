package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post

interface PostRepository {

    val data: Flow<List<Post>>
    suspend fun getAll()
    suspend fun save(post: Post): Post
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long, postLikedByMe: Boolean?)
    fun toShareById(id: Long)
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload): Post
    suspend fun upload(upload: MediaUpload): Media
}