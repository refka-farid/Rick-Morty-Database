package com.shevaalex.android.rickmortydatabase.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.shevaalex.android.rickmortydatabase.source.remote.CharacterApi
import com.shevaalex.android.rickmortydatabase.source.remote.EpisodeApi
import com.shevaalex.android.rickmortydatabase.source.remote.LocationApi
import com.shevaalex.android.rickmortydatabase.utils.Constants.Companion.CONNECTION_TIMEOUT
import com.shevaalex.android.rickmortydatabase.utils.Constants.Companion.READ_TIMEOUT
import com.shevaalex.android.rickmortydatabase.utils.Constants.Companion.WRITE_TIMEOUT
import com.shevaalex.android.rickmortydatabase.utils.networking.*
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
object NetworkModule{

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build()
    }


    @Singleton
    @Provides
    fun provideRetrofitBuilder(okHttpClient: OkHttpClient): Retrofit.Builder{
        val baseUrl = when {
            Locale.getDefault().language.startsWith(LOCALE_PREFIX_RU) -> {
                BASE_URL_RU
            }
            Locale.getDefault().language.startsWith(LOCALE_PREFIX_UK) -> {
                BASE_URL_UK
            }
            else -> {
                BASE_URL_EN
            }
        }
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addCallAdapterFactory(ApiResultCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
    }

    @Singleton
    @Provides
    fun provideCharacterApiService(retrofitBuilder: Retrofit.Builder): CharacterApi {
        return retrofitBuilder
                .build()
                .create(CharacterApi::class.java)
    }

    @Singleton
    @Provides
    fun provideLocationApiService(retrofitBuilder: Retrofit.Builder): LocationApi {
        return retrofitBuilder
                .build()
                .create(LocationApi::class.java)
    }

    @Singleton
    @Provides
    fun provideEpisodeApiService(retrofitBuilder: Retrofit.Builder): EpisodeApi {
        return retrofitBuilder
                .build()
                .create(EpisodeApi::class.java)
    }

}