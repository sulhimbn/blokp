package com.example.iurankomplek.network
import com.example.iurankomplek.model.ResponseUser
import retrofit2.Call
import retrofit2.http.GET
interface ApiService {
    @GET(".")
    fun getUsers(): Call<ResponseUser>
    @GET(".")
    fun getPemanfaatan(): Call<ResponseUser>
}