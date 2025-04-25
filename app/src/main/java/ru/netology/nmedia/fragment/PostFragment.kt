package ru.netology.nmedia.fragment

import LongArg
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.databinding.FragmentPostBinding
import com.google.gson.Gson
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.fragment.NewPostFragment.Companion.NEW_POST_KEY
import ru.netology.nmedia.fragment.NewPostFragment.Companion.textArg
import ru.netology.nmedia.fragment.NewPostFragment.Companion.textContentArg
import ru.netology.nmedia.util.CountCalculator
import ru.netology.nmedia.util.StringArg

class PostFragment : Fragment() {

    companion object{
        private var post = Post(
            id = 0,
            author = "Me",
            video = null,
            content = "",
            published = "New",
            likedByMe = false,
            toShare = false,
            numberLikes = 0,
            shared = 0,
            numberViews = 0)
        private val gson = Gson()
        const val LONG_KEY = "longKey"
        var Bundle.textPost by StringArg
//        var Bundle.idPost by LongArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        enableEdgeToEdge()
        val binding = FragmentPostBinding.inflate(layoutInflater, container, false)
        applyInset(binding.postFragment)
        val viewModel: PostViewModel by activityViewModels()

        arguments?.textPost?.let {
            post = gson.fromJson(it, Post::class.java)
            arguments?.textPost = null
        }
        with(binding){
            author.text = post.author
            content.text = post.content
            published.text = post.published
            like.isChecked = post.likedByMe
            toShare.isChecked = post.toShare
            like.text = CountCalculator.calculator(post.numberLikes)
            toShare.text = CountCalculator.calculator(post.shared)
            views.text = CountCalculator.calculator(post.numberViews)
            groupVideo.visibility = View.VISIBLE
            content.visibility = View.VISIBLE

            if (post.video == null) {
                groupVideo.visibility = View.GONE
            }

            if (post.content == null) {
                content.visibility = View.GONE
            }

            like.setOnClickListener {
                viewModel.likeById(post.id)
                findNavController()
                    .navigate(R.id.feedFragment, Bundle().apply {
                        putLong(LONG_KEY, post.id)
                    }
                )
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
                findNavController()
                    .navigate(R.id.feedFragment, Bundle().apply {
                        putLong(LONG_KEY, post.id)
                    }
                )
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
        }
        return binding.root
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