package com.picmewall.picmewall.api

import com.picmewall.picmewall.models.CheckoutResponse
import com.picmewall.picmewall.models.TileInfoResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

//    @Headers("Content-Type: application/json")
//    @Multipart
//    @POST("api/sale")
//    suspend fun requestOrder(@Part attachedFiles: List<MultipartBody.Part>,
//                             @Part("upazila") upazila: RequestBody,
//                             @Part("city") city: RequestBody,
//                             @Part("customerName") customerName: RequestBody,
//                             @Part("mobile") mobile: RequestBody,
//                             @Part("invoiceID") invoiceID: RequestBody,
//                             @Part("payemtReferenceID") payemtReferenceID: RequestBody,
//                             @Part("address") address: RequestBody,
//                             @Part("date") date: RequestBody,
//                             @Part("grand_total") grand_total: RequestBody): Response<CheckoutResponse>

//    @Headers("Content-Type: application/json")
//    @Multipart
//    @POST("api/sale")
//    suspend fun requestOrder(@Part files: ArrayList<MultipartBody.Part>,
//                             @PartMap partMap: HashMap<String, RequestBody>): Response<CheckoutResponse>

    @POST("api/sale")
    suspend fun requestOrder(@Body partFormData: RequestBody): Response<CheckoutResponse>

    @GET("api/sales/price")
    suspend fun getTileInfo(): Response<TileInfoResponse>
}