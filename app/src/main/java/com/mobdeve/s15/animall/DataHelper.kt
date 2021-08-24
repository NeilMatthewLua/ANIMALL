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

object DataHelper {
    const val TAG = "FIRESTORE"
    val db = Firebase.firestore

    suspend fun initializeData(): ArrayList<ListingModel> = coroutineScope {
        val listingRef = db.collection("listings")
        val data = ArrayList<ListingModel>()
        try {
            val job = listingRef.get().await()
            for (document in job.documents) {
                var photoArray = document["photos"] as ArrayList<String>
                // Convert to Long
                var unitPrice = document["unitPrice"]
                if (unitPrice is Long)
                    unitPrice = unitPrice.toDouble()
                data.add(ListingModel(
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
        } catch (e: Exception) {
            Log.d("FIREBASE:", "ERROR RETRIEVING LISTINGS")
        }

        data
    }
}