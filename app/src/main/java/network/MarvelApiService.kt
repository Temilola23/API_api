package network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MarvelApiService {
    @GET("v1/public/characters")
    fun getCharacters(
        @Query("ts") timestamp: String,
        @Query("apikey") apiKey: String,
        @Query("hash") hash: String
    ): Call<MarvelResponse>
}

//data class MarvelResponse(val data: Data)
//
//data class Data(val results: List<Character>)
//
//data class Character(
//    val name: String,
//    val description: String,
//    val thumbnail: Thumbnail
//)
//
//data class Thumbnail(val path: String, val extension: String)