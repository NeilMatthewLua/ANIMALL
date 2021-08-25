package com.mobdeve.s15.animall

object MyFirestoreReferences {
    const val USERS_COLLECTION = "users"
    const val CATEGORIES_COLLECTION = "categories"
    const val CONVERSATIONS_COLLECTION = "conversations"
    const val ORDERS_COLLECTION = "orders"
    const val LISTINGS_COLLECTION = "listings"

    //Listing
    const val CATEGORY_FIELD = "category"
    const val DESCRIPTION_FIELD = "description"
    const val PRODUCT_NAME_FIELD = "name"
    const val LOCATION_FIELD = "preferredLocation"
    const val SELLER_FIELD = "seller"
    const val STOCK_FIELD = "stock"
    const val PRICE_FIELD = "unitPrice"
    const val PHOTOS_FIELD = "photos"

    //User
    const val EMAIL_FIELD = "email"
    const val NAME_FIELD = "name"
    const val PREF_LOCATION_FIELD = "preferredLocation"

    //Conversation
    const val RECEIPIENT_FIELD = "receipientEmail"
    const val SENDER_FIELD = "senderEmail"
    const val MESSAGES_FIELD = "messages"
    const val LISTING_ID_FIELD = "listingId"
    const val LISTING_NAME_FIELD = "listingName"
    const val LISTING_PHOTO_FIELD = "listingPhoto"

    //Message
    const val TIME_FILED = "timestamp"
    const val MESSAGE_FIELD = "message"
    const val MESSAGE_SENDER_FIELD = "sender"
}