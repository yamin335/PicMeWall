package com.picmewall.picmewall.models

data class CheckoutResponse(val code: Int?, val status: String?, val message: String?, val data: CheckoutResponseData?)

data class CheckoutResponseData(val sale: CheckoutResponseSale?)

data class CheckoutResponseSale(val status: String?, val date: String?, val invoiceID: String?, val payemtReferenceID: String?, val folderName: String?, val address: String?, val grand_total: Double?, val mobile: String?, val upazila: String?, val city: String?, val customerName: String?, val discount: Double?, val updated_at: String?, val created_at: String?, val id: Int?)