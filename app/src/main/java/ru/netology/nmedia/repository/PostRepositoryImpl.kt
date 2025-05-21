package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

class PostRepositoryImpl : PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }

//    override fun getAll(): List<Post> {
//        val request: Request = Request.Builder()
//            .url("${BASE_URL}/api/slow/posts")
//            .build()
//
//        return client.newCall(request)
//            .execute()
//            .let { it.body?.string() ?: throw RuntimeException("body is null") }
//            .let {
//                gson.fromJson(it, typeToken.type)
//            }
//    }

    override fun getAllAsync(callback: PostRepository.Callback<List<Post>>) {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string() ?: throw RuntimeException("body is null")
                    try {
                        callback.onSuccess(gson.fromJson(body, typeToken.type))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
            })
    }

//    override fun likeById(id: Long): Post {
//        var request: Request = Request.Builder()
//            .url("${BASE_URL}/api/slow/posts/${id}")
//            .build()
//
//        val post = client.newCall(request)
//            .execute()
//            .let { it.body?.string() ?: throw RuntimeException("body is null") }
//            .let {
//                gson.fromJson(it, Post::class.java)
//            }
//
//        request = if (!post.likedByMe) {
//            Request.Builder()
//                .url("${BASE_URL}/api/slow/posts/${id}/likes")
//                .build()
//        } else {
//            Request.Builder()
//                .delete()
//                .url("${BASE_URL}/api/slow/posts/${id}/likes")
//                .build()
//        }
//
//        return client.newCall(request)
//            .execute()
//            .let { it.body?.string() ?: throw RuntimeException("body is null") }
//            .let {
//                gson.fromJson(it, Post::class.java)
//            }
//    }

    override fun likeByIdAsync(id: Long, callback: PostRepository.Callback<Post>) {
        var request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts/${id}")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    var body = response.body?.string() ?: throw RuntimeException("body is null")
                    try {
                        val post = gson.fromJson(body, Post::class.java)

                        request = if (!post.likedByMe) {
                            Request.Builder()
                                .url("${BASE_URL}/api/slow/posts/${id}/likes")
                                .build()
                        } else {
                            Request.Builder()
                                .delete()
                                .url("${BASE_URL}/api/slow/posts/${id}/likes")
                                .build()
                        }

                        body = response.body?.string() ?: throw RuntimeException("body is null")
                        callback.onSuccess(gson.fromJson(body, Post::class.java))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
            })
    }

    override fun toShareById(id: Long) {
        TODO("Not yet implemented")
    }

    override fun save(post: Post) {
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }

    override fun removeById(id: Long) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/${id}")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }
}