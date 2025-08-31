package ru.netology.nmedia.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.ContentDraftEntity
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.util.Converter

@Database(entities = [PostEntity::class, ContentDraftEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class AppDb : RoomDatabase() {
    abstract val postDao: PostDao
}