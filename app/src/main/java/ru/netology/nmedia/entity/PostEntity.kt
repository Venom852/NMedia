package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
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
) {
    fun toDto() = Post(
        id,
        author,
        video,
        content,
        published,
        likedByMe,
        toShare,
        numberLikes,
        shared,
        numberViews
    )

    companion object {
        fun fromDto(post: Post) = PostEntity(
            post.id,
            post.author,
            post.video,
            post.content,
            post.published,
            post.likedByMe,
            post.toShare,
            post.numberLikes,
            post.shared,
            post.numberViews
        )
    }
}