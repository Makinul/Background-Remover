package com.makinul.background.remover.utils

import android.content.SharedPreferences
import com.makinul.background.remover.data.model.User
import javax.inject.Inject

class PreferenceHelper @Inject constructor(
    private val pref: SharedPreferences,
    private val editor: SharedPreferences.Editor
) {

    fun logout() {
        editor.apply {
            clear()
        }.also {
            it.apply()
        }
        prepareUserLoginState()
    }

//    fun getUser(): User {
//        prepareUserLoginState()
//        return User(
//            userId = pref.getInt(AppConstants.KEY_USER_ID, -1),
//            userName = pref.getString(AppConstants.KEY_USER_NAME, "") ?: "",
//            email = pref.getString(AppConstants.KEY_USER_EMAIL, "") ?: "",
//            fullName = pref.getString(AppConstants.KEY_USER_FULL_NAME, null) ?: "",
//            firstName = pref.getString(AppConstants.KEY_USER_FIRST_NAME, null) ?: "",
//            lastName = pref.getString(AppConstants.KEY_USER_LAST_NAME, null) ?: "",
//            gender = pref.getString(AppConstants.KEY_USER_GENDER, null) ?: "",
//            token = pref.getString(AppConstants.KEY_USER_TOKEN, null) ?: "",
//            refreshToken = pref.getString(AppConstants.KEY_REFRESH_TOKEN, null) ?: "",
//            roleName = pref.getString(AppConstants.KEY_USER_ROLE_NAME, null) ?: "",
//            isReferred = pref.getBoolean(AppConstants.KEY_USER_IS_REFERRED, false),
//            hasDoctor = pref.getBoolean(AppConstants.KEY_USER_HAS_DOCTOR, false),
//            timeZone = pref.getString(AppConstants.KEY_USER_TIME_ZONE, null) ?: "",
//            height = pref.getFloat(AppConstants.KEY_USER_HEIGHT, 0f).toDouble(),
//            weight = pref.getFloat(AppConstants.KEY_USER_WEIGHT, 0f).toDouble(),
//            dateOfBirth = pref.getString(AppConstants.KEY_USER_DATE_OF_BIRTH, null),
//            age = pref.getInt(AppConstants.KEY_USER_AGE, 0),
//            dominantHand = pref.getString(AppConstants.KEY_USER_DOMINANT_HAND, null),
//            ethnicity = pref.getString(AppConstants.KEY_USER_ETHNIC_REGION, null),
//            occupation = pref.getString(AppConstants.KEY_USER_OCCUPATION, null),
//            isSuperAdmin = pref.getBoolean(AppConstants.KEY_USER_IS_SUPER_ADMIN, false)
//        )
//    }

    var cameraCompatibility: Int
        get() = pref.getInt(AppConstants.KEY_CAMERA_COMPATIBILITY, -100)
        set(value) = editor.putInt(AppConstants.KEY_CAMERA_COMPATIBILITY, value)
            .apply()

    var questionDataFirstTimeLoaded: Boolean
        get() = pref.getBoolean(AppConstants.KEY_QUESTION_DATA_FIRST_TIME_LOADED, false)
        set(value) = editor.putBoolean(AppConstants.KEY_QUESTION_DATA_FIRST_TIME_LOADED, value)
            .apply()

    var invitationId: String?
        get() = pref.getString(AppConstants.KEY_USER_INVITATION_ID, null)
        set(value) = editor.putString(AppConstants.KEY_USER_INVITATION_ID, value)
            .apply()

    var userToken: String?
        get() = pref.getString(AppConstants.KEY_USER_TOKEN, null)
        set(value) = editor.putString(AppConstants.KEY_USER_TOKEN, value)
            .apply()

    var firstName: String?
        get() = pref.getString(AppConstants.KEY_USER_FIRST_NAME, null)
        set(value) = editor.putString(AppConstants.KEY_USER_FIRST_NAME, value)
            .apply()

    var lastName: String?
        get() = pref.getString(AppConstants.KEY_USER_LAST_NAME, null)
        set(value) = editor.putString(AppConstants.KEY_USER_LAST_NAME, value)
            .apply()

    var fullName: String?
        get() = pref.getString(AppConstants.KEY_USER_FULL_NAME, null)
        set(value) = editor.putString(AppConstants.KEY_USER_FULL_NAME, value)
            .apply()

    var dob: String?
        get() = pref.getString(AppConstants.KEY_USER_DATE_OF_BIRTH, null)
        set(value) = editor.putString(AppConstants.KEY_USER_DATE_OF_BIRTH, value)
            .apply()

    var refreshToken: String?
        get() = pref.getString(AppConstants.KEY_REFRESH_TOKEN, null)
        set(value) = editor.putString(AppConstants.KEY_REFRESH_TOKEN, value)
            .apply()

    var userEmail: String?
        get() = pref.getString(AppConstants.KEY_USER_EMAIL, null)
        set(value) = editor.putString(AppConstants.KEY_USER_EMAIL, value)
            .apply()

    var isInstructionVoiceActive: Boolean
        get() = pref.getBoolean(AppConstants.KEY_IS_INSTRUCTION_VOICE_ACTIVE, true)
        set(value) = editor.putBoolean(AppConstants.KEY_IS_INSTRUCTION_VOICE_ACTIVE, value)
            .apply()

    var isDemographicDataPrepared: Boolean
        get() = pref.getBoolean(AppConstants.KEY_IS_DEMOGRAPHIC_DATA_PREPARED, false)
        set(value) = editor.putBoolean(AppConstants.KEY_IS_DEMOGRAPHIC_DATA_PREPARED, value)
            .apply()

    var height: Double
        get() = pref.getFloat(AppConstants.KEY_USER_HEIGHT, 0f).toDouble()
        set(value) = editor.putFloat(AppConstants.KEY_USER_HEIGHT, value.toFloat())
            .apply()

    var weight: Double
        get() = pref.getFloat(AppConstants.KEY_USER_WEIGHT, 0f).toDouble()
        set(value) = editor.putFloat(AppConstants.KEY_USER_WEIGHT, value.toFloat())
            .apply()

    var gender: String?
        get() = pref.getString(AppConstants.KEY_USER_GENDER, null)
        set(value) = editor.putString(AppConstants.KEY_USER_GENDER, value).apply()

    var ethnic: String?
        get() = pref.getString(AppConstants.KEY_USER_ETHNIC_REGION, null)
        set(value) = editor.putString(AppConstants.KEY_USER_ETHNIC_REGION, value).apply()

    var occupation: String?
        get() = pref.getString(AppConstants.KEY_USER_OCCUPATION, null)
        set(value) = editor.putString(AppConstants.KEY_USER_OCCUPATION, value).apply()

    var dominantHand: String?
        get() = pref.getString(AppConstants.KEY_USER_DOMINANT_HAND, null)
        set(value) = editor.putString(AppConstants.KEY_USER_DOMINANT_HAND, value).apply()

    private fun prepareUserLoginState() {
        AppConstants.isUserLoggedIn = pref.getBoolean(AppConstants.KEY_IS_USER_LOGGED_IN, false)
        AppConstants.userId = pref.getInt(AppConstants.KEY_USER_ID, -1)
        AppConstants.userEmail = pref.getString(AppConstants.KEY_USER_EMAIL, null)
        AppConstants.userFirstName = pref.getString(AppConstants.KEY_USER_FIRST_NAME, null)
        AppConstants.userLastName = pref.getString(AppConstants.KEY_USER_LAST_NAME, null)
        AppConstants.userFullName = pref.getString(AppConstants.KEY_USER_FULL_NAME, null)
        AppConstants.token = pref.getString(AppConstants.KEY_USER_TOKEN, null)
        AppConstants.selectedTenantId = pref.getInt(AppConstants.KEY_SELECTED_TENANT_ID, -1)
        AppConstants.selectedTenantName =
            pref.getString(AppConstants.KEY_SELECTED_TENANT_NAME, null)
    }

    var selectedTenantId: Int
        get() = pref.getInt(AppConstants.KEY_SELECTED_TENANT_ID, -1)
        set(value) = editor.putInt(AppConstants.KEY_SELECTED_TENANT_ID, value)
            .apply()

    var selectedTenantName: String
        get() = pref.getString(AppConstants.KEY_SELECTED_TENANT_NAME, null)
            ?: AppConstants.TENANT_VATB
        set(value) = editor.putString(AppConstants.KEY_SELECTED_TENANT_NAME, value)
            .apply()

    var patientId: String?
        get() = pref.getString(AppConstants.KEY_PATIENT_ID, null)
        set(value) = editor.putString(AppConstants.KEY_PATIENT_ID, value)
            .apply()

    fun setTenantName(tenantId: Int, tenantName: String) {
        editor.apply {
            putString(AppConstants.KEY_SELECTED_TENANT_NAME, tenantName)
            putInt(AppConstants.KEY_SELECTED_TENANT_ID, tenantId)
        }.also {
            it.apply()
        }
    }

    fun getDownloadedReportUrl(testId: String): String? {
        return pref.getString(testId, null)
    }

    fun setDownloadedReportUrl(testId: String, reportUrl: String) {
        editor.apply {
            putString(testId, reportUrl)
        }.also {
            it.apply()
        }
    }

//    fun saveUserInvitation(verifyResponse: VerifyResponse) {
//        verifyResponse.let {
//            editor.apply {
//                putString(AppConstants.KEY_USER_INVITATION_ID, it.invitationId)
//                putString(AppConstants.KEY_USER_INVITATION_EMAIL_ADDRESS, it.emailAddress)
//                putString(AppConstants.KEY_USER_INVITATION_FIRST_NAME, it.firstName)
//                putString(AppConstants.KEY_USER_INVITATION_LAST_NAME, it.lastName)
//                putInt(AppConstants.KEY_USER_INVITATION_VERIFY_STATUS, it.verifyStatus)
//                putString(AppConstants.KEY_USER_INVITATION_VERIFY_STATUS_TEXT, it.verifyStatusText)
//                putInt(AppConstants.KEY_USER_INVITATION_REFER_TYPE, it.referType)
//                putString(AppConstants.KEY_USER_INVITATION_REFER_TYPE_TEXT, it.referTypeText)
//                putString(AppConstants.KEY_USER_INVITATION_TENANT, it.tenant)
//            }.also {
//                it.apply()
//            }
//        }
//    }

//    fun getUserInvitation(): VerifyResponse {
//        return VerifyResponse(
//            invitationId = pref.getString(AppConstants.KEY_USER_INVITATION_ID, null) ?: "",
//            emailAddress = pref.getString(AppConstants.KEY_USER_INVITATION_EMAIL_ADDRESS, null)
//                ?: "",
//            firstName = pref.getString(AppConstants.KEY_USER_INVITATION_FIRST_NAME, null) ?: "",
//            lastName = pref.getString(AppConstants.KEY_USER_INVITATION_LAST_NAME, null) ?: "",
//            verifyStatus = pref.getInt(AppConstants.KEY_USER_INVITATION_VERIFY_STATUS, -1),
//            verifyStatusText = pref.getString(
//                AppConstants.KEY_USER_INVITATION_VERIFY_STATUS_TEXT,
//                null
//            ),
//            referType = pref.getInt(AppConstants.KEY_USER_INVITATION_REFER_TYPE, -1),
//            referTypeText = pref.getString(AppConstants.KEY_USER_INVITATION_REFER_TYPE_TEXT, null),
//            tenant = pref.getString(AppConstants.KEY_USER_INVITATION_TENANT, null) ?: "",
//        )
//    }

//    fun saveLogin(response: AuthResponse) {
//        response.let {
//            editor.apply {
//                putBoolean(AppConstants.KEY_IS_USER_LOGGED_IN, true)
//                putInt(AppConstants.KEY_USER_ID, it.userId)
//                putString(AppConstants.KEY_USER_EMAIL, it.email)
//                putString(AppConstants.KEY_USER_FIRST_NAME, it.firstName)
//                putString(AppConstants.KEY_USER_LAST_NAME, it.lastName)
//                putString(AppConstants.KEY_USER_FULL_NAME, it.fullName)
//                putString(AppConstants.KEY_USER_NAME, it.userName)
//                putString(AppConstants.KEY_USER_TOKEN, it.token)
//                putString(AppConstants.KEY_REFRESH_TOKEN, it.refreshToken)
//                putString(AppConstants.KEY_USER_ROLE_NAME, it.roleName)
//                putBoolean(AppConstants.KEY_USER_IS_REFERRED, it.isReferred)
//                putBoolean(AppConstants.KEY_USER_HAS_DOCTOR, it.hasDoctor)
//                putString(AppConstants.KEY_USER_GENDER, it.gender)
//                putString(AppConstants.KEY_USER_DOMINANT_HAND, it.dominantHand)
//                putBoolean(AppConstants.KEY_USER_IS_SUPER_ADMIN, it.isSuperAdmin)
//                putString(AppConstants.KEY_USER_TIME_ZONE, it.timeZone)
//                putFloat(AppConstants.KEY_USER_HEIGHT, it.height.toFloat())
//                putFloat(AppConstants.KEY_USER_WEIGHT, it.weight.toFloat())
//                putString(AppConstants.KEY_USER_DATE_OF_BIRTH, it.dateOfBirth)
//                putInt(AppConstants.KEY_USER_AGE, it.age)
//                putString(AppConstants.KEY_PATIENT_ID, it.patientId)
//            }.also {
//                it.apply()
//            }
//        }
//    }

    fun saveUser(user: User) {
        editor.apply {
            putBoolean(AppConstants.KEY_IS_USER_LOGGED_IN, true)
            putString(AppConstants.KEY_USER_ID, user.userId)
            putString(AppConstants.KEY_USER_EMAIL, user.email)
            putString(AppConstants.KEY_USER_FIRST_NAME, user.firstName)
            putString(AppConstants.KEY_USER_LAST_NAME, user.lastName)
//            putString(AppConstants.KEY_USER_FULL_NAME, user.fullName)
//            putString(AppConstants.KEY_USER_NAME, user.userName)
//            putString(AppConstants.KEY_USER_TOKEN, user.token)
//            putString(AppConstants.KEY_REFRESH_TOKEN, user.refreshToken)
//            putString(AppConstants.KEY_USER_ROLE_NAME, user.roleName)
//            putBoolean(AppConstants.KEY_USER_IS_REFERRED, user.isReferred)
//            putBoolean(AppConstants.KEY_USER_HAS_DOCTOR, user.hasDoctor)
            putString(AppConstants.KEY_USER_GENDER, user.gender)
//            putString(AppConstants.KEY_USER_DOMINANT_HAND, user.dominantHand)
//            putBoolean(AppConstants.KEY_USER_IS_SUPER_ADMIN, user.isSuperAdmin)
//            putString(AppConstants.KEY_USER_TIME_ZONE, user.timeZone)
//            putFloat(AppConstants.KEY_USER_HEIGHT, user.height.toFloat())
//            putFloat(AppConstants.KEY_USER_WEIGHT, user.weight.toFloat())
            putString(AppConstants.KEY_USER_DATE_OF_BIRTH, user.dateOfBirth)
//            putInt(AppConstants.KEY_USER_AGE, user.age)
//            putString(AppConstants.KEY_PATIENT_ID, user.patientId)
        }.also {
            it.apply()
        }
    }

    companion object {
        private const val TAG = "PreferenceHelper"
    }
}