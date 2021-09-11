package com.mobdeve.s15.animall

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

class MessageModel(
    val convoId: String,
    val timestamp: Date,
    val sender: String,
    val message: String
) {
    constructor() : this(
        "",
         Date(),
        "",
        ""
    )
}