package com.example.iurankomplek.network
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.model.PemanfaatanResponse
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
import com.example.iurankomplek.model.VendorWorkOrderRequest
import com.example.iurankomplek.model.AssignVendorRequest
import com.example.iurankomplek.model.UpdateWorkOrderStatusRequest
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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
    
    // Vendor Management endpoints
    @GET("vendors")
    suspend fun getVendors(): Response<VendorResponse>
    
    @GET("vendors/{id}")
    suspend fun getVendor(@Path("id") id: String): Response<SingleVendorResponse>
    
    @POST("vendors")
    suspend fun createVendor(
        @Query("name") name: String,
        @Query("contactPerson") contactPerson: String,
        @Query("phoneNumber") phoneNumber: String,
        @Query("email") email: String,
        @Query("specialty") specialty: String,
        @Query("address") address: String,
        @Query("licenseNumber") licenseNumber: String,
        @Query("insuranceInfo") insuranceInfo: String,
        @Query("contractStart") contractStart: String,
        @Query("contractEnd") contractEnd: String
    ): Response<SingleVendorResponse>
    
    @PUT("vendors/{id}")
    suspend fun updateVendor(
        @Path("id") id: String,
        @Query("name") name: String,
        @Query("contactPerson") contactPerson: String,
        @Query("phoneNumber") phoneNumber: String,
        @Query("email") email: String,
        @Query("specialty") specialty: String,
        @Query("address") address: String,
        @Query("licenseNumber") licenseNumber: String,
        @Query("insuranceInfo") insuranceInfo: String,
        @Query("contractStart") contractStart: String,
        @Query("contractEnd") contractEnd: String,
        @Query("isActive") isActive: Boolean
    ): Response<SingleVendorResponse>
    
    // Work Order endpoints
    @GET("work-orders")
    suspend fun getWorkOrders(): Response<WorkOrderResponse>
    
    @GET("work-orders/{id}")
    suspend fun getWorkOrder(@Path("id") id: String): Response<SingleWorkOrderResponse>
    
    @POST("work-orders")
    suspend fun createWorkOrder(
        @Query("title") title: String,
        @Query("description") description: String,
        @Query("category") category: String,
        @Query("priority") priority: String,
        @Query("propertyId") propertyId: String,
        @Query("reporterId") reporterId: String,
        @Query("estimatedCost") estimatedCost: Double
    ): Response<SingleWorkOrderResponse>
    
    @PUT("work-orders/{id}/assign")
    suspend fun assignVendorToWorkOrder(
        @Path("id") id: String,
        @Query("vendorId") vendorId: String,
        @Query("scheduledDate") scheduledDate: String?
    ): Response<SingleWorkOrderResponse>
    
    @PUT("work-orders/{id}/status")
    suspend fun updateWorkOrderStatus(
        @Path("id") id: String,
        @Query("status") status: String,
        @Query("notes") notes: String?
    ): Response<SingleWorkOrderResponse>
}