package ru.netology.nmedia.fragment

import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.netology.nmedia.databinding.ErrorCode400And500Binding
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel
import kotlinx.coroutines.launch

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
        val bindingErrorCode400And500 =
            ErrorCode400And500Binding.inflate(layoutInflater, container, false)
        val dialog = BottomSheetDialog(requireContext())
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
                lifecycleScope.launch {
                    if (dao.getDraft() != null) {
                        binding.content.setText(dao.getDraft())
                        dao.removeDraft()
                    }
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
                AndroidUtils.hideKeyboard(requireView())
            }
            viewModel.edited.value = viewModel.empty
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.dataState.observe(viewLifecycleOwner) {
            if (it.errorCode300) {
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

        binding.cancel.setOnClickListener {
            viewModel.edited.value = viewModel.empty
            findNavController().navigateUp()
        }

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (!isEmpty(binding.content.text.toString()) && !editing) {
                lifecycleScope.launch {
                    dao.saveDraft(binding.content.text.toString())
                }
            }
            editing = false
            viewModel.edited.value = viewModel.empty
            findNavController().navigateUp()
        }

        callback.isEnabled
        return binding.root
    }
}

