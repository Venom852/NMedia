package ru.netology.nmedia.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import ru.netology.nmedia.dto.Attachment
import kotlin.jvm.java

object Converter {
    private val gson = Gson()

    @TypeConverter
    fun convertToJson(attachment: Attachment?): String? = gson.toJson(attachment)

    @TypeConverter
    fun convertFromJson(string: String): Attachment? = gson.fromJson(string, Attachment::class.java)
}