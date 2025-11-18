package com.example.iurankomplek.network
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.model.CommunityPost
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("users")
    fun getUsers(): Call<UserResponse>
    
    @GET("pemanfaatan")
    fun getPemanfaatan(): Call<PemanfaatanResponse>
    
    // Communication endpoints
    @GET("announcements")
    fun getAnnouncements(): Call<List<Announcement>>
    
    @GET("messages")
    fun getMessages(@Query("userId") userId: String): Call<List<Message>>
    
    @GET("messages/{receiverId}")
    fun getMessagesWithUser(@Path("receiverId") receiverId: String, @Query("senderId") senderId: String): Call<List<Message>>
    
    @POST("messages")
    fun sendMessage(@Query("senderId") senderId: String, @Query("receiverId") receiverId: String, @Query("content") content: String): Call<Message>
    
    @GET("community-posts")
    fun getCommunityPosts(): Call<List<CommunityPost>>
    
    @POST("community-posts")
    fun createCommunityPost(@Query("authorId") authorId: String, @Query("title") title: String, @Query("content") content: String, @Query("category") category: String): Call<CommunityPost>
}