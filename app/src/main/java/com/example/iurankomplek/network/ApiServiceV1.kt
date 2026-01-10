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
import com.example.iurankomplek.network.model.HealthCheckRequest
import com.example.iurankomplek.network.model.HealthCheckResponse
import retrofit2.Body
import retrofit2.GET
import retrofit2.POST
import retrofit2.PUT
import retrofit2.Path
import retrofit2.Query
import retrofit2.Response

interface ApiServiceV1 {

    @GET("api/v1/users")
    suspend fun getUsers(): Response<ApiResponse<UserResponse>>

    @GET("api/v1/pemanfaatan")
    suspend fun getPemanfaatan(): Response<ApiResponse<PemanfaatanResponse>>

    @GET("api/v1/announcements")
    suspend fun getAnnouncements(): Response<ApiListResponse<Announcement>>

    @GET("api/v1/messages")
    suspend fun getMessages(@Query("userId") userId: String): Response<ApiListResponse<Message>>

    @GET("api/v1/messages/{receiverId}")
    suspend fun getMessagesWithUser(
        @Path("receiverId") receiverId: String,
        @Query("senderId") senderId: String
    ): Response<ApiListResponse<Message>>

    @POST("api/v1/messages")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): Response<ApiResponse<Message>>

    @GET("api/v1/community-posts")
    suspend fun getCommunityPosts(): Response<ApiListResponse<CommunityPost>>

    @POST("api/v1/community-posts")
    suspend fun createCommunityPost(
        @Body request: CreateCommunityPostRequest
    ): Response<ApiResponse<CommunityPost>>

    @POST("api/v1/payments/initiate")
    suspend fun initiatePayment(
        @Body request: InitiatePaymentRequest
    ): Response<ApiResponse<PaymentResponse>>

    @GET("api/v1/payments/{id}/status")
    suspend fun getPaymentStatus(@Path("id") id: String): Response<ApiResponse<PaymentStatusResponse>>

    @POST("api/v1/payments/{id}/confirm")
    suspend fun confirmPayment(@Path("id") id: String): Response<ApiResponse<PaymentConfirmationResponse>>

    @GET("api/v1/vendors")
    suspend fun getVendors(): Response<ApiResponse<VendorResponse>>

    @GET("api/v1/vendors/{id}")
    suspend fun getVendor(@Path("id") id: String): Response<ApiResponse<SingleVendorResponse>>

    @POST("api/v1/vendors")
    suspend fun createVendor(
        @Body request: CreateVendorRequest
    ): Response<ApiResponse<SingleVendorResponse>>

    @PUT("api/v1/vendors/{id}")
    suspend fun updateVendor(
        @Path("id") id: String,
        @Body request: UpdateVendorRequest
    ): Response<ApiResponse<SingleVendorResponse>>

    @GET("api/v1/work-orders")
    suspend fun getWorkOrders(): Response<ApiResponse<WorkOrderResponse>>

    @GET("api/v1/work-orders/{id}")
    suspend fun getWorkOrder(@Path("id") id: String): Response<ApiResponse<SingleWorkOrderResponse>>

    @POST("api/v1/work-orders")
    suspend fun createWorkOrder(
        @Body request: CreateWorkOrderRequest
    ): Response<ApiResponse<SingleWorkOrderResponse>>

    @PUT("api/v1/work-orders/{id}/assign")
    suspend fun assignVendorToWorkOrder(
        @Path("id") id: String,
        @Body request: AssignVendorRequest
    ): Response<ApiResponse<SingleWorkOrderResponse>>

    @PUT("api/v1/work-orders/{id}/status")
    suspend fun updateWorkOrderStatus(
        @Path("id") id: String,
        @Body request: UpdateWorkOrderRequest
    ): Response<ApiResponse<SingleWorkOrderResponse>>
    
    @POST("api/v1/health")
    suspend fun getHealth(@Body request: HealthCheckRequest): Response<ApiResponse<HealthCheckResponse>>
}
