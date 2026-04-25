package com.oilwatcher.monitor.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.oilwatcher.monitor.data.repository.FirestoreStationRepository
import com.oilwatcher.monitor.domain.repository.StationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStationRepository(
        firestoreStationRepository: FirestoreStationRepository
    ): StationRepository

    @Binds
    @Singleton
    abstract fun bindContributionRepository(
        contributionRepositoryImpl: com.oilwatcher.monitor.data.repository.ContributionRepositoryImpl
    ): com.oilwatcher.monitor.domain.repository.ContributionRepository
}
