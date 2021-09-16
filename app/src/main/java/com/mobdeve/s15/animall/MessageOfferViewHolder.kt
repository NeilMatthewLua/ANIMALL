package com.mobdeve.s15.animall

import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mobdeve.s15.animall.DatabaseManager.db
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class MessageOfferViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var usernameTv: TextView
    private var productTv: TextView
    private var messageTimeTv: TextView
    private var messageBtnDecline: Button
    private var messageBtnAccept: Button
    private var messageLinearLayout: LinearLayout
    private var actionButtonLayout: LinearLayout
    private var cardLayout: LinearLayout
    private var messageOfferCv: CardView
    private var listing: ListingModel? = null

    init {
        usernameTv = itemView.findViewById(R.id.messageUsernameTv)
        productTv = itemView.findViewById(R.id.messageListingTv)
        messageTimeTv = itemView.findViewById(R.id.messageTimeTv)
        messageLinearLayout = itemView.findViewById(R.id.messageLinearLayout)
        messageBtnDecline = itemView.findViewById(R.id.messageBtnDecline)
        messageBtnAccept = itemView.findViewById(R.id.messageBtnAccept)
        messageOfferCv = itemView.findViewById(R.id.messageOfferCv)
        cardLayout = itemView.findViewById(R.id.cardLayout)
        actionButtonLayout = itemView.findViewById(R.id.actionButtonLayout)
    }

    fun bindData(m: MessageModel) {
        usernameTv.text = m.sender

        val sdf3 =
                SimpleDateFormat("hh:mm:ss")
        sdf3.timeZone = TimeZone.getTimeZone("Asia/Singapore")

        var date = m.timestamp
        var dateString = sdf3.format(date)

        messageTimeTv.text = dateString

        Log.i("MessegeOfferVHolder", "${m.addressed}")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                val job = async {
                    listing = DatabaseManager.getConversationListing(m.convoId)
                }
                job.await()
                println(listing!!.name)
                productTv.text = listing!!.name
            }
        } catch (e: Exception) {
            Log.d("MessageOfferVHolder:", "ERROR RETRIEVING LISTING")
        }
    }

    fun sendMessage(model: MessageModel, accepted: Boolean) {
        val messageRef = db!!.collection(MyFirestoreReferences.MESSAGES_COLLECTION)
        val timeNow = Date()
        val loggedUser = Firebase.auth.currentUser!!
        val message = if (accepted) "The order/offer has been accepted" else "The your order/offer has been rejected"
        var messageId = UUID.randomUUID().toString()

        val data = hashMapOf(
            MyFirestoreReferences.MESSAGE_CONVO_FIELD to model.convoId,
            MyFirestoreReferences.MESSAGE_SENDER_FIELD to loggedUser.email,
            MyFirestoreReferences.MESSAGE_FIELD to message,
            MyFirestoreReferences.TIME_FIELD to timeNow,
            MyFirestoreReferences.MESSAGE_OFFER_FIELD to false,
            MyFirestoreReferences.MESSAGE_OFFER_QUANTITY_FIELD to -1,
            MyFirestoreReferences.MESSAGE_OFFER_AMOUNT_FIELD to -1,
            MyFirestoreReferences.MESSAGE_ADDRESSED_FIELD to true,
            MyFirestoreReferences.MESSAGE_ID_FIELD to messageId,
        )

        messageRef
            .add(data)
            .addOnSuccessListener {
                Log.d(
                    "MessageOfferViewHolder SUCCESS",
                    "DocumentSnapshot posted"
                )
                val indivMsgRef = db!!
                    .collection(MyFirestoreReferences.MESSAGES_COLLECTION)
                    .document(it.id)

                indivMsgRef.update(MyFirestoreReferences.MESSAGE_ADDRESSED_FIELD, true)

                //Subtract order from listing stock count
                if (accepted) {
                        val listingRef = db!!
                            .collection(MyFirestoreReferences.LISTINGS_COLLECTION)
                            .document(listing!!.id)

                        listingRef.update(MyFirestoreReferences.STOCK_FIELD, listing!!.stock - model.quantity)
                            .addOnSuccessListener {
                                Log.i("MessageOfferVHolder", "ReducedStock Count by ${model.quantity}")
                            }
                            .addOnFailureListener {
                                Log.i("MessageOfferVHolder", "Failed to recude stock.")
                            }
                }
            }
            .addOnFailureListener { e ->
                Log.w(
                    "MessageOfferViewHolder FAIL",
                    "Error adding document",
                    e
                )
            }
    }

    fun leftAlignText(m: MessageModel) {
        usernameTv.gravity = Gravity.LEFT
        messageTimeTv.gravity = Gravity.LEFT
        messageLinearLayout.gravity = Gravity.LEFT
        cardLayout.gravity = Gravity.LEFT

        //Only show action buttons of the offer/order has NOT yet been addressed
        if (!m.addressed) {
            actionButtonLayout.visibility = View.VISIBLE

            messageBtnDecline.setOnClickListener {
                actionButtonLayout.visibility = View.GONE
                sendMessage(m, false)
            }

            messageBtnAccept.setOnClickListener {
                actionButtonLayout.visibility = View.GONE
                sendMessage(m, true)
            }
        }
        else {
            actionButtonLayout.visibility = View.GONE
        }
    }

    fun rightAlignText(m: MessageModel) {
        usernameTv.gravity = Gravity.RIGHT
        messageTimeTv.gravity = Gravity.RIGHT
        messageLinearLayout.gravity = Gravity.RIGHT
        cardLayout.gravity = Gravity.RIGHT
        actionButtonLayout.visibility = View.GONE
    }

}