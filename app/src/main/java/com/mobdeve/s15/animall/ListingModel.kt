package com.mobdeve.s15.animall

class ListingModel(
    val listingId: String,
    var isOpen: Boolean,
    val category: String,
    val description: String,
    val name: String,
    val preferredLocation: String,
    val seller: String,
    var stock: Long,
    val unitPrice: Long,
    val photos: ArrayList<String>,
    val distanceFromUser: Double = Double.MAX_VALUE
)