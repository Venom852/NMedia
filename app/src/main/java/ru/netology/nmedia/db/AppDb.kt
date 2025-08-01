package ru.netology.nmedia.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.ContentDraftEntity
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.util.Converter

@Database(entities = [PostEntity::class, ContentDraftEntity::class], version = 1)
@TypeConverters(Converter::class)
abstract class AppDb : RoomDatabase() {
    abstract val postDao: PostDao

    companion object {
        @Volatile
        private var instance: AppDb? = null

        fun getInstance(context: Context): AppDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) = Room
            .databaseBuilder(context, AppDb::class.java, "app.db")
            .build()
    }
}