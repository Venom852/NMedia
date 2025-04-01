package ru.netology.nmedia.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyInset(binding.main)

        val viewModel: PostViewModel by viewModels()
        viewModel.data.observe(this) {post ->
            with(binding) {
                like.setImageResource(
                    if (post.likedByMe) {
                        R.drawable.ic_liked_24
                    } else {
                        R.drawable.ic_like_24
                    }
                )

                toShare.setImageResource(
                    if (post.toShare) {
                        R.drawable.ic_shared_24
                    } else {
                        R.drawable.ic_to_share_24
                    }
                )

                like.setOnClickListener {
                    viewModel.like()
                }

                toShare.setOnClickListener {
                    viewModel.toShare()
                }

                author.text = post.author
                content.text = post.content
                published.text = post.published
                numberLikes.text = CountCalculator.calculator(post.numberLikes)
                shared.text = CountCalculator.calculator(post.shared)
                numberViews.text = CountCalculator.calculator(post.numberViews)
            }
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