package com.example.mynotes.di

import android.content.Context
import com.example.mynotes.data.ai.LocalAiEngine
import com.example.mynotes.data.audio.AudioPlayerManager
import com.example.mynotes.data.audio.AudioRecorderManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideLocalAiEngine(
        @ApplicationContext context: Context
    ): LocalAiEngine {
        return LocalAiEngine(context)
    }

    @Provides
    @Singleton
    fun provideAudioRecorderManager(
        @ApplicationContext context: Context
    ): AudioRecorderManager {
        return AudioRecorderManager(context)
    }

    @Provides
    @Singleton
    fun provideAudioPlayerManager(
        @ApplicationContext context: Context
    ): AudioPlayerManager {
        return AudioPlayerManager(context)
    }
}
