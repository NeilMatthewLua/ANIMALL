package com.mobdeve.s15.animall

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_add_listing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

object DatabaseManager {
    const val TAG = "FIRESTORE"
    val db = Firebase.firestore
    val LISTING_LIMIT: Long = 5
    val PH_UPPER_LON: Double = 126.537423944
    val PH_UPPER_LAT: Double = 18.5052273625
    val PH_LOWER_LON: Double = 117.17427453
    val PH_LOWER_LAT: Double = 5.58100332277

    fun getInstance(): FirebaseFirestore {
        return db
    }

    suspend fun initializeListingData(): ArrayList<ListingModel> = coroutineScope {
        val listingRef = db.collection(MyFirestoreReferences.LISTINGS_COLLECTION)
        val data = ArrayList<ListingModel>()
        try {
            val job = listingRef.whereEqualTo(MyFirestoreReferences.LISTING_IS_OPEN, true).limit(LISTING_LIMIT).get().await()
            for (document in job.documents) {
                var photoArray = document[MyFirestoreReferences.PHOTOS_FIELD] as ArrayList<String>
                // Convert to Long
                var unitPrice = document[MyFirestoreReferences.PRICE_FIELD]
                if (unitPrice is Long)
                    unitPrice = unitPrice.toDouble()
                data.add(ListingModel(
                    document.reference.id,
                    document[MyFirestoreReferences.LISTING_IS_OPEN] as Boolean,
                    document[MyFirestoreReferences.CATEGORY_FIELD].toString(),
                    document[MyFirestoreReferences.DESCRIPTION_FIELD].toString(),
                    document[MyFirestoreReferences.PRODUCT_NAME_FIELD].toString(),
                    document[MyFirestoreReferences.LOCATION_FIELD].toString(),
                    document[MyFirestoreReferences.SELLER_FIELD].toString(),
                    document[MyFirestoreReferences.STOCK_FIELD] as Long,
                    unitPrice as Double,
                    photoArray,
                    document.id
                ))
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING LISTINGS")
        }

        data
    }

    suspend fun filteredListingData(filterOption: String, sortOption: String, searchOption: String, userCity: String, context: Context, lastDocId: String = ""): ArrayList<ListingModel> = coroutineScope {
        val listingRef = db.collection(MyFirestoreReferences.LISTINGS_COLLECTION)
        var data = ArrayList<ListingModel>()
        Log.d(TAG, searchOption)
        try {
            var job : Query? = null
            // Filter options (e.g. category)
            if (filterOption.isNotBlank()) {
                job = listingRef.whereEqualTo(MyFirestoreReferences.CATEGORY_FIELD, filterOption)
            }
            // Search options (e.g. search inputs)
            if (searchOption.isNotBlank()) {
                if (job != null) {
                    job = job.whereGreaterThanOrEqualTo(MyFirestoreReferences.NAME_FIELD, searchOption)
                             .whereLessThanOrEqualTo(MyFirestoreReferences.NAME_FIELD, searchOption+"\uF7FF")
                } else {
                    job = listingRef.whereGreaterThanOrEqualTo(MyFirestoreReferences.NAME_FIELD, searchOption)
                                    .whereLessThanOrEqualTo(MyFirestoreReferences.NAME_FIELD, searchOption+"\uF7FF")
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
            val result = job.whereEqualTo(MyFirestoreReferences.LISTING_IS_OPEN, true).limit(LISTING_LIMIT).get().await()
            for (document in result.documents) {
                if (document["isOpen"] as Boolean) {
                    var photoArray = document[MyFirestoreReferences.PHOTOS_FIELD] as ArrayList<String>
                    // Convert to Long
                    var unitPrice = document[MyFirestoreReferences.PRICE_FIELD]
                    if (unitPrice is Long)
                        unitPrice = unitPrice.toDouble()

                    // Default distance is max
                    var distanceFromUser: Double = Double.MAX_VALUE
                    val getLocationDistances = launch (Dispatchers.IO) {
                        var geocoderObj = Geocoder(context)
                        var cityNameUser = userCity
                        var cityNameListing = document[MyFirestoreReferences.LOCATION_FIELD] as String
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
                        document[MyFirestoreReferences.CATEGORY_FIELD].toString(),
                        document[MyFirestoreReferences.DESCRIPTION_FIELD].toString(),
                        document[MyFirestoreReferences.PRODUCT_NAME_FIELD].toString(),
                        document[MyFirestoreReferences.LOCATION_FIELD].toString(),
                        document[MyFirestoreReferences.SELLER_FIELD].toString(),
                        document[MyFirestoreReferences.STOCK_FIELD] as Long,
                        unitPrice as Double,
                        photoArray,
                        document.id,
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

            if (sortOption == MyFirestoreReferences.SORT_UNIT_PRICE_ASC) {
                data.sortBy { it.unitPrice }
            } else if (sortOption == MyFirestoreReferences.SORT_UNIT_PRICE_DSC) {
                data.sortByDescending { it.unitPrice }
            } else if (sortOption == MyFirestoreReferences.SORT_PROXIMITY) {
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
        val conversationRef = db.collection(MyFirestoreReferences.CONVERSATIONS_COLLECTION)
        val data = ArrayList<ConversationModel>()
        try {
            val receive_job = conversationRef
                        .whereEqualTo(MyFirestoreReferences.RECIPIENT_FIELD, email)
                        .get()
                        .await()
            Log.i("`DatabaseManager`", "Getting conversations")
            for (document in receive_job.documents) {
                Log.i("`DatabaseManager`", "Got one as recipient")
                var recipientEmail = document[MyFirestoreReferences.RECIPIENT_FIELD] as String
                var senderEmail = document[MyFirestoreReferences.SENDER_FIELD] as String
                val listingId =  document[MyFirestoreReferences.LISTING_ID_FIELD] as String
                val listingName =  document[MyFirestoreReferences.LISTING_NAME_FIELD] as String
                val listingPhoto = document[MyFirestoreReferences.LISTING_PHOTO_FIELD] as String
                val id = document.id

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

            val sent_job = conversationRef
                .whereEqualTo(MyFirestoreReferences.SENDER_FIELD, email)
                .get()
                .await()
            Log.i("`DatabaseManager`", "Getting conversations")
            for (document in sent_job.documents) {
                Log.i("`DatabaseManager`", "Got one as sender")
                var recipientEmail = document[MyFirestoreReferences.RECIPIENT_FIELD] as String
                var senderEmail = document[MyFirestoreReferences.SENDER_FIELD] as String
                val listingId =  document[MyFirestoreReferences.LISTING_ID_FIELD] as String
                Log.i("DBManager", listingId)
                val listingName =  document[MyFirestoreReferences.LISTING_NAME_FIELD] as String
                val listingPhoto = document[MyFirestoreReferences.LISTING_PHOTO_FIELD] as String
                val id = document.id
                Log.i("DBManager", id)

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
        val userRef = db.collection(MyFirestoreReferences.USERS_COLLECTION)
        var userName = ""
        try {
            val job = userRef.whereEqualTo(MyFirestoreReferences.EMAIL_FIELD, userEmail).get().await()
            for (document in job.documents) {
                userName = document[MyFirestoreReferences.NAME_FIELD] as String
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING USER NAME")
        }
        userName
    }

    suspend fun getUserCity(userEmail: String): String = coroutineScope {
        val userRef = db.collection(MyFirestoreReferences.USERS_COLLECTION)
        var city = ""
        try {
            val job = userRef.whereEqualTo(MyFirestoreReferences.EMAIL_FIELD, userEmail).get().await()
            for (document in job.documents) {
                city = document[MyFirestoreReferences.PREF_LOCATION_FIELD] as String
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING USER NAME")
        }
        city
    }

    suspend fun getUserListings(userEmail: String): ArrayList<ListingModel> = coroutineScope {
        val listingRef = db.collection(MyFirestoreReferences.LISTINGS_COLLECTION)
        val data = ArrayList<ListingModel>()
        try {
            val job = listingRef.whereEqualTo(MyFirestoreReferences.SELLER_FIELD, userEmail).get().await()
            for (document in job.documents) {
                var photoArray = document[MyFirestoreReferences.PHOTOS_FIELD] as ArrayList<String>
                // Convert to Long
                var unitPrice = document[MyFirestoreReferences.PRICE_FIELD]
                if (unitPrice is Long)
                    unitPrice = unitPrice.toDouble()
                data.add(ListingModel(
                    document.reference.id,
                    document[MyFirestoreReferences.LISTING_IS_OPEN] as Boolean,
                    document[MyFirestoreReferences.CATEGORY_FIELD].toString(),
                    document[MyFirestoreReferences.DESCRIPTION_FIELD].toString(),
                    document[MyFirestoreReferences.PRODUCT_NAME_FIELD].toString(),
                    document[MyFirestoreReferences.LOCATION_FIELD].toString(),
                    document[MyFirestoreReferences.SELLER_FIELD].toString(),
                    document[MyFirestoreReferences.STOCK_FIELD] as Long,
                    unitPrice as Double,
                    photoArray,
                    document.id
                ))
            }
            Log.d("FIREBASE:", job.documents.size.toString())
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING LISTINGS")
        }
        data
    }

    suspend fun getUserOrders(customerEmail: String): ArrayList<OrderModel> = coroutineScope {
        val orderRef = db.collection(MyFirestoreReferences.ORDERS_COLLECTION)
        val data = ArrayList<OrderModel>()
        try {
            val job = orderRef.whereEqualTo(MyFirestoreReferences.ORDER_CUSTOMER_ID_FIELD, customerEmail).get().await()
            for (document in job.documents) {
                // Convert to Long
                var unitPrice = document[MyFirestoreReferences.ORDER_SOLD_PRICE_FIELD]
                if (unitPrice is Long)
                    unitPrice = unitPrice.toDouble()
                data.add(OrderModel(
                    document.reference.id,
                    document[MyFirestoreReferences.ORDER_CUSTOMER_ID_FIELD] as String,
                    document[MyFirestoreReferences.ORDER_LISTING_ID_FIELD] as String,
                    document[MyFirestoreReferences.ORDER_LISTING_NAME_FIELD] as String,
                    document[MyFirestoreReferences.ORDER_PHOTOS_ID_FIELD] as String,
                    document[MyFirestoreReferences.ORDER_QUANTITY_FIELD] as Long,
                    unitPrice as Double,
                    document[MyFirestoreReferences.ORDER_IS_CONFIRMED_FIELD] as Boolean
                ))
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", e.toString())
        }

        data
    }

    suspend fun getUserViaEmail(userEmail: String): UserModel? = coroutineScope {
        val conversationRef = db.collection(MyFirestoreReferences.USERS_COLLECTION)
        var user: UserModel? = null
        try {
            Log.i("FIREBASE", "UserID ${userEmail}")

            //WHERE DocumentID (PK) is userId
            val job = conversationRef
                .whereEqualTo(MyFirestoreReferences.EMAIL_FIELD, userEmail)
                .get()
                .await()
            Log.i("`DatabaseManager`", "Getting the user")
            for (document in job.documents) {
                var userEmail = document[MyFirestoreReferences.EMAIL_FIELD] as String
                var userName = document[MyFirestoreReferences.NAME_FIELD] as String
                var userPrefLoc = document[MyFirestoreReferences.PREF_LOCATION_FIELD] as String
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
        val conversationRef = db.collection(MyFirestoreReferences.CONVERSATIONS_COLLECTION)
        var conversation: ConversationModel? = null

        try {
            Log.i("FIREBASE", "UserID ${loggedUser} listingId ${listingId}")

            val job = conversationRef
                .whereEqualTo(MyFirestoreReferences.SENDER_FIELD, loggedUser)
                .whereEqualTo(MyFirestoreReferences.LISTING_ID_FIELD, listingId)
                .get()
                .await()

            Log.i("`DatabaseManager`", "Checking for conversations")
            for (document in job.documents) {
                conversation = ConversationModel(
                    document[MyFirestoreReferences.RECIPIENT_FIELD] as String,
                    document[MyFirestoreReferences.SENDER_FIELD] as String,
                    document[MyFirestoreReferences.LISTING_ID_FIELD] as String,
                    document[MyFirestoreReferences.LISTING_NAME_FIELD] as String,
                    document[MyFirestoreReferences.LISTING_PHOTO_FIELD] as String,
                    document.id,
                )
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING USER")
        }

        conversation
    }

    suspend fun getLatestMessage(convoId: String): MessageModel? = coroutineScope {
        val messageRef = db.collection(MyFirestoreReferences.MESSAGES_COLLECTION)
        var latestMessage: MessageModel? = null

        try {
            Log.i("DatabaseManager", "conversation ID to retrieve: ${convoId} ${convoId.length}")

            messageRef
                .whereEqualTo(MyFirestoreReferences.MESSAGE_CONVO_FIELD, convoId)
                .orderBy(MyFirestoreReferences.TIME_FIELD, Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    Log.i("DatabaseManager", "Document size: ${documents.size()}")
                    for (document in documents) {
                        Log.d("DatabaseManager", "${document.id} => ${document.data}")
                        var convoId = document[MyFirestoreReferences.MESSAGE_CONVO_FIELD] as String
                        var timestamp = document[MyFirestoreReferences.TIME_FIELD] as Timestamp
                        var sender = document[MyFirestoreReferences.MESSAGE_SENDER_FIELD] as String
                        var message = document[MyFirestoreReferences.MESSAGE_FIELD] as String

                        latestMessage = MessageModel(convoId, timestamp.toDate(), sender, message)
                    }
                }
                .addOnFailureListener {
                    Log.i("DatabaseManager", "Failed to retrieve latest message")
                }
                .await()

        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING LATEST MESSAGE")
        }

//        Log.i("Done", "${latestMessage == null}")
        latestMessage
    }

    suspend fun confirmOrder(orderId: String): Boolean = coroutineScope {
        val orderRef = db.collection(MyFirestoreReferences.ORDERS_COLLECTION)
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
        val listingRef = db.collection(MyFirestoreReferences.LISTINGS_COLLECTION)
        val orderRef = db.collection(MyFirestoreReferences.ORDERS_COLLECTION)
        var result = "false"
        try {
            val pendingOrders = orderRef
                .whereEqualTo("listingId", listingId)
                .whereEqualTo("isConfirmed", false)
                .get()
                .await()
            if (pendingOrders.documents.size == 0) {
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
        val listingRef = db.collection(MyFirestoreReferences.LISTINGS_COLLECTION)
        val orderRef = db.collection(MyFirestoreReferences.ORDERS_COLLECTION)
        var result = "false"
        try {
            val pendingOrders = orderRef
                .whereEqualTo("listingId", listingId)
                .whereEqualTo("isConfirmed", false)
                .get()
                .await()
            if (pendingOrders.documents.size == 0) {
                val job = listingRef
                    .document(listingId)
                    .delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "Listing deleted")
                        result = "true"
                    }
                    .await()
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
        val listingRef = db.collection(MyFirestoreReferences.LISTINGS_COLLECTION)
        val orderRef = db.collection(MyFirestoreReferences.ORDERS_COLLECTION)
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
}