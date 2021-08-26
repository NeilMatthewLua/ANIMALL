package com.mobdeve.s15.animall

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

object DatabaseManager {
    const val TAG = "FIRESTORE"
    val db = Firebase.firestore

    suspend fun initializeData(): ArrayList<ListingModel> = coroutineScope {
        val listingRef = db.collection("listings")
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

        data
    }

    suspend fun getUserName(userEmail: String): String = coroutineScope {
        val userRef = db.collection("users")
        var userName = ""
        try {
            val job = userRef.whereEqualTo("email", userEmail).get().await()
            for (document in job.documents) {
                userName = document["name"] as String
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING USER NAME")
        }
        userName
    }

    suspend fun getUserListings(userEmail: String): ArrayList<ListingModel> = coroutineScope {
        val listingRef = db.collection("listings")
        val data = ArrayList<ListingModel>()
        try {
            val job = listingRef.whereEqualTo("seller", userEmail).get().await()
            for (document in job.documents) {
                Log.d("FIREBASE:", document["name"].toString())
                var photoArray = document["photos"] as ArrayList<String>
                // Convert to Long
                var unitPrice = document["unitPrice"]
                if (unitPrice is Long)
                    unitPrice = unitPrice.toDouble()
                data.add(ListingModel(
                    document["isOpen"] as Boolean,
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
            Log.d("FIREBASE:", job.documents.size.toString())
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING LISTINGS")
        }
        data
    }

    suspend fun getUserOrders(customerEmail: String): ArrayList<OrderModel> = coroutineScope {
        val listingRef = db.collection("orders")
        val data = ArrayList<OrderModel>()
        try {
            val job = listingRef.whereEqualTo("customerId", customerEmail).get().await()
            for (document in job.documents) {
                // Convert to Long
                var unitPrice = document["soldPrice"]
                if (unitPrice is Long)
                    unitPrice = unitPrice.toDouble()
                data.add(OrderModel(
                    document["customerId"] as String,
                    document["listingId"] as String,
                    document["listingName"] as String,
                    document["photosId"] as String,
                    document["quantity"] as Long,
                    unitPrice as Double
                ))
            }
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING LISTINGS")
        }

        data
    }
}