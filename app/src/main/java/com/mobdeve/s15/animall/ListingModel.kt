package com.mobdeve.s15.animall

import java.util.*
import kotlin.collections.ArrayList

class ListingModel(
    val listingId: String,
    var isOpen: Boolean,
    val category: String,
    val description: String,
    val name: String,
    val preferredLocation: String,
    val seller: String,
    var stock: Long,
    val unitPrice: Double,
    val photos: ArrayList<String>,
    val id: String = "",
    val timestamp: Date
)