package com.goldhardt.core.data.di

import com.goldhardt.core.data.datasource.CategoryRemoteDataSource
import com.goldhardt.core.data.datasource.FirestoreCategoryRemoteDataSource
import com.goldhardt.core.data.repository.CategoryRepository
import com.goldhardt.core.data.repository.FirestoreCategoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRemoteDataSource(
        impl: FirestoreCategoryRemoteDataSource
    ): CategoryRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: FirestoreCategoryRepository
    ): CategoryRepository
}

