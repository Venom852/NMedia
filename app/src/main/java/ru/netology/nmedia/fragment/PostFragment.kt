package ru.netology.nmedia.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.netology.nmedia.R
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.databinding.FragmentPostBinding
import com.google.gson.Gson
import ru.netology.nmedia.databinding.ErrorCode400And500Binding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.fragment.NewPostFragment.Companion.textContentArg
import ru.netology.nmedia.util.CountCalculator
import ru.netology.nmedia.util.StringArg

class PostFragment : Fragment() {

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
            numberViews = 0
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
//        enableEdgeToEdge()
        val binding = FragmentPostBinding.inflate(layoutInflater, container, false)
        val bindingErrorCode400And500 = ErrorCode400And500Binding.inflate(layoutInflater, container, false)
        applyInset(binding.postFragment)
        val viewModel: PostViewModel by activityViewModels()
        val dialog = BottomSheetDialog(requireContext())

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

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.remove -> {
                                viewModel.removeById(post.id)
                                findNavController().navigateUp()
                                true
                            }

                            R.id.edit -> {
                                viewModel.editById(post)
                                findNavController().navigate(
                                    R.id.action_postFragment2_to_newPostFragment,
                                    Bundle().apply {
                                        textContentArg = post.content
                                    }
                                )
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            viewModel.data.observe(viewLifecycleOwner) {
                it.posts.forEach { post ->
                    if (post.id == postId) {
                        Companion.post = post
                        setValues(binding, post)
                    }
                }
                if (it.errorCode300){
                    findNavController().navigateUp()
                }
            }

            viewModel.bottomSheet.observe(viewLifecycleOwner) {
                dialog.setCancelable(false)
                dialog.setContentView(bindingErrorCode400And500.root)
                dialog.show()
            }

            bindingErrorCode400And500.errorCode400And500.setOnClickListener {
                dialog.dismiss()
            }
        }
        return binding.root
    }

    private fun setValues(binding: FragmentPostBinding, post: Post) {
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