package com.example.iurankomplek.network
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.model.PaymentResponse
import com.example.iurankomplek.model.PaymentStatusResponse
import com.example.iurankomplek.model.PaymentConfirmationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("users")
    suspend fun getUsers(): Response<UserResponse>
    
    @GET("pemanfaatan")
    suspend fun getPemanfaatan(): Response<PemanfaatanResponse>
    
    // Communication endpoints
    @GET("announcements")
    suspend fun getAnnouncements(): Response<List<Announcement>>
    
    @GET("messages")
    suspend fun getMessages(@Query("userId") userId: String): Response<List<Message>>
    
    @GET("messages/{receiverId}")
    suspend fun getMessagesWithUser(@Path("receiverId") receiverId: String, @Query("senderId") senderId: String): Response<List<Message>>
    
    @POST("messages")
    suspend fun sendMessage(@Query("senderId") senderId: String, @Query("receiverId") receiverId: String, @Query("content") content: String): Response<Message>
    
    @GET("community-posts")
    suspend fun getCommunityPosts(): Response<List<CommunityPost>>
    
    @POST("community-posts")
    suspend fun createCommunityPost(@Query("authorId") authorId: String, @Query("title") title: String, @Query("content") content: String, @Query("category") category: String): Response<CommunityPost>
    
    // Payment endpoints
    @POST("payments/initiate")
    suspend fun initiatePayment(
        @Query("amount") amount: String,
        @Query("description") description: String,
        @Query("customerId") customerId: String,
        @Query("paymentMethod") paymentMethod: String
    ): Response<PaymentResponse>
    
    @GET("payments/{id}/status")
    suspend fun getPaymentStatus(@Path("id") id: String): Response<PaymentStatusResponse>
    
    @POST("payments/{id}/confirm")
    suspend fun confirmPayment(@Path("id") id: String): Response<PaymentConfirmationResponse>
}