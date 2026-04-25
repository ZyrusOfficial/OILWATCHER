package com.oilwatcher.monitor.di

import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI module providing app-wide singletons.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * ML Kit Text Recognizer — on-device OCR.
     * The model (~4MB) is bundled in the APK. No cloud calls needed.
     */
    @Provides
    @Singleton
    fun provideTextRecognizer(): TextRecognizer {
        return TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }
}
