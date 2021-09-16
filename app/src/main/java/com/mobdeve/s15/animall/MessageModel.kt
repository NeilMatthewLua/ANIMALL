package com.mobdeve.s15.animall

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

class MessageModel(
    val convoId: String,
    val timestamp: Date,
    val sender: String,
    val message: String,
    var offer: Boolean,
    var offerPrice: Int,
    var quantity: Int,
    var addressed: Boolean,
    var id: String
) {
    constructor() : this(
        "",
         Date(),
        "",
        "",
        false,
        -1,
        -1,
        false,
        ""
    )
}