package com.mobdeve.s15.animall

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

object DataHelper {
    const val TAG = "FIRESTORE"
    val db = Firebase.firestore

    suspend fun initializeListingData(): ArrayList<ListingModel> = coroutineScope {
        val listingRef = db.collection(MyFirestoreReferences.LISTINGS_COLLECTION)
        val data = ArrayList<ListingModel>()
        try {
            val job = listingRef.get().await()
            for (document in job.documents) {
                if (document["isOpen"] as Boolean) {
                    var photoArray = document["photos"] as ArrayList<String>
                    // Convert to Long
                    var unitPrice = document["unitPrice"]
                    if (unitPrice is Long)
                        unitPrice = unitPrice.toDouble()
                    data.add(ListingModel(
                        true,
                        document["category"].toString(),
                        document["description"].toString(),
                        document["name"].toString(),
                        document["preferredLocation"].toString(),
                        document["seller"].toString(),
                        document["stock"] as Long,
                        unitPrice as Double,
                        photoArray
                    ))
                }
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING LISTINGS")
        }

        val messageRef = db.collection(MyFirestoreReferences.CONVERSATIONS_COLLECTION)
        val messageData = ArrayList<MessageModel>()

        messageData.add(0, MessageModel(
            Date(),
            "carlos_shi@dlsu.edu.ph",
            "Boy am I tired"
        ))

        messageData.add(0, MessageModel(
            Date(),
            "neil_lua@dlsu.edu.ph",
            "But why tho"
        ))

        val convData = hashMapOf(
            MyFirestoreReferences.RECEIPIENT_FIELD to "neil_lua@dlsu.edu.ph",
            MyFirestoreReferences.SENDER_FIELD to "carlos_shi@dlsu.edu.ph",
            MyFirestoreReferences.MESSAGES_FIELD to messageData,
            MyFirestoreReferences.LISTING_ID_FIELD to "7c72e01a-1582-463e-a685-138eda44c8e1",
            MyFirestoreReferences.LISTING_NAME_FIELD to "Miko Iino Poster",
            MyFirestoreReferences.LISTING_PHOTO_FIELD to "https://firebasestorage.googleapis.com/v0/b/animall-b7841.appspot.com/o/images%2F7c72e01a-1582-463e-a685-138eda44c8e1%2Fe239d3a3-18fa-4e75-ae3f-077c9e4b708e?alt=media&token=eb584791-a391-4551-aa80-cf5e222408e8"
        )

        try {
            val job = messageRef.add(convData).await()
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
            Log.i("DataHelper", "Getting conversations")
            for (document in receive_job.documents) {
                Log.i("DataHelper", "Got one")
                var messageArray = document[MyFirestoreReferences.MESSAGES_FIELD] as ArrayList<MessageModel>
                var receipientEmail = document[MyFirestoreReferences.RECEIPIENT_FIELD] as String
                var senderEmail = document[MyFirestoreReferences.SENDER_FIELD] as String
                val listingId =  document[MyFirestoreReferences.LISTING_ID_FIELD] as String
                val listingName =  document[MyFirestoreReferences.LISTING_NAME_FIELD] as String
                val listingPhoto = document[MyFirestoreReferences.LISTING_PHOTO_FIELD] as String
                data.add(
                    ConversationModel(
                        receipientEmail,
                        senderEmail,
                        messageArray,
                        listingId,
                        listingName,
                        listingPhoto
                    )
                )
            }
            val sent_job = conversationRef
                .whereEqualTo(MyFirestoreReferences.SENDER_FIELD, "carlos_shi@dlsu.edu.ph")
                .get()
                .await()
            Log.i("DataHelper", "Getting conversations")
            for (document in sent_job.documents) {
                Log.i("DataHelper", "Got one")
                var receipientEmail = document[MyFirestoreReferences.RECEIPIENT_FIELD] as String
                Log.i("DataHelper", "Got rece")
                var senderEmail = document[MyFirestoreReferences.SENDER_FIELD] as String
                Log.i("DataHelper", "Got send")
                val listingId =  document[MyFirestoreReferences.LISTING_ID_FIELD] as String
                Log.i("DataHelper", "Got id")
                val listingName =  document[MyFirestoreReferences.LISTING_NAME_FIELD] as String
                Log.i("DataHelper", "Got name")
                val listingPhoto = document[MyFirestoreReferences.LISTING_PHOTO_FIELD] as String
                Log.i("DataHelper", "Got photo")
                //https://medium.com/firebase-tips-tricks/how-to-map-an-array-of-objects-from-cloud-firestore-to-a-list-of-objects-122e579eae10
                var messageList = document[MyFirestoreReferences.MESSAGES_FIELD] as List<Map<String, Any>>
                Log.i("DataHelper", "Got messages")

                var messageArray = ArrayList<MessageModel>()
                messageList.forEach{
                    Log.i("sender", "${it["sender"].toString()}")
                    Log.i("sender", "${it["message"].toString()}")
                    var date = it["timestamp"] as Timestamp
                    Log.i("Time", "$date")

                    messageArray.add(MessageModel(
                        date.toDate(),
                        it["sender"].toString(),
                        it["message"].toString()
                    ))
                }
                data.add(
                    ConversationModel(
                        receipientEmail,
                        senderEmail,
                        messageArray,
                        listingId,
                        listingName,
                        listingPhoto
                    )
                )
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING CONVERSATIONS")
        }

        data
    }
}