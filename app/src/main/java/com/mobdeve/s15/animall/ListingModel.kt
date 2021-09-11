package com.mobdeve.s15.animall

class ListingModel(
    val listingId: String,
    var isOpen: Boolean,
    val category: String,
    val description: String,
    val name: String,
    val preferredLocation: String,
    val seller: String,
    val stock: Long,
    val unitPrice: Double,
    val photos: ArrayList<String>
)