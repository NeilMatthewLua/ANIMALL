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
    private var messageQuantityTotalTv: TextView
    private var messageQuantityTv: TextView
    private var messageMessageTv: TextView
    private var messageNewAmountTv: TextView
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
        messageQuantityTv = itemView.findViewById(R.id.messageQuantityTv)
        messageNewAmountTv = itemView.findViewById(R.id.messageNewAmountTv)
        messageQuantityTotalTv = itemView.findViewById(R.id.messageQuantityTotalTv)
        messageMessageTv = itemView.findViewById(R.id.messageMessageTv)
    }

    fun bindData(m: MessageModel) {
        usernameTv.text = m.sender

        val sdf3 =
                SimpleDateFormat("hh:mm:ss")
        sdf3.timeZone = TimeZone.getTimeZone("Asia/Singapore")

        var date = m.timestamp
        var dateString = sdf3.format(date)

        messageTimeTv.text = dateString

        Log.i("MessageOverVHolder", m.quantity.toString())
        Log.i("MessageOverVHolder", m.offerPrice.toString())

        messageQuantityTv.text = m.quantity.toString()
        messageNewAmountTv.text = m.offerPrice.toString()

        val total = m.quantity * m.offerPrice
        messageQuantityTotalTv.text = "Total: ₱${total}"


        Log.i("MessegeOfferVHolder", "${m.addressed}")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                val job = async {
                    listing = DatabaseManager.getConversationListing(m.convoId)
                }
                job.await()
                println(listing!!.name)
                productTv.text = listing!!.name

                if (listing!!.unitPrice != m.offerPrice) {
                    messageMessageTv.text = "New Offer"
                }
                else {
                    messageMessageTv.text = "New Order"
                }
            }
        } catch (e: Exception) {
            Log.d("MessageOfferVHolder:", "ERROR RETRIEVING LISTING")
        }
    }

    fun sendMessage(model: MessageModel, accepted: Boolean, order: Boolean) {
        val messageRef = db!!.collection(MyFirebaseReferences.MESSAGES_COLLECTION)
        val timeNow = Date()
        val loggedUser = Firebase.auth.currentUser!!
        val orderOffer = if (order) "order" else "offer"

        val listingRef = db!!
            .collection(MyFirebaseReferences.LISTINGS_COLLECTION)
            .document(listing!!.listingId)

        val message = if (listing!!.stock - model.quantity >= 0)
                            if (accepted) "The $orderOffer has been accepted"
                            else "The your $orderOffer has been declined"
                      else "The listing quantity has since then been changed. Please try again"
        var messageId = UUID.randomUUID().toString()

        val data = hashMapOf(
            MyFirebaseReferences.MESSAGE_CONVO_FIELD to model.convoId,
            MyFirebaseReferences.MESSAGE_SENDER_FIELD to loggedUser.email,
            MyFirebaseReferences.MESSAGE_FIELD to message,
            MyFirebaseReferences.TIME_FIELD to timeNow,
            MyFirebaseReferences.MESSAGE_OFFER_FIELD to false,
            MyFirebaseReferences.MESSAGE_OFFER_QUANTITY_FIELD to -1,
            MyFirebaseReferences.MESSAGE_OFFER_AMOUNT_FIELD to -1,
            MyFirebaseReferences.MESSAGE_ADDRESSED_FIELD to true,
            MyFirebaseReferences.MESSAGE_ID_FIELD to messageId,
        )

            messageRef
                .add(data)
                .addOnSuccessListener {
                    Log.d(
                        "MessageOfferViewHolder SUCCESS old message here",
                        "${model.id}"
                    )
                    val indivMsgRef = db!!
                        .collection(MyFirebaseReferences.MESSAGES_COLLECTION)
                        .document(model.id)

                    indivMsgRef
                        .update(MyFirebaseReferences.MESSAGE_ADDRESSED_FIELD, true)
                        .addOnFailureListener {
                            println("Done Updating")
                        }
                    if(listing!!.stock - model.quantity >= 0) {
                        //Subtract order from listing stock count
                        if (accepted) {
                                listingRef.update(MyFirebaseReferences.STOCK_FIELD, listing!!.stock - model.quantity)
                                    .addOnSuccessListener {
                                        Log.i("MessageOfferVHolder", "ReducedStock Count by ${model.quantity}")
                                    }
                                    .addOnFailureListener {
                                        Log.i("MessageOfferVHolder", "Failed to reduce stock.")
                                    }

                                val remainder = listing!!.stock - model.quantity
                                if(remainder == 0.toLong()) {
                                    listingRef.update(MyFirebaseReferences.LISTING_IS_OPEN, false)
                                        .addOnSuccessListener {
                                            Log.i("MessageOfferVHolder", "ReducedStock Count by ${model.quantity}")
                                        }
                                        .addOnFailureListener {
                                            Log.i("MessageOfferVHolder", "Failed to reduce stock.")
                                        }
                                }
                                val orderId = UUID.randomUUID().toString()

                                val orderData = hashMapOf(
                                    MyFirebaseReferences.ORDER_ID_FIELD to orderId,
                                    MyFirebaseReferences.ORDER_CUSTOMER_ID_FIELD to model.sender,
                                    MyFirebaseReferences.ORDER_LISTING_ID_FIELD to listing!!.listingId,
                                    MyFirebaseReferences.ORDER_LISTING_NAME_FIELD to listing!!.name,
                                    MyFirebaseReferences.ORDER_PHOTOS_ID_FIELD to listing!!.photos[0],
                                    MyFirebaseReferences.ORDER_QUANTITY_FIELD to model.quantity,
                                    MyFirebaseReferences.ORDER_SOLD_PRICE_FIELD to model.offerPrice,
                                    MyFirebaseReferences.ORDER_IS_CONFIRMED_FIELD to false,
                                )

                                val orderRef = db!!
                                    .collection(MyFirebaseReferences.ORDERS_COLLECTION)
                                    .document(orderId)

                                orderRef
                                    .set(orderData)
                                    .addOnSuccessListener {
                                        Log.i("MessageOfferVHolder", "Order made")
                                    }
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
                if (listing!!.unitPrice != m.offerPrice) {
                    sendMessage(m, false, false)
                }
                else {
                    sendMessage(m, false, true)
                }
            }

            messageBtnAccept.setOnClickListener {
                actionButtonLayout.visibility = View.GONE
                if (listing!!.unitPrice != m.offerPrice) {
                    sendMessage(m, true, false)
                }
                else {
                    sendMessage(m, true, true)
                }
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