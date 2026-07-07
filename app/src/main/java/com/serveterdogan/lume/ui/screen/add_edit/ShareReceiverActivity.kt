package com.serveterdogan.lume.ui.screen.add_edit

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity


@RequiresApi(Build.VERSION_CODES.O)
class ShareReceiverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleSharedImage(intent)
        finish() // Hemen kapat — kullanıcı hiçbir şey görmez
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSharedImage(intent)
        finish()
    }

    private fun handleSharedImage(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        if (!intent.type.orEmpty().startsWith("image/")) return

        val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }

        if (uri != null) {
            // URI'ye okuma izni ver (share sheet'ten gelince gerekli)
            grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            AutoSaveMemoryService.start(this, uri)
        }
    }
}
