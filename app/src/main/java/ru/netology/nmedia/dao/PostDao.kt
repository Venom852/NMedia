package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.ContentDraftEntity
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity WHERE viewed == 1 ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM PostEntity")
    suspend fun count(): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Query("UPDATE PostEntity Set content = :text WHERE id = :id")
    suspend fun changeContentById(id: Long, text: String)

    @Query("UPDATE PostEntity Set id = :newId, savedOnTheServer = :savedOnTheServer WHERE id = :id")
    suspend fun changeIdPostById(id: Long, newId: Long, savedOnTheServer: Boolean)

    @Query("UPDATE PostEntity Set viewed = 1")
    suspend fun browse()

    suspend fun save(post: PostEntity) =
        if (post.id == 0L) insert(post) else changeContentById(post.id, post.content.toString())

    @Query(
        """
            UPDATE PostEntity SET
                shared = shared + 1,
                toShare = 1
            WHERE id = :id;
        """
    )
    suspend fun toShareById(id: Long)

    @Query(
        """
            UPDATE PostEntity SET
                numberLikes = numberLikes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
                likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
            WHERE id = :id;
        """
    )
    suspend fun likeById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(contentDraftEntity: ContentDraftEntity)

    suspend fun saveDraft(draft: String) = insertDraft(ContentDraftEntity(contentDraft = draft))

    @Query("DELETE FROM ContentDraftEntity")
    suspend fun removeDraft()

    @Query("SELECT * FROM ContentDraftEntity")
    suspend fun getDraft(): String?
}