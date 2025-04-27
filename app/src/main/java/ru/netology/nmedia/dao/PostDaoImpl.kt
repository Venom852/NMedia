package ru.netology.nmedia.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.provider.Telephony.Mms.Draft
import ru.netology.nmedia.dto.Post

class PostDaoImpl(private val db: SQLiteDatabase) : PostDao {
    companion object {
        val DDL = """
        CREATE TABLE ${PostColumns.TABLE} (
            ${PostColumns.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${PostColumns.COLUMN_AUTHOR} TEXT NOT NULL,
            ${PostColumns.COLUMN_VIDEO} TEXT,
            ${PostColumns.COLUMN_CONTENT} TEXT,
            ${PostColumns.COLUMN_PUBLISHED} TEXT NOT NULL,
            ${PostColumns.COLUMN_LIKED_BY_ME} BOOLEAN NOT NULL DEFAULT 0,
            ${PostColumns.COLUMN_TO_SHARE} BOOLEAN NOT NULL DEFAULT 0,
            ${PostColumns.COLUMN_NUMBER_LIKES} INTEGER NOT NULL DEFAULT 0,
            ${PostColumns.COLUMN_SHARED} INTEGER NOT NULL DEFAULT 0,
            ${PostColumns.COLUMN_NUMBER_VIEWS} INTEGER NOT NULL DEFAULT 0 
        );
        
        CREATE TABLE ${DraftColumns.TABLE} (
            ${DraftColumns.COLUMN_CONTENT_DRAFT} TEXT UNIQUE
        );
        """.trimIndent()
    }

    object PostColumns {
        const val TABLE = "posts"
        const val COLUMN_ID = "id"
        const val COLUMN_AUTHOR = "author"
        const val COLUMN_VIDEO = "video"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_PUBLISHED = "published"
        const val COLUMN_LIKED_BY_ME = "likedByMe"
        const val COLUMN_TO_SHARE = "toShare"
        const val COLUMN_NUMBER_LIKES = "numberLikes"
        const val COLUMN_SHARED = "shared"
        const val COLUMN_NUMBER_VIEWS = "numberViews"
        val ALL_COLUMNS = arrayOf(
            COLUMN_ID,
            COLUMN_AUTHOR,
            COLUMN_VIDEO,
            COLUMN_CONTENT,
            COLUMN_PUBLISHED,
            COLUMN_LIKED_BY_ME,
            COLUMN_TO_SHARE,
            COLUMN_NUMBER_LIKES,
            COLUMN_SHARED,
            COLUMN_NUMBER_VIEWS
        )
    }

    object DraftColumns {
        const val TABLE = "draft"
//        const val COLUMN_ID = "id"
        const val COLUMN_CONTENT_DRAFT = "contentDraft"
//        val ALL_COLUMNS = arrayOf(
//            COLUMN_ID,
//            COLUMN_CONTENT_DRAFT
//        )
    }

    override fun getAll(): List<Post> {
        val posts = mutableListOf<Post>()
        db.query(
            PostColumns.TABLE,
            PostColumns.ALL_COLUMNS,
            null,
            null,
            null,
            null,
            "${PostColumns.COLUMN_ID} DESC"
        ).use {
            while (it.moveToNext()) {
                posts.add(map(it))
            }
        }
        return posts
    }

    override fun save(post: Post): Post {
        val values = ContentValues().apply {
            // TODO: remove hardcoded values
            put(PostColumns.COLUMN_AUTHOR, "Me")
            put(PostColumns.COLUMN_CONTENT, post.content)
            put(PostColumns.COLUMN_PUBLISHED, "now")
        }
        val id = if (post.id != 0L) {
            db.update(
                PostColumns.TABLE,
                values,
                "${PostColumns.COLUMN_ID} = ?",
                arrayOf(post.id.toString()),
            )
            post.id
        } else {
            db.insert(PostColumns.TABLE, null, values)
        }
        db.query(
            PostColumns.TABLE,
            PostColumns.ALL_COLUMNS,
            "${PostColumns.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null,
        ).use {
            it.moveToNext()
            return map(it)
        }
    }

    override fun likeById(id: Long) {
        db.execSQL(
            """
           UPDATE posts SET
               numberLikes = numberLikes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
               likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
           WHERE id = ?;
        """.trimIndent(), arrayOf(id)
        )
    }

    override fun toShareById(id: Long) {
        db.execSQL(
            """
           UPDATE posts SET
               shared = shared + 1,
               toShare = 1
           WHERE id = ?;
        """.trimIndent(), arrayOf(id)
        )
    }

    override fun removeById(id: Long) {
        db.delete(
            PostColumns.TABLE,
            "${PostColumns.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    private fun map(cursor: Cursor): Post {
        with(cursor) {
            return Post(
                id = getLong(getColumnIndexOrThrow(PostColumns.COLUMN_ID)),
                author = getString(getColumnIndexOrThrow(PostColumns.COLUMN_AUTHOR)),
                video = getString(getColumnIndexOrThrow(PostColumns.COLUMN_VIDEO)),
                content = getString(getColumnIndexOrThrow(PostColumns.COLUMN_CONTENT)),
                published = getString(getColumnIndexOrThrow(PostColumns.COLUMN_PUBLISHED)),
                likedByMe = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_LIKED_BY_ME)) != 0,
                toShare = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_TO_SHARE)) != 0,
                numberLikes = getLong(getColumnIndexOrThrow(PostColumns.COLUMN_NUMBER_LIKES)),
                shared = getLong(getColumnIndexOrThrow(PostColumns.COLUMN_SHARED)),
                numberViews = getLong(getColumnIndexOrThrow(PostColumns.COLUMN_NUMBER_VIEWS))
            )
        }
    }

    override fun saveDraft(draft: String) {
        val values = ContentValues().apply {
            put(DraftColumns.COLUMN_CONTENT_DRAFT, draft)
        }

        db.insert(DraftColumns.TABLE, null, values)
    }

    override fun removeDraft() {
        db.delete(
            DraftColumns.TABLE,
            null,
            null
        )
    }

    override fun getDraft(): String? {
        db.query(
            DraftColumns.TABLE,
            arrayOf(DraftColumns.COLUMN_CONTENT_DRAFT),
            null,
            null,
            null,
            null,
            null
        ).use {
            it.moveToNext()
            return it.getString(it.getColumnIndexOrThrow(DraftColumns.COLUMN_CONTENT_DRAFT))
        }
    }
}