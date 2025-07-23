package ru.netology.nmedia.repository

import androidx.lifecycle.*
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.ErrorCode400And500
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {
    override val data = dao.getAll().map{ it.toDto()}

    override suspend fun getAll() {
        try {
            val response = PostsApi.retrofitService.getAll()

            if (response.isSuccessful) {
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                dao.insertPosts(body.map { it.copy(savedOnTheServer = true) }.toEntity())
                return
            }

            if (response.code() in 400..599){
                throw ErrorCode400And500
            }

            throw ApiError(response.code(), response.message())
        } catch (e: ErrorCode400And500){
            throw ErrorCode400And500
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long, postLikedByMe: Boolean?) {
        try {
            if (postLikedByMe != null && !postLikedByMe){
                val response = PostsApi.retrofitService.likeById(id)

                if (response.isSuccessful) {
                    return
                }

                if (response.code() in 400..599){
                    throw ErrorCode400And500
                }

                throw ApiError(response.code(), response.message())
            } else {
                val response = PostsApi.retrofitService.dislikeById(id)

                if (response.isSuccessful) {
                    return
                }

                if (response.code() in 400..599){
                    throw ErrorCode400And500
                }

                throw ApiError(response.code(), response.message())
            }
        } catch (e: ErrorCode400And500){
            throw ErrorCode400And500
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun toShareById(id: Long) {
        println()
    }

    override suspend fun save(post: Post): Post {
        try {
            val response = PostsApi.retrofitService.save(post)

            if (response.isSuccessful) {
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                return body
            }

            if (response.code() in 400..599){
                throw ErrorCode400And500
            }

            throw ApiError(response.code(), response.message())
        } catch (e: ErrorCode400And500){
            throw ErrorCode400And500
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            val response = PostsApi.retrofitService.removeById(id)

            if (response.isSuccessful) {
                return
            }

            if (response.code() in 400..599){
                throw ErrorCode400And500
            }

            throw ApiError(response.code(), response.message())
        } catch (e: ErrorCode400And500){
            throw ErrorCode400And500
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}