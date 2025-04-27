package ru.netology.nmedia.dao

import ru.netology.nmedia.dto.Post

interface PostDao {
    fun getAll(): List<Post>
    fun save(post: Post): Post
    fun toShareById(id: Long)
    fun likeById(id: Long)
    fun removeById(id: Long)
    fun saveDraft(draft: String)
    fun removeDraft()
    fun getDraft() : String?
}