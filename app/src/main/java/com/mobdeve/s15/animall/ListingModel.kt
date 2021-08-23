package com.mobdeve.s15.animall

class ListingModel(
    val category: String,
    val description: String,
    val name: String, //TODO: Update to link to user in db
    val preferredLocation: String,
    val seller: String,
    val stock: Int,
    val unitPrice: Double,
    val photos: Array<String> //TODO: Update to links in the future
)