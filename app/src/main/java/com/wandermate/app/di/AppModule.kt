package com.wandermate.app.di
import android.content.Context
import androidx.room.Room
import com.wandermate.app.data.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun database(@ApplicationContext context: Context): TravelDatabase = Room.databaseBuilder(context, TravelDatabase::class.java, "wandermate.db").build()
    @Provides fun dao(database: TravelDatabase): TravelDao = database.dao()
    @Provides @Singleton fun client(): OkHttpClient = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).callTimeout(20, TimeUnit.SECONDS).build()
}
