package com.makinul.background.remover.data.repository

import com.makinul.background.remover.data.model.firebase.Support

interface FirebaseRepository {

    suspend fun saveSupport(support: Support): Boolean
}
