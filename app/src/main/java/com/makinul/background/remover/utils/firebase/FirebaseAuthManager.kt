package com.makinul.background.remover.utils.firebase

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.makinul.background.remover.data.model.User

class FirebaseAuthManager(private val activity: Activity, private val listener: AuthListener) {

    interface AuthListener {
        fun onLoading(loadingStopped: Boolean)
        fun onFinished(success: Boolean, message: String?, user: User? = null)
    }

    private lateinit var phoneNumber: String

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val timeout = 60000L

//    private fun requestForPhoneAuth(phoneNumber: String, needToReset: Boolean = false) {
//        if (needToReset) {
//            storedVerificationId = null
//            resendToken = null
//        }
//        this.phoneNumber = phoneNumber
//        val options = PhoneAuthOptions.newBuilder(auth)
//            .setPhoneNumber(phoneNumber)                // Phone number to verify
//            .setTimeout(timeout, TimeUnit.MILLISECONDS) // Timeout and unit
//            .setActivity(activity)                      // Activity (for callback binding)
//            .setCallbacks(phoneVerificationCallback)                    // OnVerificationStateChangedCallbacks
//        if (resendToken != null) {
//            options.setForceResendingToken(resendToken!!)
//        }
//
//        listener.onLoading(false)
//        PhoneAuthProvider.verifyPhoneNumber(options.build())
//    }
//
//    private val phoneVerificationCallback =
//        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//
//            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//                // This callback will be invoked in two situations:
//                // 1 - Instant verification. In some cases the phone number can be instantly
//                //     verified without needing to send or enter a verification code.
//                // 2 - Auto-retrieval. On some devices Google Play services can automatically
//                //     detect the incoming verification SMS and perform verification without
//                //     user action.
//                signInWithCredential(credential)
//            }
//
//            override fun onVerificationFailed(e: FirebaseException) {
//                // This callback is invoked in an invalid request for verification is made,
//                // for instance if the the phone number format is not valid.
//                Log.v(TAG, "onVerificationFailed", e)
//                val message = when (e) {
//                    is FirebaseAuthInvalidCredentialsException -> {
//                        // Invalid request
//                        activity.getString(R.string.invalid_phone_number)
//                    }
//                    is FirebaseTooManyRequestsException -> {
//                        // The SMS quota for the project has been exceeded
////                    getString(R.string.otp_limit_cross)
//                        e.message ?: activity.getString(R.string.otp_limit_cross)
//                    }
//                    else -> {
////                    getString(R.string.unknown_error)
//                        e.message ?: activity.getString(R.string.unknown_error)
//                    }
//                }
//                // Show a message and update the UI
//                listener.onFinished(false, message)
//            }
//
//            override fun onCodeSent(
//                verificationId: String,
//                token: PhoneAuthProvider.ForceResendingToken
//            ) {
//                // The SMS verification code has been sent to the provided phone number, we
//                // now need to ask the user to enter the code and then construct a credential
//                // by combining the code with a verification ID.
////            Log.d(TAG, "onCodeSent:$verificationId")
//                // Save verification ID and resending token so we can use them later
//                storedVerificationId = verificationId
//                resendToken = token
//
//                listener.onLoading(true)
//                showOtpDialog(phoneNumber)
//            }
//        }
//
//    private var storedVerificationId: String? = null
//    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
//
//    private fun showOtpDialog(phoneNumber: String) {
//        val dialog = Dialog(activity)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        val dBinding = DialogOtpBinding.inflate(
//            LayoutInflater.from(
//                activity
//            )
//        )
//        dialog.setContentView(dBinding.root)
//        val window = dialog.window
//        if (window != null) {
//            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            window.setLayout(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//            )
//        }
//        dialog.setCanceledOnTouchOutside(false)
//        dialog.show()
//
//        dBinding.code1.requestFocus()
//        dBinding.code1.addTextChangedListener(OtpTextWatcher(dBinding.code1, dBinding.code2))
//        dBinding.code2.addTextChangedListener(
//            OtpTextWatcher(
//                dBinding.code2,
//                dBinding.code3,
//                dBinding.code1
//            )
//        )
//        dBinding.code3.addTextChangedListener(
//            OtpTextWatcher(
//                dBinding.code3,
//                dBinding.code4,
//                dBinding.code2
//            )
//        )
//        dBinding.code4.addTextChangedListener(
//            OtpTextWatcher(
//                dBinding.code4,
//                dBinding.code5,
//                dBinding.code3
//            )
//        )
//        dBinding.code5.addTextChangedListener(
//            OtpTextWatcher(
//                dBinding.code5,
//                dBinding.code6,
//                dBinding.code4
//            )
//        )
//
//        val timer = object : CountDownTimer(timeout, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                val sec = millisUntilFinished / 1000
//                val countDownMessage =
//                    activity.getString(R.string.count_down_message, sec.toString())
//                dBinding.countdownTime.text = countDownMessage
//            }
//
//            override fun onFinish() {
//                dBinding.countdownTime.isEnabled = true
//                dBinding.countdownTime.text = activity.getString(R.string.count_down_retry)
//            }
//        }
//        timer.start()
//
//        dBinding.code6.addTextChangedListener(OtpTextWatcher(dBinding.code6, dBinding.code5))
//        dBinding.countdownTime.setOnClickListener {
//            requestForPhoneAuth(phoneNumber)
//            dialog.dismiss()
//        }
//
//        dBinding.otpSentMessage.text =
//            activity.getString(R.string.otp_sent_message_phone, phoneNumber)
//        dBinding.countdownTime.isEnabled = false
//
//        dBinding.confirm.setOnClickListener {
//            val code1 = dBinding.code1.text.toString()
//            val code2 = dBinding.code2.text.toString()
//            val code3 = dBinding.code3.text.toString()
//            val code4 = dBinding.code4.text.toString()
//            val code5 = dBinding.code5.text.toString()
//            val code6 = dBinding.code6.text.toString()
//
//            if (code1.isEmpty() || code2.isEmpty() ||
//                code3.isEmpty() || code4.isEmpty() ||
//                code5.isEmpty() || code6.isEmpty()
//            ) {
//                listener.onFinished(false, activity.getString(R.string.please_enter_otp))
//                return@setOnClickListener
//            }
//
//            val finalOtp = code1 + code2 + code3 + code4 + code5 + code6
//            val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, finalOtp)
//            signInWithCredential(credential)
//            dialog.dismiss()
//            listener.onLoading(false)
//        }
//        dialog.setOnDismissListener {
//            timer.cancel()
//        }
//    }
//
//    fun login(email: String, password: String, listener: AuthListener) {
//        listener.onLoading(false)
//        val credential = EmailAuthProvider.getCredential(email, password)
//        auth.signInWithCredential(credential)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    // Sign in success, update UI with the signed-in user's information
//                    listener.onFinished(true, null)
//                } else {
//                    // Sign in failed, display a message and update the UI
//                    val message = if (task.exception is FirebaseAuthInvalidCredentialsException) {
//                        task.exception?.message
//                            ?: activity.getString(R.string.firebase_invalid_credential)
//                    } else {
//                        task.exception?.message ?: activity.getString(R.string.unknown_error)
//                    }
//                    listener.onFinished(false, message)
//                }
//            }
//    }
//
//    fun registration(email: String, password: String, listener: AuthListener) {
//        listener.onLoading(false)
//        auth.createUserWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    // Sign in success, update UI with the signed-in user's information
//                    listener.onFinished(true, null)
//                } else {
//                    // Sign in failed, display a message and update the UI
//                    val message = if (task.exception is FirebaseAuthInvalidCredentialsException) {
//                        task.exception?.message
//                            ?: activity.getString(R.string.firebase_invalid_credential)
//                    } else {
//                        task.exception?.message ?: activity.getString(R.string.unknown_error)
//                    }
//                    listener.onFinished(false, message)
//                }
//            }
//    }
//
//    private fun signInWithCredential(credential: AuthCredential) {
//        listener.onLoading(false)
//        auth.signInWithCredential(credential)
//            .addOnCompleteListener(authCompletionListener)
//    }
//
//    private val authCompletionListener = OnCompleteListener<AuthResult> { task ->
//        if (task.isSuccessful) {
//            // Sign in success, update UI with the signed-in user's information
//            listener.onFinished(true, null)
//        } else {
//            // Sign in failed, display a message and update the UI
//            val message = if (task.exception is FirebaseAuthInvalidCredentialsException) {
//                task.exception?.message ?: activity.getString(R.string.firebase_invalid_credential)
//            } else {
//                task.exception?.message ?: activity.getString(R.string.unknown_error)
//            }
//            listener.onFinished(false, message)
//        }
//    }

    companion object {
        private const val TAG = "FirebaseAuthManager"
    }
}