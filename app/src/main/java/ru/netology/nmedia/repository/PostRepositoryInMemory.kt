package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post

class PostRepositoryInMemory : PostRepository {
    private var posts = List(10) { index ->
        val id = index + 1L
        Post(
            id = id,
            author = "Нетология. Университет интернет-профессий будущего",
            content = "$id Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            published = "21 мая в 18:36",
            likedByMe = false,
            toShare = false,
            numberLikes = 1099,
            shared = 1099,
            numberViews = 1100
        )
    }.reversed()

    private val data = MutableLiveData(posts)

    override fun get(): LiveData<List<Post>> = data

    override fun likeById(id: Long) {
        posts = posts.map { post ->
            if (id == post.id) {
                post.copy(
                    likedByMe = !post.likedByMe,
                    numberLikes = if (post.likedByMe) {
                        post.numberLikes - 1
                    } else {
                        post.numberLikes + 1
                    }
                )
            } else {
                post
            }
        }
        data.value = posts
    }

    override fun toShareById(id: Long) {
        posts = posts.map { post ->
            if (id == post.id) {
                post.copy(
                    toShare = true,
                    shared = post.shared + 1
                )
            } else {
                post
            }
        }
        data.value = posts
    }
}