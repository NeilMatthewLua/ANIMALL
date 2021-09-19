package com.mobdeve.s15.animall

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.collections.ArrayList

object DatabaseManager {
    const val TAG = "FIRESTORE"
    val db = Firebase.firestore
    val PH_UPPER_LON: Double = 126.537423944
    val PH_UPPER_LAT: Double = 18.5052273625
    val PH_LOWER_LON: Double = 117.17427453
    val PH_LOWER_LAT: Double = 5.58100332277

    fun getInstance(): FirebaseFirestore {
        return db
    }

    suspend fun initializeListingData(): ArrayList<ListingModel> = coroutineScope {
        val listingRef = db.collection(MyFirebaseReferences.LISTINGS_COLLECTION)
        val data = ArrayList<ListingModel>()
        try {
            val job = listingRef.whereEqualTo(MyFirebaseReferences.LISTING_IS_OPEN, true).get().await()
            for (document in job.documents) {
                var photoArray = document[MyFirebaseReferences.PHOTOS_FIELD] as ArrayList<String>
                var unitPrice = (document[MyFirebaseReferences.PRICE_FIELD] as Double).toLong()
                data.add(ListingModel(
                    document.reference.id,
                    document[MyFirebaseReferences.LISTING_IS_OPEN] as Boolean,
                    document[MyFirebaseReferences.CATEGORY_FIELD].toString(),
                    document[MyFirebaseReferences.DESCRIPTION_FIELD].toString(),
                    document[MyFirebaseReferences.PRODUCT_NAME_FIELD].toString(),
                    document[MyFirebaseReferences.LOCATION_FIELD].toString(),
                    document[MyFirebaseReferences.SELLER_FIELD].toString(),
                    document[MyFirebaseReferences.STOCK_FIELD] as Long,
                    unitPrice as Long,
                    photoArray
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("FIREBASE:", "ERROR RETRIEVING LISTINGS")
        }

        data
    }

    suspend fun filteredListingData(filterOption: String, sortOption: String, searchOption: String, userCity: String, context: Context, lastDocId: String = ""): ArrayList<ListingModel> = coroutineScope {
        val listingRef = db.collection(MyFirebaseReferences.LISTINGS_COLLECTION)
        var data = ArrayList<ListingModel>()
        Log.d(TAG, searchOption)
        try {
            var job : Query? = null
            // Filter options (e.g. category)
            if (filterOption.isNotBlank()) {
                job = listingRef.whereEqualTo(MyFirebaseReferences.CATEGORY_FIELD, filterOption)
            }
            // Search options (e.g. search inputs)
            if (searchOption.isNotBlank()) {
                if (job != null) {
                    job = job.whereGreaterThanOrEqualTo(MyFirebaseReferences.NAME_FIELD, searchOption)
                             .whereLessThanOrEqualTo(MyFirebaseReferences.NAME_FIELD, searchOption+"\uF7FF")
                } else {
                    job = listingRef.whereGreaterThanOrEqualTo(MyFirebaseReferences.NAME_FIELD, searchOption)
                                    .whereLessThanOrEqualTo(MyFirebaseReferences.NAME_FIELD, searchOption+"\uF7FF")
                }
            }
            // Last doc (when user clicks load more)
            if (lastDocId.isNotBlank()) {
                val lastDocSnapshot = listingRef.document(lastDocId).get().await()
                if (job != null) {
                    job = job.startAfter(lastDocSnapshot)
                } else {
                    job = listingRef.startAfter(lastDocSnapshot)
                }
            }
            if (job == null) {
                job = listingRef
            }
            // Only get
            val result = job.whereEqualTo(MyFirebaseReferences.LISTING_IS_OPEN, true).get().await()
            for (document in result.documents) {
                if (document["isOpen"] as Boolean) {
                    var photoArray = document[MyFirebaseReferences.PHOTOS_FIELD] as ArrayList<String>
                    // Convert to Long
                    var unitPrice = (document[MyFirebaseReferences.PRICE_FIELD] as Double).toLong()

                    // Default distance is max
                    var distanceFromUser: Double = Double.MAX_VALUE
                    val getLocationDistances = launch (Dispatchers.IO) {
                        var geocoderObj = Geocoder(context)
                        var cityNameUser = userCity
                        var cityNameListing = document[MyFirebaseReferences.LOCATION_FIELD] as String
                        // Get the addresses objects
                        var addressResultUser = geocoderObj.getFromLocationName(
                            cityNameUser,
                            1,
                            PH_LOWER_LAT,
                            PH_LOWER_LON,
                            PH_UPPER_LAT,
                            PH_UPPER_LON
                        )

                        var addressResultListing = geocoderObj.getFromLocationName(
                            cityNameListing,
                            1,
                            PH_LOWER_LAT,
                            PH_LOWER_LON,
                            PH_UPPER_LAT,
                            PH_UPPER_LON
                        )

                        // If address was not found then distance is set to the max value of double
                        if (addressResultUser.size == 0 || addressResultListing.size == 0) {
                            Log.d(TAG, "Address not found")
                        } else {
                            // Otherwise get the first result from the addresses
                            val locationResultUser = addressResultUser.get(0)
                            val locationResultListing = addressResultListing.get(0)

                            // Convert into location objects to get distance from each other in meters
                            var locationObjUser = Location("User")
                            locationObjUser.latitude = locationResultUser.latitude
                            locationObjUser.longitude = locationResultUser.longitude

                            var locationObjListing = Location("Listing")
                            locationObjListing.latitude = locationResultListing.latitude
                            locationObjListing.longitude = locationResultListing.longitude

                            distanceFromUser = locationObjUser.distanceTo(locationObjListing).toDouble()
                        }
                    }
                    // Wait for distances to be computed
                    getLocationDistances.join()

                    data.add(ListingModel(
                        document.reference.id,
                        true,
                        document[MyFirebaseReferences.CATEGORY_FIELD].toString(),
                        document[MyFirebaseReferences.DESCRIPTION_FIELD].toString(),
                        document[MyFirebaseReferences.PRODUCT_NAME_FIELD].toString(),
                        document[MyFirebaseReferences.LOCATION_FIELD].toString(),
                        document[MyFirebaseReferences.SELLER_FIELD].toString(),
                        document[MyFirebaseReferences.STOCK_FIELD] as Long,
                        unitPrice as Long,
                        photoArray,
                        distanceFromUser
                    ))
                }
            }

//            // Filter the data based on search options
//            if (searchOption.isNotBlank()) {
//                data = ArrayList(data.filter {
//                    searchOption.lowercase() in it.name.lowercase()
//                })
//            }

            if (sortOption == MyFirebaseReferences.SORT_UNIT_PRICE_ASC) {
                data.sortBy { it.unitPrice }
            } else if (sortOption == MyFirebaseReferences.SORT_UNIT_PRICE_DSC) {
                data.sortByDescending { it.unitPrice }
            } else if (sortOption == MyFirebaseReferences.SORT_PROXIMITY) {
                for (i in data) {
                    Log.d(TAG, i.distanceFromUser.toString())
                }
                data.sortBy { it.distanceFromUser }
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING LISTINGS")
        }

        data
    }

    suspend fun initializeConversationData(email: String): ArrayList<ConversationModel> = coroutineScope {
        val conversationRef = db.collection(MyFirebaseReferences.CONVERSATIONS_COLLECTION)
        val data = ArrayList<ConversationModel>()
        try {
            val receive_job = conversationRef
                        .whereArrayContains("users", email)
                        .get()
                        .await()
            Log.i("`DatabaseManager`", "Getting conversations")
            for (document in receive_job.documents) {
                Log.i("`DatabaseManager`", "Got one as recipient")
                var recipientEmail = document[MyFirebaseReferences.RECIPIENT_FIELD] as String
                var senderEmail = document[MyFirebaseReferences.SENDER_FIELD] as String
                val listingId =  document[MyFirebaseReferences.LISTING_ID_FIELD] as String
                val listingName =  document[MyFirebaseReferences.LISTING_NAME_FIELD] as String
                val listingPhoto = document[MyFirebaseReferences.LISTING_PHOTO_FIELD] as String
                val id = document[MyFirebaseReferences.CONVO_ID_FIELD] as String

                var message = getLatestMessage(id)

                if (message != null) {
                    data.add(
                        ConversationModel(
                            recipientEmail,
                            senderEmail,
                            listingId,
                            listingName,
                            listingPhoto,
                            id,
                            message!!.timestamp
                        )
                    )
                }
            }

            data.sortByDescending { it.timestamp }

            data.forEach {
                Log.i("DBManager Got a conversation", "Sender: ${it.senderEmail}: ${it.timestamp}")
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING CONVERSATIONS")
        }

        for (subdata in data) {
            Log.i("initializeConvo", "${subdata.id}: ${subdata.listingName}")
        }
        data
    }

    suspend fun getUserName(userEmail: String): String = coroutineScope {
        val userRef = db.collection(MyFirebaseReferences.USERS_COLLECTION)
        var userName = ""
        try {
            val job = userRef.whereEqualTo(MyFirebaseReferences.EMAIL_FIELD, userEmail).get().await()
            for (document in job.documents) {
                userName = document[MyFirebaseReferences.NAME_FIELD] as String
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING USER NAME")
        }
        userName
    }


    suspend fun getUserCity(userEmail: String): String = coroutineScope {
        val userRef = db.collection(MyFirebaseReferences.USERS_COLLECTION)
        var city = ""
        try {
            val job = userRef.whereEqualTo(MyFirebaseReferences.EMAIL_FIELD, userEmail).get().await()
            for (document in job.documents) {
                city = document[MyFirebaseReferences.PREF_LOCATION_FIELD] as String
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING USER NAME")
        }
        city
    }

    suspend fun getUserListings(userEmail: String, openListingsOnly: Boolean = false): ArrayList<ListingModel> = coroutineScope {
        val listingRef = db.collection(MyFirebaseReferences.LISTINGS_COLLECTION)
        val data = ArrayList<ListingModel>()
        try {
            var job : QuerySnapshot? = null
            if (openListingsOnly) {
                job = listingRef
                    .whereEqualTo(MyFirebaseReferences.SELLER_FIELD, userEmail)
                    .whereEqualTo(MyFirebaseReferences.LISTING_IS_OPEN, true)
                    .get().await()
            } else {
                job = listingRef.whereEqualTo(MyFirebaseReferences.SELLER_FIELD, userEmail).get().await()
            }
            for (document in job.documents) {
                var photoArray = document[MyFirebaseReferences.PHOTOS_FIELD] as ArrayList<String>
                // Convert to Long
                var unitPrice = (document[MyFirebaseReferences.PRICE_FIELD] as Double).toLong()

                data.add(ListingModel(
                    document.reference.id,
                    document[MyFirebaseReferences.LISTING_IS_OPEN] as Boolean,
                    document[MyFirebaseReferences.CATEGORY_FIELD].toString(),
                    document[MyFirebaseReferences.DESCRIPTION_FIELD].toString(),
                    document[MyFirebaseReferences.PRODUCT_NAME_FIELD].toString(),
                    document[MyFirebaseReferences.LOCATION_FIELD].toString(),
                    document[MyFirebaseReferences.SELLER_FIELD].toString(),
                    document[MyFirebaseReferences.STOCK_FIELD] as Long,
                    unitPrice as Long,
                    photoArray
                ))
            }
            Log.d("FIREBASE:", job.documents.size.toString())
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING LISTINGS")
        }
        data
    }

    suspend fun getUserOrders(customerEmail: String): ArrayList<OrderModel> = coroutineScope {
        val orderRef = db.collection(MyFirebaseReferences.ORDERS_COLLECTION)
        val data = ArrayList<OrderModel>()
        try {
            val job = orderRef.whereEqualTo(MyFirebaseReferences.ORDER_CUSTOMER_ID_FIELD, customerEmail).get().await()
            for (document in job.documents) {
                // Convert to Long
                var unitPrice = document[MyFirebaseReferences.ORDER_SOLD_PRICE_FIELD] as Long

                data.add(OrderModel(
                    document.reference.id,
                    document[MyFirebaseReferences.ORDER_CUSTOMER_ID_FIELD] as String,
                    document[MyFirebaseReferences.ORDER_LISTING_ID_FIELD] as String,
                    document[MyFirebaseReferences.ORDER_LISTING_NAME_FIELD] as String,
                    document[MyFirebaseReferences.ORDER_PHOTOS_ID_FIELD] as String,
                    document[MyFirebaseReferences.ORDER_QUANTITY_FIELD] as Long,
                    unitPrice,
                    document[MyFirebaseReferences.ORDER_IS_CONFIRMED_FIELD] as Boolean
                ))
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", e.toString())
        }

        data
    }

    suspend fun getUserViaEmail(userEmail: String): UserModel? = coroutineScope {
        val conversationRef = db.collection(MyFirebaseReferences.USERS_COLLECTION)
        var user: UserModel? = null
        try {
            Log.i("FIREBASE", "UserID ${userEmail}")

            //WHERE DocumentID (PK) is userId
            val job = conversationRef
                .whereEqualTo(MyFirebaseReferences.EMAIL_FIELD, userEmail)
                .get()
                .await()
            Log.i("`DatabaseManager`", "Getting the user")
            for (document in job.documents) {
                var userEmail = document[MyFirebaseReferences.EMAIL_FIELD] as String
                var userName = document[MyFirebaseReferences.NAME_FIELD] as String
                var userPrefLoc = document[MyFirebaseReferences.PREF_LOCATION_FIELD] as String
                user = UserModel(
                    userEmail,
                    userName,
                    userPrefLoc
                )
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING USER")
        }

        user
    }

    suspend fun getConversation(listingId: String, loggedUser: String): ConversationModel? = coroutineScope {
        val conversationRef = db.collection(MyFirebaseReferences.CONVERSATIONS_COLLECTION)
        var conversation: ConversationModel? = null

        try {
            Log.i("FIREBASE", "UserID ${loggedUser} listingId ${listingId}")

            val job = conversationRef
                .whereEqualTo(MyFirebaseReferences.SENDER_FIELD, loggedUser)
                .whereEqualTo(MyFirebaseReferences.LISTING_ID_FIELD, listingId)
                .get()
                .await()

            Log.i("`DatabaseManager`", "Checking for conversations")
            for (document in job.documents) {
                conversation = ConversationModel(
                    document[MyFirebaseReferences.RECIPIENT_FIELD] as String,
                    document[MyFirebaseReferences.SENDER_FIELD] as String,
                    document[MyFirebaseReferences.LISTING_ID_FIELD] as String,
                    document[MyFirebaseReferences.LISTING_NAME_FIELD] as String,
                    document[MyFirebaseReferences.LISTING_PHOTO_FIELD] as String,
                    document.id,
                )
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING USER")
        }

        conversation
    }

    suspend fun getConversationListing(convoId: String): ListingModel? = coroutineScope {
        val conversationRef = db.collection(MyFirebaseReferences.CONVERSATIONS_COLLECTION)
        var listing: ListingModel? = null
        Log.i("DBManager convoId", "${convoId}")
        try {
            val convo = conversationRef
                            .document(convoId)
                            .get()
                            .await()

            val listingRef = db.collection(MyFirebaseReferences.LISTINGS_COLLECTION)
            val listingId = convo[MyFirebaseReferences.LISTING_ID_FIELD] as String

            val listing_doc = listingRef
                            .document(listingId)
                            .get()
                            .await()

            var photoArray = listing_doc[MyFirebaseReferences.PHOTOS_FIELD] as ArrayList<String>
            // Convert to Long
            var unitPrice = (listing_doc[MyFirebaseReferences.PRICE_FIELD] as Double).toLong()

            listing = ListingModel(
                listing_doc.id,
                listing_doc[MyFirebaseReferences.LISTING_IS_OPEN] as Boolean,
                listing_doc[MyFirebaseReferences.CATEGORY_FIELD].toString(),
                listing_doc[MyFirebaseReferences.DESCRIPTION_FIELD].toString(),
                listing_doc[MyFirebaseReferences.PRODUCT_NAME_FIELD].toString(),
                listing_doc[MyFirebaseReferences.LOCATION_FIELD].toString(),
                listing_doc[MyFirebaseReferences.SELLER_FIELD].toString(),
                listing_doc[MyFirebaseReferences.STOCK_FIELD] as Long,
                unitPrice as Long,
                photoArray
            )
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING LISTING")
        }

        println(listing)
        listing
    }

    suspend fun getLatestMessage(convoId: String): MessageModel? = coroutineScope {
        val messageRef = db.collection(MyFirebaseReferences.MESSAGES_COLLECTION)
        var latestMessage: MessageModel? = null

        try {
            Log.i("DatabaseManager", "conversation ID to retrieve: ${convoId} ${convoId.length}")

            messageRef
                .whereEqualTo(MyFirebaseReferences.MESSAGE_CONVO_FIELD, convoId)
                .orderBy(MyFirebaseReferences.TIME_FIELD, Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    Log.i("DatabaseManager", "Document size: ${documents.size()}")
                    for (document in documents) {
                        Log.d("DatabaseManager", "${document.id} => ${document.data}")
                        var convoId = document[MyFirebaseReferences.MESSAGE_CONVO_FIELD] as String
                        var timestamp = document[MyFirebaseReferences.TIME_FIELD] as Timestamp
                        var sender = document[MyFirebaseReferences.MESSAGE_SENDER_FIELD] as String
                        var message = document[MyFirebaseReferences.MESSAGE_FIELD] as String
                        var offer = document[MyFirebaseReferences.MESSAGE_OFFER_FIELD] as Boolean
                        var amount = document[MyFirebaseReferences.MESSAGE_OFFER_AMOUNT_FIELD] as Long
                        var quantity = document[MyFirebaseReferences.MESSAGE_OFFER_QUANTITY_FIELD] as Long
                        var addressed = document[MyFirebaseReferences.MESSAGE_ADDRESSED_FIELD] as Boolean
                        var id = document[MyFirebaseReferences.MESSAGE_ID_FIELD] as String

                        Log.i("Database Manager AAAAAAA", "${amount}")
                        latestMessage = MessageModel(convoId, timestamp.toDate(), sender, message, offer, amount, quantity, addressed, id)
                    }
                }
                .addOnFailureListener {
                    Log.i("DatabaseManager", "Failed to retrieve latest message")
                }
                .await()

        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING LATEST MESSAGE")
        }

        latestMessage
    }

    suspend fun confirmOrder(orderId: String): Boolean = coroutineScope {
        val orderRef = db.collection(MyFirebaseReferences.ORDERS_COLLECTION)
        var success = false
        try {
            val job = orderRef
                .document(orderId)
                .update("isConfirmed", true)
                .addOnSuccessListener {
                    Log.d(TAG, "Order Confirmed")
                    success = true
                }
                .await()
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR CONFIRMING ORDER")
        }

        success
    }

    suspend fun closeListing(listingId: String): String = coroutineScope {
        val listingRef = db.collection(MyFirebaseReferences.LISTINGS_COLLECTION)
        val orderRef = db.collection(MyFirebaseReferences.ORDERS_COLLECTION)
        var result = "false"
        try {
            val pendingOrders = orderRef
                .whereEqualTo(MyFirebaseReferences.ORDER_LISTING_ID_FIELD, listingId)
                .get()
                .await()
            var isEmpty = true
            for (document in pendingOrders.documents) {
                if (!(document.get(MyFirebaseReferences.ORDER_IS_CONFIRMED_FIELD) as Boolean)) {
                    isEmpty = false
                    break
                }
            }
            if (isEmpty) {
                val job = listingRef
                    .document(listingId)
                    .update("isOpen", false)
                    .addOnSuccessListener {
                        Log.d(TAG, "Listing closed")
                        result = "true"
                    }
                    .await()
            } else if (pendingOrders.documents.size > 0) {
                result = "pending_orders"
                Log.d(TAG, "FAILED TO CLOSE LISTING WITH PENDING ORDER")
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR CLOSING LISTING")
        }

        result
    }

    suspend fun deleteListing(listingId: String): String = coroutineScope {
        val listingRef = db.collection(MyFirebaseReferences.LISTINGS_COLLECTION)
        val orderRef = db.collection(MyFirebaseReferences.ORDERS_COLLECTION)
        val convoRef = db.collection(MyFirebaseReferences.CONVERSATIONS_COLLECTION)
        var result = "false"
        try {
            val pendingOrders = orderRef
                .whereEqualTo(MyFirebaseReferences.ORDER_LISTING_ID_FIELD, listingId)
                .get()
                .await()
            var isEmpty = true
            for (document in pendingOrders.documents) {
                if (!(document.get(MyFirebaseReferences.ORDER_IS_CONFIRMED_FIELD) as Boolean)) {
                    isEmpty = false
                    break
                }
            }
            if (isEmpty) {
                Log.d(TAG, listingId)
                val job = launch (Dispatchers.IO) {
                     listingRef
                        .document(listingId)
                        .delete()
                        .await()
                    val deleteConversation = convoRef
                        .whereEqualTo(MyFirebaseReferences.LISTING_ID_FIELD, listingId)
                        .get()
                        .await()

                    for (document in deleteConversation.documents) {
                        document.reference.delete().await()
                    }
                }
                job.join()
                result = "true"
            } else if (pendingOrders.documents.size > 0) {
                result = "pending_orders"
                Log.d(TAG, "FAILED TO DELETE LISTING WITH PENDING ORDER")
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR DELETING LISTING")
        }

        result
    }

    suspend fun editListing(listingId: String, newStock: Long): Boolean = coroutineScope {
        val listingRef = db.collection(MyFirebaseReferences.LISTINGS_COLLECTION)
        val orderRef = db.collection(MyFirebaseReferences.ORDERS_COLLECTION)
        var result = false
        try {
            val job = listingRef
                .document(listingId)
                .update("stock", newStock)
                .addOnSuccessListener {
                    Log.d(TAG, "Listing stock count edited")
                    result = true
                }
                .await()
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR EDITING LISTING")
        }

        result
    }

    suspend fun getListingFromId(listingId: String): ListingModel? = coroutineScope {
        val listingRef = db.collection(MyFirebaseReferences.LISTINGS_COLLECTION)
        var listing: ListingModel? = null
        try {
            val listing_doc = listingRef
                .document(listingId)
                .get()
                .await()
            var photoArray = listing_doc[MyFirebaseReferences.PHOTOS_FIELD] as ArrayList<String>
            // Convert to Long
            var unitPrice = (listing_doc[MyFirebaseReferences.PRICE_FIELD] as Double).toLong()

            listing = ListingModel(
                listing_doc.id,
                listing_doc[MyFirebaseReferences.LISTING_IS_OPEN] as Boolean,
                listing_doc[MyFirebaseReferences.CATEGORY_FIELD].toString(),
                listing_doc[MyFirebaseReferences.DESCRIPTION_FIELD].toString(),
                listing_doc[MyFirebaseReferences.PRODUCT_NAME_FIELD].toString(),
                listing_doc[MyFirebaseReferences.LOCATION_FIELD].toString(),
                listing_doc[MyFirebaseReferences.SELLER_FIELD].toString(),
                listing_doc[MyFirebaseReferences.STOCK_FIELD] as Long,
                unitPrice as Long,
                photoArray
            )
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR EDITING LISTING")
        }
       listing
    }
}