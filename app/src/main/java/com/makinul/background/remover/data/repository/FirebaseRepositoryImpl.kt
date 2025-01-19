package com.makinul.background.remover.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.makinul.background.remover.data.model.firebase.Support
import com.makinul.background.remover.utils.AppConstants
import com.makinul.background.remover.utils.DeviceUtils
import com.makinul.background.remover.utils.PreferenceHelper
import com.makinul.background.remover.utils.firebase.FirebaseConstants.KEY_SUPPORTS
import com.makinul.background.remover.utils.DateUtils
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(
    private val preferenceHelper: PreferenceHelper,
    private val deviceUtils: DeviceUtils,
    private val database: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth
) : BaseRepo(), FirebaseRepository {

    override suspend fun saveSupport(support: Support): Boolean {
        val reference = database.getReference().child(KEY_SUPPORTS)

        val emailPath = if (support.email.contains(".")) {
            support.email.replace(".", "_")
        } else {
            support.email
        }

        val key = reference.child(emailPath).push().key
        key?.let {
            support.id = key
            support.created = System.currentTimeMillis()

            support.timeZone = DateUtils.getTimeZone()
            support.deviceName = deviceUtils.deviceName
//            support.deviceId = deviceUtils.deviceId

            reference.child(emailPath).child("$key").setValue(support).await()
            return true
        } ?: run {
            return false
        }
    }

    private fun showLog(message: String = "Test Message") {
        AppConstants.showLog(TAG, message)
    }

    companion object {
        const val TAG = "FirebaseRepository"
    }
}
