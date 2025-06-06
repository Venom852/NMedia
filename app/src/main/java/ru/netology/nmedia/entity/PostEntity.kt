package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorAvatar: String?,
    val video: String?,
    val content: String?,
    val published: Long,
    val likedByMe: Boolean,
    val toShare: Boolean,
    val numberLikes: Long,
    val attachment: Attachment?,
    val shared: Long,
    val numberViews: Long
) {
    fun toDto() = Post(
        id,
        author,
        authorAvatar,
        video,
        content,
        published,
        likedByMe,
        toShare,
        numberLikes,
        attachment,
        shared,
        numberViews
    )

    companion object {
        fun fromDto(post: Post) = PostEntity(
            post.id,
            post.author,
            post.authorAvatar,
            post.video,
            post.content,
            post.published,
            post.likedByMe,
            post.toShare,
            post.likes,
            post.attachment,
            post.shared,
            post.numberViews
        )
    }
}