package id.co.bcaf.adapinjam.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.co.bcaf.adapinjam.data.provider.FirebaseTokenProvider
import id.co.bcaf.adapinjam.data.provider.TokenProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class TokenModule {

    companion object {
        @Provides
        @Singleton
        @JvmStatic
        fun provideTokenProvider(): TokenProvider = FirebaseTokenProvider()
    }
}

