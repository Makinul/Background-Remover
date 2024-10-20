package com.makinul.background.remover.di

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import com.makinul.background.remover.utils.PreferenceHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {
    private val appPref = "bgRemoverPref"

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences(appPref, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providesSharedPreferencesEditor(
        pref: SharedPreferences
    ): Editor {
        return pref.edit()
    }

    @Provides
    @Singleton
    fun providesPreferenceHelper(
        pref: SharedPreferences,
        editor: Editor
    ): PreferenceHelper {
        return PreferenceHelper(pref, editor)
    }
}