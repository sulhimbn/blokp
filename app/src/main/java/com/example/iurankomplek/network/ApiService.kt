package com.example.iurankomplek.network

import com.example.iurankomplek.data.api.models.UserResponse
import com.example.iurankomplek.data.api.models.PemanfaatanResponse
import com.example.iurankomplek.data.api.models.ApiResponse
import com.example.iurankomplek.data.api.models.ApiListResponse
import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.model.PaymentResponse
import com.example.iurankomplek.model.PaymentStatusResponse
import com.example.iurankomplek.model.PaymentConfirmationResponse
import com.example.iurankomplek.model.VendorResponse
import com.example.iurankomplek.model.SingleVendorResponse
import com.example.iurankomplek.model.WorkOrderResponse
import com.example.iurankomplek.model.SingleWorkOrderResponse
import com.example.iurankomplek.network.model.CreateVendorRequest
import com.example.iurankomplek.network.model.UpdateVendorRequest
import com.example.iurankomplek.network.model.CreateWorkOrderRequest
import com.example.iurankomplek.network.model.AssignVendorRequest
import com.example.iurankomplek.network.model.UpdateWorkOrderRequest
import com.example.iurankomplek.network.model.SendMessageRequest
import com.example.iurankomplek.network.model.CreateCommunityPostRequest
import com.example.iurankomplek.network.model.InitiatePaymentRequest
import retrofit2.Body
import retrofit2.GET
import retrofit2.POST
import retrofit2.PUT
import retrofit2.Path
import retrofit2.Query
import retrofit2.Response

interface ApiService {

    @GET("users")
    suspend fun getUsers(): Response<UserResponse>

    @GET("pemanfaatan")
    suspend fun getPemanfaatan(): Response<PemanfaatanResponse>

    @GET("announcements")
    suspend fun getAnnouncements(): Response<List<Announcement>>

    @GET("messages")
    suspend fun getMessages(@Query("userId") userId: String): Response<List<Message>>

    @GET("messages/{receiverId}")
    suspend fun getMessagesWithUser(
        @Path("receiverId") receiverId: String,
        @Query("senderId") senderId: String
    ): Response<List<Message>>

    @POST("messages")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): Response<Message>

    @GET("community-posts")
    suspend fun getCommunityPosts(): Response<List<CommunityPost>>

    @POST("community-posts")
    suspend fun createCommunityPost(
        @Body request: CreateCommunityPostRequest
    ): Response<CommunityPost>

    @POST("payments/initiate")
    suspend fun initiatePayment(
        @Body request: InitiatePaymentRequest
    ): Response<PaymentResponse>

    @GET("payments/{id}/status")
    suspend fun getPaymentStatus(@Path("id") id: String): Response<PaymentStatusResponse>

    @POST("payments/{id}/confirm")
    suspend fun confirmPayment(@Path("id") id: String): Response<PaymentConfirmationResponse>

    @GET("vendors")
    suspend fun getVendors(): Response<VendorResponse>

    @GET("vendors/{id}")
    suspend fun getVendor(@Path("id") id: String): Response<SingleVendorResponse>

    @POST("vendors")
    suspend fun createVendor(
        @Body request: CreateVendorRequest
    ): Response<SingleVendorResponse>

    @PUT("vendors/{id}")
    suspend fun updateVendor(
        @Path("id") id: String,
        @Body request: UpdateVendorRequest
    ): Response<SingleVendorResponse>

    @GET("work-orders")
    suspend fun getWorkOrders(): Response<WorkOrderResponse>

    @GET("work-orders/{id}")
    suspend fun getWorkOrder(@Path("id") id: String): Response<SingleWorkOrderResponse>

    @POST("work-orders")
    suspend fun createWorkOrder(
        @Body request: CreateWorkOrderRequest
    ): Response<SingleWorkOrderResponse>

    @PUT("work-orders/{id}/assign")
    suspend fun assignVendorToWorkOrder(
        @Path("id") id: String,
        @Body request: AssignVendorRequest
    ): Response<SingleWorkOrderResponse>

    @PUT("work-orders/{id}/status")
    suspend fun updateWorkOrderStatus(
        @Path("id") id: String,
        @Body request: UpdateWorkOrderRequest
    ): Response<SingleWorkOrderResponse>
}
