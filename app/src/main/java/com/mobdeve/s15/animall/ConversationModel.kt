package com.mobdeve.s15.animall

import java.util.*

class ConversationModel(
    val recipientEmail: String,
    val senderEmail: String,
    val listingId: String,
    val listingName: String,
    val listingPhoto: String,
    val id: String = "",
    val timestamp: Date = Date(),
    val latestMessage: String = "",
    val latestSender: String = ""
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        "",
        Date(),
        "",
        ""
    )
}