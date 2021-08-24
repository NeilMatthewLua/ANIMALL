package com.mobdeve.s15.animall

class ListingModel(
    
    val category: String,
    val description: String,
    val name: String,
    val preferredLocation: String,
    val seller: String,
    val stock: Int,
    val unitPrice: Double,
    val photos: ArrayList<String>
)