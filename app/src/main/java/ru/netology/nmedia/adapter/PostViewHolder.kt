package ru.netology.nmedia.adapter

import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.view.CountCalculator

class PostViewHolder(
    private val bindingActivity: ActivityMainBinding,
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
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
            bindingActivity.group.visibility = View.GONE

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            toShare.setOnClickListener {
                onInteractionListener.onShare(post)
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { menuItem ->
                        when(menuItem.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                bindingActivity.group.visibility = View.VISIBLE
                                onInteractionListener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}