package com.mobdeve.s15.animall

object MyFirestoreReferences {
    const val USERS_COLLECTION = "users"
    const val CATEGORIES_COLLECTION = "categories"
    const val CONVERSATIONS_COLLECTION = "conversations"
    const val ORDERS_COLLECTION = "orders"
    const val LISTINGS_COLLECTION = "listings"
    const val MESSAGES_COLLECTION = "messages"

    //Listing
    const val LISTING_IS_OPEN = "isOpen"
    const val CATEGORY_FIELD = "category"
    const val DESCRIPTION_FIELD = "description"
    const val PRODUCT_NAME_FIELD = "name"
    const val LOCATION_FIELD = "preferredLocation"
    const val SELLER_FIELD = "seller"
    const val STOCK_FIELD = "stock"
    const val PRICE_FIELD = "unitPrice"
    const val PHOTOS_FIELD = "photos"

    //Order Model
    const val ORDER_CUSTOMER_ID_FIELD = "customerId"
    const val ORDER_LISTING_ID_FIELD = "listingId"
    const val ORDER_LISTING_NAME_FIELD = "listingName"
    const val ORDER_PHOTOS_ID_FIELD = "photosId"
    const val ORDER_QUANTITY_FIELD = "quantity"
    const val ORDER_SOLD_PRICE_FIELD = "soldPrice"

    //User
    const val EMAIL_FIELD = "email"
    const val NAME_FIELD = "name"
    const val PREF_LOCATION_FIELD = "preferredLocation"

    //Conversation
    const val RECIPIENT_FIELD = "recipientEmail"
    const val SENDER_FIELD = "senderEmail"
    const val MESSAGES_FIELD = "messages"
    const val LISTING_ID_FIELD = "listingId"
    const val LISTING_NAME_FIELD = "listingName"
    const val LISTING_PHOTO_FIELD = "listingPhoto"
    const val CONVO_TIMESTAMP_FIELD = "timestamp"
    const val CONVO_ID_FIELD = "id"
    const val CONVO_USERS_FIELD = "users"

    //Message
    const val TIME_FIELD = "timestamp"
    const val MESSAGE_FIELD = "message"
    const val MESSAGE_SENDER_FIELD = "sender"
    const val MESSAGE_CONVO_FIELD = "convoId"
    const val MESSAGE_OFFER_FIELD = "offer"
    const val MESSAGE_OFFER_QUANTITY_FIELD = "quantity"
    const val MESSAGE_OFFER_AMOUNT_FIELD = "amount"
    const val MESSAGE_ADDRESSED_FIELD = "addressed"
    const val MESSAGE_ID_FIELD = "id"
}