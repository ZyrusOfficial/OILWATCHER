package com.oilwatcher.monitor.di

import com.oilwatcher.monitor.data.location.LocationClient
import com.oilwatcher.monitor.data.location.LocationClientImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    @Singleton
    abstract fun bindLocationClient(
        locationClientImpl: LocationClientImpl
    ): LocationClient
}
