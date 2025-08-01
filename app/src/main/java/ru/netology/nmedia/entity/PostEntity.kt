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
    val numberViews: Long,
    val savedOnTheServer: Boolean,
    val viewed: Boolean
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
        numberViews,
        savedOnTheServer,
        viewed
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
            post.numberViews,
            post.savedOnTheServer,
            post.viewed
        )
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)