package com.mobdeve.s15.animall

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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

object DatabaseManager {
    const val TAG = "FIRESTORE"
    val db = Firebase.firestore

    fun getInstance(): FirebaseFirestore {
        return db
    }

    suspend fun initializeListingData(): ArrayList<ListingModel> = coroutineScope {
        val listingRef = db.collection(MyFirestoreReferences.LISTINGS_COLLECTION)
        val data = ArrayList<ListingModel>()
        try {
            val job = listingRef.get().await()
            for (document in job.documents) {
                if (document["isOpen"] as Boolean) {
                    var photoArray = document[MyFirestoreReferences.PHOTOS_FIELD] as ArrayList<String>
                    // Convert to Long
                    var unitPrice = document[MyFirestoreReferences.PRICE_FIELD]
                    if (unitPrice is Long)
                        unitPrice = unitPrice.toDouble()
                    data.add(ListingModel(
                        true,
                        document[MyFirestoreReferences.CATEGORY_FIELD].toString(),
                        document[MyFirestoreReferences.DESCRIPTION_FIELD].toString(),
                        document[MyFirestoreReferences.PRODUCT_NAME_FIELD].toString(),
                        document[MyFirestoreReferences.LOCATION_FIELD].toString(),
                        document[MyFirestoreReferences.SELLER_FIELD].toString(),
                        document[MyFirestoreReferences.STOCK_FIELD] as Long,
                        unitPrice as Double,
                        photoArray
                    ))
                }
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING LISTINGS")
        }

        val convoID = "ppM1zG50BJypCp3frVEu"

        //TODO Currently placed to add conversation messages, remove when no longer needed
        val convoRef = db.collection(MyFirestoreReferences.CONVERSATIONS_COLLECTION).document(convoID)

        val convData = hashMapOf(
            MyFirestoreReferences.RECEIPIENT_FIELD to "neil_lua@dlsu.edu.ph",
            MyFirestoreReferences.SENDER_FIELD to "carlos_shi@dlsu.edu.ph",
            MyFirestoreReferences.LISTING_ID_FIELD to "7c72e01a-1582-463e-a685-138eda44c8e1",
            MyFirestoreReferences.LISTING_NAME_FIELD to "Miko Iino Poster",
            MyFirestoreReferences.LISTING_PHOTO_FIELD to "https://firebasestorage.googleapis.com/v0/b/animall-b7841.appspot.com/o/images%2F7c72e01a-1582-463e-a685-138eda44c8e1%2Fe239d3a3-18fa-4e75-ae3f-077c9e4b708e?alt=media&token=eb584791-a391-4551-aa80-cf5e222408e8"
        )

        try {
            convoRef
                .set(convData)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot written, will add messages now")

                    var messageRef = db.collection(MyFirestoreReferences.MESSAGES_COLLECTION)

                    //TODO Fate convoId, but check
                    messageRef
                        .add(
                            MessageModel(
                                convoID,
                                Date(),
                                "carlos_shi@dlsu.edu.ph",
                                "Boy am I tired"
                            )
                        )

                    messageRef
                        .add(
                            MessageModel(
                                convoID,
                                Date(),
                                "neil_lua@dlsu.edu.ph",
                                "Aren't we all"
                            )
                        )
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)

                }.await()

            Log.i("FIREBASE:", "SUCCESS UPLOAD MESSAGE")
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR UPLOADING MESSAGE")
        }

        data
    }

    suspend fun initializeConversationData(): ArrayList<ConversationModel> = coroutineScope {
        val conversationRef = db.collection(MyFirestoreReferences.CONVERSATIONS_COLLECTION)
        val data = ArrayList<ConversationModel>()
        try {
            //TODO use logged in email, for now use carlos_shi
            val receive_job = conversationRef
                        .whereEqualTo(MyFirestoreReferences.RECEIPIENT_FIELD, "carlos_shi@dlsu.edu.ph")
                        .get()
                        .await()
            Log.i("`DatabaseManager`", "Getting conversations")
            for (document in receive_job.documents) {
                Log.i("`DatabaseManager`", "Got one")
                var receipientEmail = document[MyFirestoreReferences.RECEIPIENT_FIELD] as String
                var senderEmail = document[MyFirestoreReferences.SENDER_FIELD] as String
                val listingId =  document[MyFirestoreReferences.LISTING_ID_FIELD] as String
                val listingName =  document[MyFirestoreReferences.LISTING_NAME_FIELD] as String
                val listingPhoto = document[MyFirestoreReferences.LISTING_PHOTO_FIELD] as String
                val id = document.id

                data.add(
                    ConversationModel(
                        receipientEmail,
                        senderEmail,
                        listingId,
                        listingName,
                        listingPhoto,
                        id
                    )
                )
            }

            val sent_job = conversationRef
                .whereEqualTo(MyFirestoreReferences.SENDER_FIELD, "carlos_shi@dlsu.edu.ph")
                .get()
                .await()
            Log.i("`DatabaseManager`", "Getting conversations")
            for (document in sent_job.documents) {
                var receipientEmail = document[MyFirestoreReferences.RECEIPIENT_FIELD] as String
                var senderEmail = document[MyFirestoreReferences.SENDER_FIELD] as String
                val listingId =  document[MyFirestoreReferences.LISTING_ID_FIELD] as String
                val listingName =  document[MyFirestoreReferences.LISTING_NAME_FIELD] as String
                val listingPhoto = document[MyFirestoreReferences.LISTING_PHOTO_FIELD] as String
                val id = document.id

                data.add(
                    ConversationModel(
                        receipientEmail,
                        senderEmail,
                        listingId,
                        listingName,
                        listingPhoto,
                        id
                    )
                )
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING CONVERSATIONS")
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
                    document[MyFirestoreReferences.LISTING_IS_OPEN] as Boolean,
                    document[MyFirestoreReferences.CATEGORY_FIELD].toString(),
                    document[MyFirestoreReferences.DESCRIPTION_FIELD].toString(),
                    document[MyFirestoreReferences.PRODUCT_NAME_FIELD].toString(),
                    document[MyFirestoreReferences.LOCATION_FIELD].toString(),
                    document[MyFirestoreReferences.SELLER_FIELD].toString(),
                    document[MyFirestoreReferences.STOCK_FIELD] as Long,
                    unitPrice as Double,
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
                    document[MyFirestoreReferences.ORDER_CUSTOMER_ID_FIELD] as String,
                    document[MyFirestoreReferences.ORDER_LISTING_ID_FIELD] as String,
                    document[MyFirestoreReferences.ORDER_LISTING_NAME_FIELD] as String,
                    document[MyFirestoreReferences.ORDER_PHOTOS_ID_FIELD] as String,
                    document[MyFirestoreReferences.ORDER_QUANTITY_FIELD] as Long,
                    unitPrice as Double
                ))
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", e.toString())
        }

        data
    }

    suspend fun getUser(userId: String): UserModel? = coroutineScope {
        val conversationRef = db.collection(MyFirestoreReferences.USERS_COLLECTION)
        var user: UserModel? = null
        try {
            Log.i("FIREBASE", "UserID ${userId}")
            //WHERE DocumentID (PK) is userId
            val job = conversationRef
                .whereEqualTo(MyFirestoreReferences.EMAIL_FIELD, userId)
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

    suspend fun getLatestMessage(convoId: String): MessageModel? = coroutineScope {
        val messageRef = db.collection(MyFirestoreReferences.MESSAGES_COLLECTION)
        var latestMessage: MessageModel? = null

        try {

            Log.i("manager", convoId)

            val job = messageRef
                .whereEqualTo(MyFirestoreReferences.MESSAGE_CONVO_FIELD, convoId)
                .orderBy(MyFirestoreReferences.TIME_FIELD, Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            Log.i("manager", "${job.documents.size}")
            for (document in job.documents) {
                var convoId = document[MyFirestoreReferences.MESSAGE_CONVO_FIELD] as String
                var timestamp = document[MyFirestoreReferences.TIME_FIELD] as Timestamp
                Log.i("manager", "${document.data}")
                var sender = document[MyFirestoreReferences.MESSAGE_SENDER_FIELD] as String
                var message = document[MyFirestoreReferences.MESSAGE_FIELD] as String

                latestMessage = MessageModel(convoId, timestamp.toDate(), sender, message)

            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING LATEST MESSAGE")
        }

        latestMessage
    }
}