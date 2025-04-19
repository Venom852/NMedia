package ru.netology.nmedia.activity

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

object NewPostContract : ActivityResultContract<Intent, String?>() {
    override fun createIntent(context: Context, input: Intent) = input

    override fun parseResult(resultCode: Int, intent: Intent?) =
        intent?.getStringExtra(Intent.EXTRA_TEXT)
}