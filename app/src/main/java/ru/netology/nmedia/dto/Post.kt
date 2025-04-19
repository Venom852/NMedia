package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val video: String?,
    val content: String?,
    val published: String,
    val likedByMe: Boolean,
    val toShare: Boolean,
    val numberLikes: Long,
    val shared: Long,
    val numberViews: Long
)