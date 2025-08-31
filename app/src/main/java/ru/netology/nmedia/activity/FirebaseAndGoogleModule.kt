package ru.netology.nmedia.activity

import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object FirebaseAndGoogleModule {
    @Provides
    @Singleton
    fun bindFirebaseMessaging() : FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }

    @Provides
    @Singleton
    fun bindFirebaseInstallations() : FirebaseInstallations {
        return FirebaseInstallations.getInstance()
    }

    @Provides
    @Singleton
    fun bindGoogleApiAvailability() : GoogleApiAvailability {
        return GoogleApiAvailability.getInstance()
    }
}