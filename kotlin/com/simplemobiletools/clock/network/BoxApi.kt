package com.simplemobiletools.clock.network
import com.simplemobiletools.clock.network.model.PillBoxModel
import retrofit2.Call
import retrofit2.http.GET

//const val API_ID = "3/movie/top_rated"
//const val BASE_URL = "https://api.themoviedb.org/"
//const val API_KEY="96624ea86553cd7a4caed4ecbdc35ec1"
interface BoxApi {
//    @Query("api_key")
@GET("api/latest?access_key=a09197b882e3c2d55792d966d90e004f&format=1")
fun getBooleanFromBox(): Call<PillBoxModel>


}

//http://data.fixer.io/api/latest?access_key=a09197b882e3c2d55792d966d90e004f&format=1
