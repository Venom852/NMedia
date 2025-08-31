package ru.netology.nmedia.repository

import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.ErrorCode400And500
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import ru.netology.nmedia.error.AppError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: ApiService
) : PostRepository {
    override val data = dao.getAll().map { it.toDto() }.flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        try {
            val response = apiService.getAll()

            if (response.isSuccessful) {
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                var newBody = emptyList<Post>()
                val postsDao = data.asLiveData().value.orEmpty()

                postsDao.forEach { post ->
                    newBody = body.map { postServer ->
                        if (postServer.id == post.id) postServer.copy(viewed = true) else postServer
                    }
                }

                dao.insertPosts(body.map { it.copy(savedOnTheServer = true) }.toEntity())
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
            val response = apiService.getNewer(id)
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
                val response = apiService.likeById(id)

                if (response.isSuccessful) {
                    return
                }

                if (response.code() in 400..599) {
                    throw ErrorCode400And500
                }

                throw ApiError(response.code(), response.message())
            } else {
                val response = apiService.dislikeById(id)

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
        // TODO:
    }

    override suspend fun save(post: Post): Post {
        try {
            val response = apiService.save(post)

            if (response.isSuccessful) {
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                return body
            }

            if (response.code() in 400..599) {
                throw ErrorCode400And500
            }

            throw ApiError(response.code(), response.message())
        } catch (e: AppError) {
            throw e
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
            val response = apiService.removeById(id)

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

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload): Post {
        try {
            val media = upload(upload)
            val postWithAttachment = post.copy(
                attachment = Attachment(
                    media.id,
                    AttachmentType.IMAGE,
                    post.attachment?.uri
                )
            )
            return save(postWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: ErrorCode400And500) {
            throw ErrorCode400And500
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = apiService.upload(media)
            if (response.isSuccessful) {
                return response.body() ?: throw ApiError(response.code(), response.message())
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

    override suspend fun signIn(login: String, password: String): AuthState {
        try {
            val response = apiService.updateUser(login, password)
            if (response.isSuccessful) {
                return response.body() ?: throw ApiError(response.code(), response.message())
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

    override suspend fun signUp(userName: String, login: String, password: String): AuthState {
        try {
            val response = apiService.registerUser(login, password, userName)
            if (response.isSuccessful) {
                return response.body() ?: throw ApiError(response.code(), response.message())
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

    override suspend fun signUpWithAPhoto(
        userName: String,
        login: String,
        password: String,
        media: MediaUpload
    ): AuthState {
        try {

            val media = MultipartBody.Part.createFormData(
                "file", media.file.name, media.file.asRequestBody()
            )
            val response = apiService.registerWithPhoto(
                login.toRequestBody("text/plain".toMediaType()),
                password.toRequestBody("text/plain".toMediaType()),
                userName.toRequestBody("text/plain".toMediaType()),
                media
            )
            if (response.isSuccessful) {
                return response.body() ?: throw ApiError(response.code(), response.message())
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