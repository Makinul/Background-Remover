package com.makinul.background.remover.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.makinul.background.remover.data.model.User
import com.makinul.background.remover.utils.AppConstants
import com.makinul.background.remover.utils.DeviceUtils
import com.makinul.background.remover.utils.firebase.FirebaseConstants.KEY_USERS
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SplashRepoImpl @Inject constructor(
    private val database: FirebaseDatabase,
    private val deviceUtils: DeviceUtils,
) : BaseRepo(), SplashRepo {

    override suspend fun createAnonymousAccount(): Boolean {
        val firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth.currentUser == null)
            firebaseAuth.signInAnonymously().await()
        else
            delay(2000)

        showLog("currentUser ${firebaseAuth.currentUser}")

        return true
    }

    override suspend fun saveUser(user: User): Boolean {
        val reference = database.getReference(KEY_USERS).child(user.userId)
        reference.setValue(user).await()

        return true
    }

    override suspend fun getUser(userId: String): Flow<User?> {
        val reference = database.getReference(KEY_USERS).child(userId)
        return getItem(reference)
    }
//
//    override suspend fun categories(): Flow<List<Category>?> {
//        return getItems(database.getReference(KEY_CATEGORIES))
//    }
//
//    override suspend fun questions(): Flow<List<Question>?> {
//        return getItems(database.getReference(KEY_QUESTIONS))
//    }
//
//    override suspend fun warmUps(): Flow<List<WarmUp>?> {
//        return getItems(database.getReference(KEY_WARM_UPS))
//    }
//
//    override suspend fun getExerciseDurations(): Flow<Map<String, ExerciseDuration?>?> {
//        val reference = database.getReference(KEY_EXERCISE_DURATIONS)
//        return getMaps<ExerciseDuration>(reference)
//    }

    private fun showLog(message: String = "Test Message") {
        AppConstants.showLog(TAG, message)
    }

    companion object {
        private const val TAG = "SplashRepoImpl"
    }
}