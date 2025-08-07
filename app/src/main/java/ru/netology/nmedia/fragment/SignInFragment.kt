package ru.netology.nmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.ErrorCode400And500Binding
import ru.netology.nmedia.databinding.FragmentSignInBinding
import ru.netology.nmedia.util.SwipeDirection
import ru.netology.nmedia.util.detectSwipe
import ru.netology.nmedia.viewmodel.SignInViewModel
import kotlin.getValue

class SignInFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignInBinding.inflate(layoutInflater, container, false)
        val bindingErrorCode400And500 =
            ErrorCode400And500Binding.inflate(layoutInflater, container, false)

        val dialog = BottomSheetDialog(requireContext())
        val viewModel: SignInViewModel by viewModels()

        with(binding) {
            login.requestFocus()

            signIn.setOnClickListener {
                if (login.text.toString().isEmpty()) {
                    Toast.makeText(requireContext(), R.string.empty_login, Toast.LENGTH_SHORT).show()
                }

                if (password.text.toString().isEmpty()) {
                    Toast.makeText(requireContext(), R.string.empty_password, Toast.LENGTH_SHORT).show()
                }

                if (!login.text.toString().isEmpty() && !password.text.toString().isEmpty()) {
                    viewModel.signIn(login.text.toString(), password.text.toString())
                }
            }

            viewModel.authState.observe(viewLifecycleOwner) {
                if (it.token != null) {
                    AppAuth.getInstance().setAuth(it.id, it.token)
                    findNavController().navigateUp()
                }
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.errorCode300) {
                binding.signInFragment.isVisible = false
                binding.errorCode300.error300Group.isVisible = true
            } else {
                binding.signInFragment.isVisible = true
                binding.errorCode300.error300Group.isVisible = false
            }

            if (state.error) {
                Snackbar.make(binding.root, R.string.something_went_wrong, Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        viewModel.bottomSheet.observe(viewLifecycleOwner) {
            dialog.setCancelable(false)
            dialog.setContentView(bindingErrorCode400And500.root)
            dialog.show()
        }

        binding.errorCode300.buttonError.setOnClickListener {
            binding.signInFragment.isVisible = true
            binding.errorCode300.error300Group.isVisible = false
        }

        bindingErrorCode400And500.errorCode400And500.detectSwipe { event ->
            val text = when (event) {
                SwipeDirection.Down -> "onSwipeDown"
                SwipeDirection.Left -> "onSwipeLeft"
                SwipeDirection.Right -> "onSwipeRight"
                SwipeDirection.Up -> "onSwipeUp"
            }

            if (text == "onSwipeDown") {
                dialog.dismiss()
                Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}