package ru.netology.nmedia.adapter

import android.content.Intent
import android.content.Intent.CATEGORY_DEFAULT
import android.content.pm.PackageManager.MATCH_ALL
import android.content.pm.PackageManager.MATCH_DIRECT_BOOT_AUTO
import android.net.Uri
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.CountCalculator

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        with(binding) {
            author.text = post.author
            content.text = post.content
            published.text = post.published
            like.isChecked = post.likedByMe
            toShare.isChecked = post.toShare
            like.text = CountCalculator.calculator(post.numberLikes)
            toShare.text = CountCalculator.calculator(post.shared)
            views.text = CountCalculator.calculator(post.numberViews)
            binding.groupVideo.visibility = View.VISIBLE
            binding.content.visibility = View.VISIBLE

            if (post.video == null) {
                binding.groupVideo.visibility = View.GONE
            }

            if (post.content == null) {
                binding.content.visibility = View.GONE
            }

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
                        when (menuItem.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            videoContent.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                println(intent.resolveActivity(it.context.packageManager))
                println(intent.resolveActivityInfo(it.context.packageManager, MATCH_ALL))
                println(it.context.packageManager.queryIntentActivities(intent, MATCH_DIRECT_BOOT_AUTO))
                println(it.context.packageManager.queryIntentActivities(intent, MATCH_ALL))
                it.context.startActivity(intent)
            }
            play.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                intent.resolveActivity(it.context.packageManager)
                it.context.startActivity(intent)
            }
        }
    }
}