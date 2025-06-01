package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.netology.nmedia.entity.ContentDraftEntity
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): LiveData<List<PostEntity>>

    @Insert
    fun insert(post: PostEntity)

    @Query("UPDATE PostEntity Set content = :text WHERE id = :id")
    fun changeContentById(id: Long, text: String)

    fun save(post: PostEntity) =
        if (post.id == 0L) insert(post) else changeContentById(post.id, post.content.toString())

    @Query(
        """
            UPDATE PostEntity SET
                shared = shared + 1,
                toShare = 1
            WHERE id = :id;
        """
    )
    fun toShareById(id: Long)

    @Query(
        """
            UPDATE PostEntity SET
                numberLikes = numberLikes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
                likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
            WHERE id = :id;
        """
    )
    fun likeById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    fun removeById(id: Long)

    @Insert
    fun insertDraft(contentDraftEntity: ContentDraftEntity)

    fun saveDraft(draft: String) = insertDraft(ContentDraftEntity(contentDraft = draft))

    @Query("DELETE FROM ContentDraftEntity")
    fun removeDraft()

    @Query("SELECT * FROM ContentDraftEntity")
    fun getDraft(): String?
}