package com.makinul.background.remover.di

import com.makinul.background.remover.data.repository.FirebaseRepository
import com.makinul.background.remover.data.repository.FirebaseRepositoryImpl
import com.makinul.background.remover.data.repository.SplashRepo
import com.makinul.background.remover.data.repository.SplashRepoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@InstallIn(ViewModelComponent::class)
@Module
abstract class RepositoryModule {

    @Binds
    abstract fun bindSplashRepo(
        impl: SplashRepoImpl
    ): SplashRepo

    @Binds
    abstract fun bindFirebaseRepository(
        impl: FirebaseRepositoryImpl
    ): FirebaseRepository
}