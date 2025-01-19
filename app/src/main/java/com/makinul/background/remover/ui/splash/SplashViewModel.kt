/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.makinul.background.remover.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.makinul.background.remover.utils.AppConstants
import com.makinul.background.remover.utils.PreferenceHelper
import com.makinul.background.remover.data.Event
import com.makinul.background.remover.data.Resource
import com.makinul.background.remover.data.model.Device
import com.makinul.background.remover.data.model.User
import com.makinul.background.remover.data.repository.SplashRepo
import com.makinul.background.remover.utils.DeviceUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 *  This ViewModel is used to store pose landmark helper settings
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferenceHelper: PreferenceHelper,
    private val deviceUtils: DeviceUtils,
    private val firebaseAuth: FirebaseAuth,
    private val repo: SplashRepo
) : ViewModel() {

    private var _delay = MutableLiveData<Event<Boolean>>()
    val delay: LiveData<Event<Boolean>>
        get() = _delay

    private val _anonymousAuth by lazy { MutableLiveData<Event<Resource<Boolean>>>() }
    val anonymousAuth: LiveData<Event<Resource<Boolean>>> = _anonymousAuth

    fun createAnonymousAccount() = viewModelScope.launch(Dispatchers.IO) {
        _anonymousAuth.postValue(Event(Resource.loading()))

        if (firebaseAuth.currentUser == null) {
            val data = repo.createAnonymousAccount()
            if (data) {
                showLog()
            }
        } else {
            delay(2000)
        }

        firebaseAuth.uid?.let { userId ->
            repo.getUser(userId).collect { remoteUser ->
                val device = Device(
                    deviceName = deviceUtils.deviceName,
                    deviceId = deviceUtils.deviceId
                )
                val finalUser = remoteUser?.let {
                    val devices: HashMap<String, Device> = HashMap(it.devices)
                    devices[device.deviceId] = device
                    it.copy(devices = devices)
                } ?: run {
                    val devices: HashMap<String, Device> = HashMap()
                    devices[device.deviceId] = device
                    User(userId, devices = devices)
                }
                repo.saveUser(finalUser)
                preferenceHelper.saveUser(finalUser)
                _anonymousAuth.postValue(Event(Resource.success(true)))
            }
        }

//        val user = User(firebaseAuth.uid ?: "null")
//        repo.saveUser(user)
//        _anonymousAuth.postValue(Event(Resource.success(true)))
    }

//    fun prepareUserBasicData() {
//        preferenceHelper.getUser()
//    }
//
//    private var _allTenants = MutableLiveData<Event<Resource<List<Tenant>>>>()
//    val allTenants: LiveData<Event<Resource<List<Tenant>>>>
//        get() = _allTenants
//
//    fun getAllTenant() = viewModelScope.launch(Dispatchers.IO) {
//        _allTenants.postValue(Event(Resource.loading()))
//        val items: ArrayList<Tenant> = ArrayList(tenantDao.getAll())
//        if (items.isNotEmpty()) {
//            _allTenants.postValue(Event(Resource.success(items)))
//        } else {
//            getAllRemoteTenant()
//        }
//    }
//
//    fun getAllRemoteTenant() = viewModelScope.launch(Dispatchers.IO) {
//        _allTenants.postValue(Event(Resource.loading()))
//        val items: ArrayList<Tenant> = ArrayList(tenantDao.getAll())
//        if (items.isNotEmpty()) {
//            _allTenants.postValue(Event(Resource.success(items)))
//            return@launch
//        }
//        val req = TenantReq()
//        val data = basicRepository.getAllTenants(req)
//        if (data is Data.Success) {
//            val tenantList = data.data
//            tenantDao.insertAll(tenantList)
//            if (items.isEmpty()) {
//                val tmpList: ArrayList<Tenant> = ArrayList()
//                tenantList.forEach {
//                    if (it.tenantName.equals(TENANT_VATB, true) ||
//                        it.tenantName.equals(AppConstants.TENANT_EMMA, true)
//                    ) {
//                        tmpList.add(it)
//                    }
//                }
//                _allTenants.postValue(Event(Resource.success(tmpList)))
//            }
//        } else if (data is Data.Error) {
//            _allTenants.postValue(Event(Resource.error(data.message)))
//        }
//    }
//
//    private var _setTenant = MutableLiveData<Event<Resource<Tenant>>>()
//    val setTenant: LiveData<Event<Resource<Tenant>>>
//        get() = _setTenant
//
//    fun setTenant(selectedTenant: Tenant) = viewModelScope.launch(Dispatchers.IO) {
//        _setTenant.postValue(Event(Resource.loading()))
//        if (preferenceHelper.selectedTenantId == selectedTenant.id) {
//            _setTenant.postValue(Event(Resource.success(selectedTenant)))
//            return@launch
//        }
//
//        preferenceHelper.questionDataFirstTimeLoaded = false
//        botQuestionDao.deleteAll()
//        questionOptionDao.deleteAll()
//        questionAnswerDao.deleteAll()
//
//        preferenceHelper.setTenantName(
//            tenantId = selectedTenant.id,
//            tenantName = selectedTenant.tenantName ?: TENANT_VATB
//        )
//        _setTenant.postValue(Event(Resource.success(selectedTenant)))
//    }
//
//    private var _loginState = MutableLiveData<Event<Resource<Boolean>>>()
//    val loginState: LiveData<Event<Resource<Boolean>>>
//        get() = _loginState
//
//    fun doLogin(
//        selectedTenant: Tenant,
//        emailAddress: String,
//        password: String,
//    ) = viewModelScope.launch(Dispatchers.IO) {
//        _loginState.postValue(Event(Resource.loading()))
//        if (selectedTenant.id < 1) {
//            _loginState.postValue(Event(Resource.error(messageResId = R.string.please_select_tenant)))
//            return@launch
//        }
//        if (emailAddress.isEmpty()) {
//            _loginState.postValue(Event(Resource.error(messageResId = R.string.please_insert_email_address)))
//            return@launch
//        }
//        if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
//            _loginState.postValue(Event(Resource.error(messageResId = R.string.please_insert_valid_email_address)))
//            return@launch
//        }
//        if (password.isEmpty()) {
//            _loginState.postValue(Event(Resource.error(messageResId = R.string.please_insert_password)))
//            return@launch
//        }
//        if (password.length < 6) {
//            _loginState.postValue(Event(Resource.error(messageResId = R.string.password_length_must_greater_or_equal)))
//            return@launch
//        }
//        val req = LoginReq(
//            emailAddress = emailAddress,
//            password = password,
//            tenant = selectedTenant.tenantName ?: TENANT_VATB
//        )
//
//        when (val response = authRepository.emailPasswordLogin(req)) {
//            is Data.Success -> {
//                showLog("API Succeed")
//                val loginResponse = response.data
//
//                if (!loginResponse.dateOfBirth.isNullOrEmpty()) {
//                    val calendar =
//                        DateUtils.getCalendar(loginResponse.dateOfBirth!!, DateUtils.serverFormat)
//                    val year = calendar.get(Calendar.YEAR)
//
//                    if (year < 1970) {
//                        loginResponse.dateOfBirth = null
//                    }
//                } else {
//                    loginResponse.dateOfBirth = null
//                }
//
//                preferenceHelper.saveLogin(loginResponse)
//                _loginState.postValue(Event(Resource.success(data = true)))
//            }
//
//            is Data.Error -> {
//                _loginState.postValue(Event(Resource.error(message = response.message)))
//            }
//
//            else -> {
//                _loginState.postValue(Event(Resource.error(messageResId = R.string.unknown_error)))
//            }
//        }
//    }
//
//    private var _verificationState = MutableLiveData<Event<Resource<VerifyResponse>>>()
//    val verificationState: LiveData<Event<Resource<VerifyResponse>>>
//        get() = _verificationState
//
//    fun doVerification(
//        selectedTenant: Tenant,
//        emailAddress: String
//    ) = viewModelScope.launch(Dispatchers.IO) {
//        _verificationState.postValue(Event(Resource.loading()))
//        if (selectedTenant.id < 1) {
//            _verificationState.postValue(Event(Resource.error(messageResId = R.string.please_select_tenant)))
//            return@launch
//        }
//        if (emailAddress.isEmpty()) {
//            _verificationState.postValue(Event(Resource.error(messageResId = R.string.please_insert_email_address)))
//            return@launch
//        }
//        if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
//            _verificationState.postValue(Event(Resource.error(messageResId = R.string.please_insert_valid_email_address)))
//            return@launch
//        }
//        val verifyReq = VerifyReq(
//            emailAddress = emailAddress,
//            tenant = selectedTenant.tenantName ?: TENANT_VATB
//        )
//        when (val response = authRepository.verifyEmailAddress(verifyReq)) {
//            is Data.Success -> {
//                showLog("API Succeed")
//                val verifyResponse = response.data
//                preferenceHelper.saveUserInvitation(verifyResponse)
//                when (verifyResponse.verifyStatus) {
//                    AppConstants.EMAIL_VERIFICATION_INVITED -> _verificationState.postValue(
//                        Event(
//                            Resource.success(
//                                data = response.data,
//                                messageResId = R.string.you_have_been_invited
//                            )
//                        )
//                    )
//
//                    AppConstants.EMAIL_VERIFICATION_NOT_FOUND -> _verificationState.postValue(
//                        Event(
//                            Resource.error(messageResId = R.string.public_registration_is_disabled_for_now)
//                        )
//                    )
//
//                    AppConstants.EMAIL_VERIFICATION_ALREADY_EXISTS -> _verificationState.postValue(
//                        Event(Resource.error(messageResId = R.string.your_are_registered_user))
//                    )
//
//                    else -> _verificationState.postValue(Event(Resource.error(messageResId = R.string.unknown_error)))
//                }
//            }
//
//            is Data.Error -> {
//                showLog(response.message)
//                _verificationState.postValue(Event(Resource.error(message = response.message)))
//            }
//
//            else -> {
//                _verificationState.postValue(Event(Resource.error(messageResId = R.string.unknown_error)))
//            }
//        }
//    }
//
//    private var _registrationState = MutableLiveData<Event<Resource<Boolean>>>()
//    val registrationState: LiveData<Event<Resource<Boolean>>>
//        get() = _registrationState
//
//    fun doRegistration(
//        selectedTenant: Tenant,
//        emailAddress: String,
//        firstName: String,
//        lastName: String,
//        password: String,
//        confirmPassword: String
//    ) = viewModelScope.launch(Dispatchers.IO) {
//        _registrationState.postValue(Event(Resource.loading()))
//        if (selectedTenant.id < 1) {
//            _registrationState.postValue(Event(Resource.error(messageResId = R.string.please_select_tenant)))
//            return@launch
//        }
//        if (emailAddress.isEmpty()) {
//            _registrationState.postValue(Event(Resource.error(messageResId = R.string.please_insert_email_address)))
//            return@launch
//        }
//        if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
//            _registrationState.postValue(Event(Resource.error(messageResId = R.string.please_insert_valid_email_address)))
//            return@launch
//        }
//        if (firstName.isEmpty()) {
//            _registrationState.postValue(Event(Resource.error(messageResId = R.string.please_insert_first_name)))
//            return@launch
//        }
//        if (lastName.isEmpty()) {
//            _registrationState.postValue(Event(Resource.error(messageResId = R.string.please_insert_last_name)))
//            return@launch
//        }
//        if (password.isEmpty()) {
//            _registrationState.postValue(Event(Resource.error(messageResId = R.string.please_insert_password)))
//            return@launch
//        }
//        if (password.length < 8 || password.length > 20) {
//            _registrationState.postValue(Event(Resource.error(messageResId = R.string.password_length_at_least_8_and_at_most_20)))
//            return@launch
//        }
//        val pattern = Pattern.compile(AppConstants.PASSWORD_PATTERN)
//        val matcher = pattern.matcher(password)
//        if (!matcher.matches()) {
//            _registrationState.postValue(Event(Resource.error(messageResId = R.string.password_policy_registration)))
//            return@launch
//        }
//        if (confirmPassword.isEmpty()) {
//            _registrationState.postValue(Event(Resource.error(messageResId = R.string.please_insert_confirm_password)))
//            return@launch
//        }
//        if (password != confirmPassword) {
//            _registrationState.postValue(Event(Resource.error(messageResId = R.string.password_and_confirm_password_did_not_match)))
//            return@launch
//        }
//        val invitationId = preferenceHelper.invitationId
//        if (invitationId.isNullOrEmpty()) {
//            _registrationState.postValue(Event(Resource.error(messageResId = R.string.please_verify_your_email_address_again)))
//            return@launch
//        }
//        val req = RegistrationReq(
//            emailAddress = emailAddress,
//            firstName = firstName,
//            lastName = lastName,
//            password = password,
//            confirmPassword = password,
//            tenant = selectedTenant.tenantName ?: TENANT_VATB,
//            invId = invitationId
//        )
//
//        when (val response = authRepository.emailPasswordRegistration(req)) {
//            is Data.Success -> {
//                showLog("API Succeed")
//                val loginResponse = response.data
//                if (!loginResponse.dateOfBirth.isNullOrEmpty()) {
//                    val calendar =
//                        DateUtils.getCalendar(loginResponse.dateOfBirth!!, DateUtils.serverFormat)
//                    val year = calendar.get(Calendar.YEAR)
//
//                    if (year < 1970) {
//                        loginResponse.dateOfBirth = null
//                    }
//                } else {
//                    loginResponse.dateOfBirth = null
//                }
//                preferenceHelper.saveLogin(loginResponse)
//                _registrationState.postValue(Event(Resource.success(data = true)))
//            }
//
//            is Data.Error -> {
//                _registrationState.postValue(Event(Resource.error(message = response.message)))
//            }
//
//            else -> {
//                _registrationState.postValue(Event(Resource.error(messageResId = R.string.unknown_error)))
//            }
//        }
//    }
//
//    private var _bodyRegions = MutableLiveData<Event<Resource<Boolean>>>()
//    val bodyRegions: LiveData<Event<Resource<Boolean>>>
//        get() = _bodyRegions
//
//    fun getAllBodyRegion() = viewModelScope.launch(Dispatchers.IO) {
//        _bodyRegions.postValue(Event(Resource.loading()))
//
//        val localItems = bodyRegionDao.getAll()
//        if (localItems.isNotEmpty()) {
//            _bodyRegions.postValue(Event(Resource.success(true)))
//            return@launch
//        }
//
//        val response = basicRepository.getAllBodyRegions()
//        if (response is Data.Success) {
//            bodyRegionDao.insertAll(response.data)
//            _bodyRegions.postValue(Event(Resource.success(true)))
//        } else if (response is Data.Error) {
//            _bodyRegions.postValue(Event(Resource.error(response.message)))
//        }
//    }
//
//    private var _botQuestions = MutableLiveData<Event<Resource<Boolean>>>()
//    val botQuestions: LiveData<Event<Resource<Boolean>>>
//        get() = _botQuestions
//
//    var botQuestionState = Status.LOADING
//
//    fun getAllBotQuestion(tenantName: String?) = viewModelScope.launch(Dispatchers.IO) {
//        if (preferenceHelper.questionDataFirstTimeLoaded) {
//            botQuestionState = Status.SUCCESS
//            _botQuestions.postValue(Event(Resource.success(true)))
//            return@launch
//        }
//        botQuestionState = Status.LOADING
//        _botQuestions.postValue(Event(Resource.loading()))
//        val response = basicRepository.getAllBotQuestions()
//        if (response is Data.Success) {
//            showLog("API Succeed")
//            val bodyRegionIdVsQuestionIds: HashMap<Int, ArrayList<Int>> = HashMap()
//            for (item in response.data.bodyRegionsSync) {
//                bodyRegionIdVsQuestionIds[item.bodyRegionId] = ArrayList()
//            }
//            for (item in response.data.questionList) {
//                botQuestionDao.insert(item)
//
//                if (!item.bodyRegion.isNullOrEmpty()) {
//                    val bodyRegionIds = item.bodyRegion
//                    if (bodyRegionIds.contains(COMMA_SEPARATOR)) {
//                        val bodyRegionIdArray = bodyRegionIds.split(COMMA_SEPARATOR)
//                        for (bodyRegionId in bodyRegionIdArray) {
//                            if (bodyRegionId.isEmpty())
//                                continue
//                            try {
//                                val id = bodyRegionId.toInt()
//                                val existingQuestionIds: ArrayList<Int> =
//                                    bodyRegionIdVsQuestionIds[id] ?: ArrayList()
//                                existingQuestionIds.add(item.questionId)
//                                bodyRegionIdVsQuestionIds[id] = existingQuestionIds
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            }
//                        }
//                    } else {
//                        try {
//                            val id = bodyRegionIds.toInt()
//                            val existingQuestionIds: ArrayList<Int> =
//                                bodyRegionIdVsQuestionIds[id] ?: ArrayList()
//                            existingQuestionIds.add(item.questionId)
//                            bodyRegionIdVsQuestionIds[id] = existingQuestionIds
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    }
//                }
//                questionOptionDao.insertAll(item.questionOptionList)
//                questionAnswerDao.insertAll(item.questionAnswerList)
//            }
//            for (bodyRegion in response.data.bodyRegionsSync) {
//                bodyRegion.questionIds =
//                    bodyRegionIdVsQuestionIds[bodyRegion.bodyRegionId]?.joinToString(separator = COMMA_SEPARATOR)
//
//                val localBodyRegion = bodyRegionDao.getBodyRegion(bodyRegion.bodyRegionId)
//                if (localBodyRegion.isNotEmpty()) {
//                    bodyRegion.sortOrder = localBodyRegion[0].sortOrder
//                }
//
//                bodyRegionDao.insert(bodyRegion)
//                bodyRegionIdVsQuestionIds.remove(bodyRegion.bodyRegionId)
//            }
//            for (bodyRegionId in bodyRegionIdVsQuestionIds.keys) {
//                val bodyRegion = BodyRegion(
//                    bodyRegionId = bodyRegionId,
//                    bodyRegionName = null,
//                    syncDateUtc = null,
//                    sortOrder = null,
//                    questionIds = bodyRegionIdVsQuestionIds[bodyRegionId]?.joinToString(separator = COMMA_SEPARATOR)
//                )
//
//                val localBodyRegion = bodyRegionDao.getBodyRegion(bodyRegionId)
//                if (localBodyRegion.isNotEmpty()) {
//                    bodyRegion.bodyRegionName = localBodyRegion[0].bodyRegionName
//                    bodyRegion.syncDateUtc = localBodyRegion[0].syncDateUtc
//                    bodyRegion.sortOrder = localBodyRegion[0].sortOrder
//                }
//
//                bodyRegionDao.insert(bodyRegion)
//            }
//            showLog("Data inserted")
//            botQuestionState = Status.SUCCESS
//            preferenceHelper.questionDataFirstTimeLoaded = true
//            _botQuestions.postValue(Event(Resource.success(true)))
//        } else if (response is Data.Error) {
//            botQuestionState = Status.ERROR
//            _botQuestions.postValue(
//                Event(
//                    Resource.error(
//                        response.message
//                    )
//                )
//            )
//        }
//    }
//
//    private val _isInstructionVoiceActive by lazy { MutableLiveData<Event<Boolean>>() }
//    val isInstructionVoiceActive: LiveData<Event<Boolean>> = _isInstructionVoiceActive
//
//    fun changeInstructionVoice() = viewModelScope.launch(Dispatchers.IO) {
//        preferenceHelper.isInstructionVoiceActive = !preferenceHelper.isInstructionVoiceActive
//        _isInstructionVoiceActive.postValue(Event(preferenceHelper.isInstructionVoiceActive))
//    }
//
//    fun currentInstructionVoice() = viewModelScope.launch(Dispatchers.IO) {
//        _isInstructionVoiceActive.postValue(Event(preferenceHelper.isInstructionVoiceActive))
//    }

    private fun showLog(message: String = "Test Message") {
        AppConstants.showLog(TAG, message)
    }

    companion object {
        private const val TAG = "SplashViewModel"
    }
}