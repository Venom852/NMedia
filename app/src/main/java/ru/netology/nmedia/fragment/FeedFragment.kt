package ru.netology.nmedia.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import com.google.gson.Gson
import ru.netology.nmedia.fragment.NewPostFragment.Companion.NEW_POST_KEY
import ru.netology.nmedia.fragment.NewPostFragment.Companion.textContentArg
import ru.netology.nmedia.fragment.PostFragment.Companion.LONG_KEY
import ru.netology.nmedia.fragment.PostFragment.Companion.textPost

class FeedFragment : Fragment() {
    @SuppressLint("SetTextI18n")

    companion object{
        private val gson = Gson()
        private var postId: Long = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        enableEdgeToEdge()
        val binding = FragmentFeedBinding.inflate(layoutInflater, container, false)
        applyInset(binding.main)

        val viewModel: PostViewModel by activityViewModels()
        val adapter = PostAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, post.content)
                }
                val chooser = Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(chooser)
                viewModel.toShareById(post.id)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onEdit(post: Post) {
                viewModel.editById(post)
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textContentArg = post.content
                    }
                )
            }
        })

        arguments?.getLong(LONG_KEY).let {
            if (it != null) {
                postId = it
            }
            arguments?.remove(LONG_KEY)
        }

        binding.main.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner) { posts ->
            posts.forEach{
                if (it.id == postId) {
                    postId = 0
                    findNavController()
                        .navigate(R.id.postFragment2, Bundle().apply {
                            textPost = gson.toJson(it)
                    })
                }
            }
            val newPost = posts.size > adapter.currentList.size && adapter.currentList.isNotEmpty()
            adapter.submitList(posts) {
                if (newPost) {
                    binding.main.smoothScrollToPosition(0)
                }
            }
        }

        binding.add.setOnClickListener{
            findNavController().navigate(
                R.id.action_feedFragment_to_newPostFragment,
                Bundle().apply {
                    textContentArg = NEW_POST_KEY
                }
            )
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