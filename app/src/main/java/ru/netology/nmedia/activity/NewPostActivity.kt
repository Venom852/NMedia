package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.databinding.ActivityNewPostBinding

class NewPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent?.let {
            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text == "newPost") {
                binding.group.visibility = View.GONE
            } else {
                binding.content.setText(text)
                binding.cancelEdit.text = binding.content.text
            }
        }

        binding.ok.setOnClickListener{
            val text = binding.content.text.toString()
            if (text.isBlank()) {
                setResult(RESULT_CANCELED)
            } else {
                setResult(RESULT_OK, Intent().apply { putExtra(Intent.EXTRA_TEXT, text) })
            }
            finish()
        }

        binding.cancel.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
    }
}

