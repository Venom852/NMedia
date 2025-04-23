package ru.netology.nmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.viewmodel.PostViewModel

class NewPostFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(layoutInflater, container, false)
        val viewModel: PostViewModel by activityViewModels()

//        intent?.let {
//            val text = it.getStringExtra(Intent.EXTRA_TEXT)
//            if (text == "newPost") {
//                binding.group.visibility = View.GONE
//            } else {
//                binding.content.setText(text)
//                binding.cancelEdit.text = binding.content.text
//            }
//        }

        binding.edit.requestFocus()

        binding.ok.setOnClickListener{
            if (!binding.content.text.isNullOrBlank()) {
                val content = binding.content.text.toString()
                viewModel.saveContent(content)
                findNavController().navigateUp()
//                if (content == null) {
//                    viewModel.edited.value = viewModel.empty
//                } else {
//                    viewModel.saveContent(content)
//                }
            }
            viewModel.edited.value = viewModel.empty
            findNavController().navigateUp()
        }

        binding.cancel.setOnClickListener {
            findNavController().navigateUp()
        }
        return binding.root
    }
}

