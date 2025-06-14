package ru.netology.nmedia.repository

import retrofit2.Response
import retrofit2.Callback
import retrofit2.Call
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dto.Post

class PostRepositoryImpl() : PostRepository {
    override fun getAllAsync(callback: PostRepository.Callback<List<Post>>) {
        PostsApi.retrofitService.getAll().enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))
                    return
                }

                if (response.code() in 300..399){
                    callback.onErrorCode300(RuntimeException(response.message()))
                    return
                }

                if (response.code() in 400..511){
                    callback.onErrorCode400And500(RuntimeException(response.message()))
                    return
                }
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                callback.onError(Exception(t))
            }
        })
    }

    override fun likeByIdAsync(id: Long, postLikedByMe: Boolean, callback: PostRepository.Callback<Unit>) {
        if (!postLikedByMe) {
            PostsApi.retrofitService.likeById(id).enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post?>, response: Response<Post?>) {
                    if (response.isSuccessful) {
                        callback.onSuccess(Unit)
                        return
                    }

                    if (response.code() in 300..399){
                        callback.onErrorCode300(RuntimeException(response.message()))
                        return
                    }

                    if (response.code() in 400..511){
                        callback.onErrorCode400And500(RuntimeException(response.message()))
                        return
                    }
                }

                override fun onFailure(call: Call<Post?>, t: Throwable) {
                    callback.onError(Exception(t))
                }
            })
        } else {
            PostsApi.retrofitService.dislikeById(id).enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post?>, response: Response<Post?>) {
                    if (response.isSuccessful) {
                        callback.onSuccess(Unit)
                        return
                    }

                    if (response.code() in 300..399){
                        callback.onErrorCode300(RuntimeException(response.message()))
                        return
                    }

                    if (response.code() in 400..511){
                        callback.onErrorCode400And500(RuntimeException(response.message()))
                        return
                    }
                }

                override fun onFailure(call: Call<Post?>, t: Throwable) {
                    callback.onError(Exception(t))
                }
            })
        }
    }

    override fun toShareById(id: Long) {
        println()
    }

    override fun saveAsync(post: Post, callback: PostRepository.Callback<Post>) {
        PostsApi.retrofitService.save(post).enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))
                    return
                }

                if (response.code() in 300..399){
                    callback.onErrorCode300(RuntimeException(response.message()))
                    return
                }

                if (response.code() in 400..511){
                    callback.onErrorCode400And500(RuntimeException(response.message()))
                    return
                }
            }

            override fun onFailure(call: Call<Post?>, t: Throwable) {
                callback.onError(Exception(t))
            }
        })
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.Callback<Unit>) {
        PostsApi.retrofitService.removeById(id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit?>, response: Response<Unit?>) {
                if (response.isSuccessful) {
                    callback.onSuccess(Unit)
                    return
                }

                if (response.code() in 300..399){
                    callback.onErrorCode300(RuntimeException(response.message()))
                    return
                }

                if (response.code() in 400..511){
                    callback.onErrorCode400And500(RuntimeException(response.message()))
                    return
                }
            }

            override fun onFailure(call: Call<Unit?>, t: Throwable) {
                callback.onError(Exception(t))
            }
        })
    }
}