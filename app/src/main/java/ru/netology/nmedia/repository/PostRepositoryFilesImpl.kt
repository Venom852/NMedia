package ru.netology.nmedia.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Post

class PostRepositoryFilesImpl(private val context: Context) : PostRepository {

    companion object{
        private val gson = Gson()
        private val typeToken = TypeToken.getParameterized(List::class.java, Post::class.java).type
        private const val FILENAME = "posts.json"
    }

    private var nextId = 1L
    private var posts = emptyList<Post>()
        set(value) {
            field = value
            sync()
        }
//        Post(
//        id = 1,
//        author = "Нетология. Университет интернет-профессий будущего",
//        video = null,
//        content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
//        published = "21 мая в 18:36",
//        likedByMe = false,
//        toShare = false,
//        numberLikes = 1099,
//        shared = 1099,
//        numberViews = 1100
//    ),
//        Post(id = 2,
//        author = "Нетология. Университет интернет-профессий будущего",
//        video = "https://rutube.ru/video/8b07ab2281235edaec284f914a355817/",
//        content = null,
//        published = "21 мая в 18:36",
//        likedByMe = false,
//        toShare = false,
//        numberLikes = 1099,
//        shared = 1099,
//        numberViews = 1100
//    )

    private val data = MutableLiveData(posts)

    init {
        val file = context.filesDir.resolve(FILENAME)
        if (file.exists()) {
            context.openFileInput(FILENAME).bufferedReader().use {
                posts = gson.fromJson(it, typeToken)
                nextId = (posts.maxOfOrNull { it.id } ?: 0) + 1
                data.value = posts
            }
        }
    }

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

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
        data.value = posts
    }

    override fun save(post: Post) {
        posts = if (post.id == 0L) {
            listOf(
                post.copy(id = nextId++)
            ) + posts
        } else {
            posts.map {
                if (it.id != post.id) it else post.copy(content = post.content)
            }
        }
        data.value = posts
    }

    private fun sync() {
        context.openFileOutput(FILENAME, Context.MODE_PRIVATE).bufferedWriter().use {
            it.write(gson.toJson(posts))
        }
    }
}