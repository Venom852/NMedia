package ru.netology.nmedia.repository

import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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
import ru.netology.nmedia.error.AppError

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {
    override val data = dao.getAll().map { it.toDto() }.flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        try {
            val response = PostsApi.retrofitService.getAll()

            if (response.isSuccessful) {
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                var newBody = emptyList<Post>()
                val postsDao = data.asLiveData().value.orEmpty()

                postsDao.forEach { post ->
                    newBody = body.map { postServer ->
                        if (postServer.id == post.id) postServer.copy(viewed = true) else postServer
                    }
                }

                dao.insertPosts(newBody.map { it.copy(savedOnTheServer = true) }.toEntity())
                return
            }

            if (response.code() in 400..599) {
                throw ErrorCode400And500
            }

            throw ApiError(response.code(), response.message())
        } catch (e: ErrorCode400And500) {
            throw ErrorCode400And500
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = PostsApi.retrofitService.getNewer(id)
            if (response.isSuccessful) {
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                dao.insertPosts(body.map { it.copy(savedOnTheServer = true) }.toEntity())
                emit(body.size)
                return@flow
            }

            if (response.code() in 400..599) {
                throw ErrorCode400And500
            }

            throw ApiError(response.code(), response.message())
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun likeById(id: Long, postLikedByMe: Boolean?) {
        try {
            if (postLikedByMe != null && !postLikedByMe) {
                val response = PostsApi.retrofitService.likeById(id)

                if (response.isSuccessful) {
                    return
                }

                if (response.code() in 400..599) {
                    throw ErrorCode400And500
                }

                throw ApiError(response.code(), response.message())
            } else {
                val response = PostsApi.retrofitService.dislikeById(id)

                if (response.isSuccessful) {
                    return
                }

                if (response.code() in 400..599) {
                    throw ErrorCode400And500
                }

                throw ApiError(response.code(), response.message())
            }
        } catch (e: ErrorCode400And500) {
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

            if (response.code() in 400..599) {
                throw ErrorCode400And500
            }

            throw ApiError(response.code(), response.message())
        } catch (e: ErrorCode400And500) {
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

            if (response.code() in 400..599) {
                throw ErrorCode400And500
            }

            throw ApiError(response.code(), response.message())
        } catch (e: ErrorCode400And500) {
            throw ErrorCode400And500
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}