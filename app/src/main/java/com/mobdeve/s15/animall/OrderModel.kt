package com.mobdeve.s15.animall

class OrderModel(
    val orderId: String,
    val customerId: String,
    val listingId: String,
    val listingName: String,
    val photosId: String,
    val quantity: Long,
    val soldPrice: Double
)