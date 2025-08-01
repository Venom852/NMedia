package ru.netology.nmedia.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.intObjectMapOf
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.gson.Gson
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentPhotoBinding
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.fragment.PostFragment
import ru.netology.nmedia.fragment.PostFragment.Companion.textPost
import ru.netology.nmedia.util.CountCalculator
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.util.StringArg.getValue
import ru.netology.nmedia.util.StringArg.setValue
import ru.netology.nmedia.viewmodel.PostViewModel
import kotlin.getValue

class PhotoFragment : Fragment() {

    companion object {
        private var post = Post(
            id = 0,
            author = "Me",
            authorAvatar = "netology",
            video = null,
            content = "",
            published = 0,
            likedByMe = false,
            toShare = false,
            likes = 0,
            attachment = null,
            shared = 0,
            numberViews = 0,
            savedOnTheServer = false,
            viewed = true
        )
        private val gson = Gson()
        private var postId: Long = 0
        var Bundle.textPost by StringArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPhotoBinding.inflate(layoutInflater, container, false)
        val viewModel: PostViewModel by activityViewModels()
        applyInset(binding.photoFragment)

        arguments?.textPost?.let {
            post = gson.fromJson(it, Post::class.java)
            postId = post.id
            arguments?.textPost = null
        }

        with(binding) {
            setValues(binding, post)

            like.setOnClickListener {
                viewModel.likeById(post.id)
            }

            toShare.setOnClickListener {
                viewModel.toShareById(post.id)
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, post.content)
                }
                val chooser = Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(chooser)
            }

            back.setOnClickListener {
                findNavController().navigateUp()
            }

            viewModel.data.observe(viewLifecycleOwner) {
                it.posts.forEach { post ->
                    if (post.id == postId) {
                        Companion.post = post
                        setValues(binding, post)
                    }
                }
            }
        }

        return binding.root
    }

    private fun setValues(binding: FragmentPhotoBinding, post: Post) {
        with(binding) {
            like.isChecked = post.likedByMe
            toShare.isChecked = post.toShare
            like.text = CountCalculator.calculator(post.likes)
            toShare.text = CountCalculator.calculator(post.shared)
            views.text = CountCalculator.calculator(post.numberViews)
//            photo.setImageURI(post.attachment?.uri?.toUri())

            val urlAttachment = "${BuildConfig.BASE_URL}/media/${post.attachment?.url}"
            Glide.with(binding.photo)
                .load(urlAttachment)
                .error(R.drawable.ic_error_24)
                .timeout(10_000)
                .into(binding.photo)

//            if (post.attachment?.url != null) {
//                Glide.with(binding.photo)
//                    .load(urlAttachment)
//                    .error(R.drawable.ic_error_24)
//                    .timeout(10_000)
//                    .into(binding.photo)
//            }
        }
    }

    private fun applyInset(main: View) {
        ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            v.setPadding(
                v.paddingLeft,
                systemBars.top,
                v.paddingRight,
                if (isImeVisible) imeInsets.bottom else systemBars.bottom
            )
            insets
        }
    }
}