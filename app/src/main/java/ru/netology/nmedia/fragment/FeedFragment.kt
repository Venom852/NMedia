package ru.netology.nmedia.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.databinding.ErrorCode300Binding
import ru.netology.nmedia.databinding.ErrorCode400And500Binding
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.fragment.NewPostFragment.Companion.NEW_POST_KEY
import ru.netology.nmedia.fragment.NewPostFragment.Companion.textContentArg

class FeedFragment : Fragment() {
    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        enableEdgeToEdge()
        val binding = FragmentFeedBinding.inflate(layoutInflater, container, false)
        val bindingErrorCode300 = ErrorCode300Binding.inflate(layoutInflater, container, false)
        val bindingErrorCode400And500 = ErrorCode400And500Binding.inflate(layoutInflater, container, false)
        val dialog = BottomSheetDialog(requireContext())
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

        binding.main.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.progress.isVisible = state.loading
            binding.errorGroup.isVisible = state.error
            binding.emptyText.isVisible = state.empty
            binding.error300Group.isVisible = state.errorCode300
        }

        viewModel.bottomSheet.observe(viewLifecycleOwner) {
            dialog.setCancelable(false)
            dialog.setContentView(bindingErrorCode400And500.root)
            dialog.show()
        }

        binding.retryButton.setOnClickListener {
            viewModel.loadPosts()
        }

        binding.add.setOnClickListener {
            findNavController().navigate(
                R.id.action_feedFragment_to_newPostFragment,
                Bundle().apply {
                    textContentArg = NEW_POST_KEY
                }
            )
        }

        binding.srl.setOnRefreshListener {
            binding.srl.isRefreshing = false
            viewModel.loadPosts()
        }

        binding.buttonError.setOnClickListener {
            viewModel.loadPostsWithoutServer()
        }

        bindingErrorCode400And500.errorCode400And500.setOnClickListener {
            dialog.dismiss()
        }

//        bindingErrorCode400And500.errorCode400And500.setOnTouchListener(object : SwipeListener(this) {
//            override fun onSwipeDown() {
//                dialog.dismiss()
//            }
//        }

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