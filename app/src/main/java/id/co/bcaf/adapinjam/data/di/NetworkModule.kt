//package id.co.bcaf.adapinjam.data.di
//
//import android.content.Context
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.components.SingletonComponent
//import id.co.bcaf.adapinjam.data.network.LoginService
//import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import okhttp3.OkHttpClient
//import java.util.concurrent.TimeUnit
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object NetworkModule {
//
//    @Provides
//    @Singleton
//    fun provideBaseUrl() = "https://e4df-2001-448a-2061-c117-8991-2f52-df50-760.ngrok-free.app"
//
//    @Provides
//    @Singleton
//    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
//        val sessionManager = SharedPrefManager(context)
//
//        return OkHttpClient.Builder()
//            .addInterceptor { chain ->
//                val token = sessionManager.getToken()
//                val request = chain.request().newBuilder().apply {
//                    if (!token.isNullOrEmpty()) {
//                        addHeader("Authorization", "Bearer $token")
//                    }
//                }.build()
//                chain.proceed(request)
//            }
//            .connectTimeout(30, TimeUnit.SECONDS)
//            .readTimeout(30, TimeUnit.SECONDS)
//            .writeTimeout(30, TimeUnit.SECONDS)
//            .build()
//    }
//
//    @Provides
//    @Singleton
//    fun provideRetrofit(client: OkHttpClient, baseUrl: String): Retrofit =
//        Retrofit.Builder()
//            .baseUrl(baseUrl)
//            .client(client)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//    @Provides
//    @Singleton
//    fun provideRepaymentApiService(retrofit: Retrofit): LoginService =
//        retrofit.create(LoginService::class.java)
//}
