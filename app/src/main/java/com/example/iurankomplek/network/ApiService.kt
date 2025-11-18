package com.example.iurankomplek.network
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.model.PemanfaatanResponse
import retrofit2.Call
import retrofit2.http.GET
interface ApiService {
    @GET("users")
    fun getUsers(): Call<UserResponse>
    
    @GET("pemanfaatan")
    fun getPemanfaatan(): Call<PemanfaatanResponse>
}