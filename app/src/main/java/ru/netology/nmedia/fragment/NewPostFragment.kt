package ru.netology.nmedia.fragment

import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class NewPostFragment : Fragment() {
    companion object {
        const val NEW_POST_KEY = "newPost"
        private var editing = false
        var Bundle.textArg by StringArg
        var Bundle.textContentArg by StringArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(layoutInflater, container, false)
        val viewModel: PostViewModel by activityViewModels()
        val dao = AppDb.getInstance(requireContext()).postDao

        arguments?.textArg?.let {
            binding.content.setText(it)
            arguments?.textArg = null
        }

        arguments?.textContentArg?.let {
            val text = it
            if (text == NEW_POST_KEY) {
                binding.group.visibility = View.GONE
                if (dao.getDraft() != null) {
                    binding.content.setText(dao.getDraft())
                    dao.removeDraft()
                }
            } else {
                binding.content.setText(text)
                binding.cancelEdit.text = binding.content.text
                editing = true
            }
            arguments?.textContentArg = null
        }

        binding.content.requestFocus()
        binding.ok.setOnClickListener {
            if (!binding.content.text.isNullOrBlank()) {
                val content = binding.content.text.toString()
                viewModel.saveContent(content)
                findNavController().navigateUp()
            }
            viewModel.edited.value = viewModel.empty
        }

        binding.cancel.setOnClickListener {
            viewModel.edited.value = viewModel.empty
            findNavController().navigateUp()
        }

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (!isEmpty(binding.content.text.toString()) && !editing) {
                dao.saveDraft(binding.content.text.toString())
            }
            editing = false
            findNavController().navigateUp()
        }

        callback.isEnabled
        return binding.root
    }
}

