package ru.netology.nmedia.adapter

import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.view.CountCalculator

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onLikeClicked: OnLikeClicked,
    private val onShareClicked: OnShareClicked
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
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

            author.text = post.author
            content.text = post.content
            published.text = post.published
            numberLikes.text = CountCalculator.calculator(post.numberLikes)
            shared.text = CountCalculator.calculator(post.shared)
            numberViews.text = CountCalculator.calculator(post.numberViews)

            like.setOnClickListener {
                onLikeClicked(post)
            }

            toShare.setOnClickListener {
                onShareClicked(post)
            }
        }
    }
}