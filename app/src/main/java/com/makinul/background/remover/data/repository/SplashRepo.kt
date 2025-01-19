package com.makinul.background.remover.data.repository

import com.makinul.background.remover.data.model.User
import kotlinx.coroutines.flow.Flow

interface SplashRepo {
    suspend fun createAnonymousAccount(): Boolean
    suspend fun saveUser(user: User): Boolean
    suspend fun getUser(userId: String): Flow<User?>
//    suspend fun user(userId: String): Flow<User?>
//    suspend fun categories(): Flow<List<Category>?>
//    suspend fun questions(): Flow<List<Question>?>
//    suspend fun warmUps(): Flow<List<WarmUp>?>
//    suspend fun getExerciseDurations(): Flow<Map<String, ExerciseDuration?>?>
}