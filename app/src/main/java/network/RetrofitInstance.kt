package network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import network.RetrofitInstance
import network.MarvelApiService
import network.MarvelResponse

object RetrofitInstance {
    private const val BASE_URL = "https://gateway.marvel.com/"

    val api: MarvelApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MarvelApiService::class.java)
    }
}
