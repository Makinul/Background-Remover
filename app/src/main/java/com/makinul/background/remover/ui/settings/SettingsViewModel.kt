package com.makinul.background.remover.ui.settings

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makinul.background.remover.R
import com.makinul.background.remover.data.Event
import com.makinul.background.remover.data.Resource
import com.makinul.background.remover.data.model.firebase.Support
import com.makinul.background.remover.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val firebaseRepo: FirebaseRepository
) : ViewModel() {

    private val _saveSupport by lazy { MutableLiveData<Event<Resource<Boolean>>>() }
    val saveSupport: LiveData<Event<Resource<Boolean>>> = _saveSupport

    fun saveSupport(support: Support) = viewModelScope.launch(Dispatchers.IO) {
        _saveSupport.postValue(Event(Resource.loading()))

        if (support.name.isEmpty()) {
            _saveSupport.postValue(Event(Resource.error(messageResId = R.string.please_write_your_name)))
            return@launch
        }
        if (support.email.isEmpty()) {
            _saveSupport.postValue(Event(Resource.error(messageResId = R.string.please_write_your_email_address)))
            return@launch
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(support.email).matches()) {
            _saveSupport.postValue(Event(Resource.error(messageResId = R.string.please_write_valid_email_address)))
            return@launch
        }
        if (support.message.isEmpty()) {
            _saveSupport.postValue(Event(Resource.error(messageResId = R.string.please_write_your_message)))
            return@launch
        }

        firebaseRepo.saveSupport(support)
    }
}