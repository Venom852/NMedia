package ru.netology.nmedia.adapter

import android.content.Intent
import android.content.pm.PackageManager.MATCH_ALL
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.Group
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.fragment.PostFragment.Companion.textPost
import ru.netology.nmedia.util.CountCalculator


class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
    private val gson: Gson = Gson()
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        with(binding) {
            author.text = post.author
            content.text = post.content
            published.text = post.published.toString()
            like.isChecked = post.likedByMe
            toShare.isChecked = post.toShare
            like.text = CountCalculator.calculator(post.likes)
            toShare.text = CountCalculator.calculator(post.shared)
            views.text = CountCalculator.calculator(post.numberViews)
            groupVideo.visibility = View.VISIBLE
            content.visibility = View.VISIBLE
            imageContent.visibility = View.VISIBLE

            val url = "http://10.0.2.2:9999/avatars/${post.authorAvatar}"
            val urlAttachment = "http://10.0.2.2:9999/images/${post.attachment?.url}"
            val options = RequestOptions()

            if (post.attachment == null) {
                imageContent.visibility = View.GONE
            } else {
                Glide.with(binding.imageContent)
                    .load(urlAttachment)
                    .error(R.drawable.ic_error_24)
                    .timeout(10_000)
                    .into(binding.imageContent)
            }

            if (post.video == null) {
                groupVideo.visibility = View.GONE
            }

            if (post.content == null) {
                content.visibility = View.GONE
            }

            if (post.authorAvatar != "netology") {
                Glide.with(binding.avatar)
                    .load(url)
                    .error(R.drawable.ic_error_24)
                    .timeout(10_000)
                    .apply(options.circleCrop())
                    .into(binding.avatar)
            } else {
                avatar.setImageResource(R.drawable.ic_netology)
            }

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            toShare.setOnClickListener {
                onInteractionListener.onShare(post)
            }

            groupPost.setAllOnClickListener {
                findNavController(it).navigate(
                    R.id.action_feedFragment_to_postFragment2,
                    Bundle().apply {
                        textPost = gson.toJson(post)
                    })
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
                println(it.context.packageManager.queryIntentActivities(intent, MATCH_ALL))
                it.context.startActivity(intent)
            }
            play.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                println(intent.resolveActivity(it.context.packageManager))
                println(intent.resolveActivityInfo(it.context.packageManager, MATCH_ALL))
                println(it.context.packageManager.queryIntentActivities(intent, MATCH_ALL))
                it.context.startActivity(intent)
            }
        }
    }

    private fun Group.setAllOnClickListener(listener: (View) -> Unit) {
        referencedIds.forEach {
            rootView.setOnClickListener(listener)
        }
    }
}