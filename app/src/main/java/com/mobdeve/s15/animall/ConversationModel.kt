package com.mobdeve.s15.animall

class ConversationModel(
    val receipientEmail: String,
    val senderEmail: String,
    val messages: ArrayList<MessageModel>,
    val listingId: String,
    val listingName: String,
    val listingPhoto: String
)