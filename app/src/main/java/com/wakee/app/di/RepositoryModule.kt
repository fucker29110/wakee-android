package com.wakee.app.di

import com.wakee.app.data.remote.FirestoreService
import com.wakee.app.data.remote.StorageService
import com.wakee.app.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        firestoreService: FirestoreService,
        storageService: StorageService
    ): AuthRepository = AuthRepository(firestoreService, storageService)

    @Provides
    @Singleton
    fun provideAlarmRepository(
        firestoreService: FirestoreService,
        storageService: StorageService
    ): AlarmRepository = AlarmRepository(firestoreService, storageService)

    @Provides
    @Singleton
    fun provideUserRepository(
        firestoreService: FirestoreService,
        storageService: StorageService
    ): UserRepository = UserRepository(firestoreService, storageService)

    @Provides
    @Singleton
    fun provideChatRepository(
        firestoreService: FirestoreService
    ): ChatRepository = ChatRepository(firestoreService)

    @Provides
    @Singleton
    fun provideFriendRepository(
        firestoreService: FirestoreService
    ): FriendRepository = FriendRepository(firestoreService)

    @Provides
    @Singleton
    fun provideActivityRepository(
        firestoreService: FirestoreService
    ): ActivityRepository = ActivityRepository(firestoreService)

    @Provides
    @Singleton
    fun provideStoryRepository(
        firestoreService: FirestoreService,
        storageService: StorageService
    ): StoryRepository = StoryRepository(firestoreService, storageService)

    @Provides
    @Singleton
    fun provideNotificationRepository(
        firestoreService: FirestoreService
    ): NotificationRepository = NotificationRepository(firestoreService)
}
