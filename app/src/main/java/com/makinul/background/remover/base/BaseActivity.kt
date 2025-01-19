package com.makinul.background.remover.base

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.http.SslError
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.makinul.background.remover.R
import com.makinul.background.remover.databinding.DialogBrowserBinding
import com.makinul.background.remover.utils.AppConstants.showLog
import com.makinul.background.remover.utils.Extensions.invisible
import com.makinul.background.remover.utils.Extensions.visible

open class BaseActivity : AppCompatActivity() {

    private fun showToast(message: String = getString(R.string.test_message)) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun showToast(@StringRes message: Int) {
        showToast(getString(message))
    }

    companion object {
        private const val TAG = "BaseActivity"
    }

    private var browserDialog: Dialog? = null

    fun showBrowserDialog(context: Context, url: String) {
        if (browserDialog != null) {
            browserDialog?.dismiss()
            browserDialog = null
        }
        browserDialog = Dialog(context)

        browserDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dBinding = DialogBrowserBinding.inflate(
            LayoutInflater.from(
                context
            )
        )
        browserDialog?.setContentView(dBinding.root)
        browserDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        browserDialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        dBinding.webView.setInitialScale(1)
        dBinding.webView.setBackgroundColor(Color.argb(1, 255, 255, 255))
        dBinding.webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        dBinding.webView.settings.javaScriptEnabled = true
        dBinding.webView.settings.loadWithOverviewMode = true
        dBinding.webView.settings.useWideViewPort = true
        dBinding.webView.settings.domStorageEnabled = true
        dBinding.webView.settings.setSupportZoom(true)

        dBinding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                showLog(TAG, "onProgressChanged $newProgress")
                if (browserDialog != null && browserDialog!!.isShowing) {
                    if (newProgress <= 90) {
                        dBinding.progressBar.visible()
                    } else {
                        dBinding.progressBar.invisible()
                    }
                }
                super.onProgressChanged(view, newProgress)
            }
        }

        dBinding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return true
            }

            @Deprecated("Deprecated in Java")
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                showLog(TAG, "onReceivedError $description")
                super.onReceivedError(view, errorCode, description, failingUrl)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                showLog(TAG, "WebResourceError $error")
                super.onReceivedError(view, request, error)
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                processSslError(handler)
            }
        }

        dBinding.webView.loadUrl(url)

        dBinding.closeButton.setOnClickListener {
            browserDialog?.dismiss()
        }

        browserDialog?.setCanceledOnTouchOutside(false)
        browserDialog?.setCancelable(false)
        browserDialog?.show()
    }

    private fun processSslError(handler: SslErrorHandler?) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(R.string.alert)
            .setMessage(R.string.notification_error_ssl_cert_invalid)
            .setPositiveButton(R.string.proceed) { _, _ ->
                handler?.proceed()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                handler?.cancel()
            }

        alertDialog.show()
    }
}