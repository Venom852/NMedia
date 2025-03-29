package ru.netology.nmedia

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyInset(binding.main)

        val post = Post(
            id = 1,
            author = "Нетология. Университет интернет-профессий будущего",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            published = "21 мая в 18:36",
            likedByMe = false,
            toShare = false,
            numberLikes = 1099,
            shared = 1099,
            numberViews = 1100
        )

        with(binding) {
            like.setImageResource(
                if (post.likedByMe) {
                    R.drawable.ic_liked_24
                } else {
                    R.drawable.ic_like_24
                }
            )

            numberLikes.text = CountCalculator.calculator(post.numberLikes)

            toShare.setImageResource(
                if (post.toShare) {
                    R.drawable.ic_shared_24
                } else {
                    R.drawable.ic_to_share_24
                }
            )

            shared.text = CountCalculator.calculator(post.shared)
            numberViews.text = CountCalculator.calculator(post.numberViews)

            like.setOnClickListener {
                post.likedByMe = !post.likedByMe
                like.setImageResource(
                    if (post.likedByMe) {
                        R.drawable.ic_liked_24
                    } else {
                        R.drawable.ic_like_24
                    }
                )

                post.numberLikes = if (post.likedByMe) {
                    post.numberLikes + 1
                } else {
                    post.numberLikes - 1
                }

                numberLikes.text = CountCalculator.calculator(post.numberLikes)
            }

            toShare.setOnClickListener {
                post.toShare = true
                toShare.setImageResource(R.drawable.ic_shared_24)
                post.shared += 1

                shared.text = CountCalculator.calculator(post.shared)
            }

            author.text = post.author
            content.text = post.content
            published.text = post.published
            numberLikes.text = post.numberLikes.toString()
            shared.text = CountCalculator.calculator(post.shared)
            numberViews.text = CountCalculator.calculator(post.numberViews)
        }
    }

    private fun applyInset(main: View) {
        ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft + systemBars.left,
                v.paddingTop + systemBars.top,
                v.paddingRight + systemBars.right,
                v.paddingBottom + systemBars.bottom
            )
            insets
        }
    }
}