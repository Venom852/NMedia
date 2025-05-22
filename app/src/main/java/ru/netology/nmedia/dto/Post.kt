package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val video: String?,
    val content: String?,
    val published: Long,
    val likedByMe: Boolean,
    val toShare: Boolean,
    val likes: Long,
    val shared: Long,
    val numberViews: Long
)