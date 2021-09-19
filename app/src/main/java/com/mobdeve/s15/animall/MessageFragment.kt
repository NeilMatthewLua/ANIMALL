package com.mobdeve.s15.animall

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_message.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MessageFragment : Fragment() {
    // Replacement of the base adapter view
    private lateinit var myFirestoreRecyclerAdapter: MessageAdapter

    // DB reference
    private var db: FirebaseFirestore? = null

    private val viewModel: MessageSharedViewModel by activityViewModels()
    private var listing: ListingModel? = null
    private lateinit var loggedUser: FirebaseUser
    private lateinit var convoModel: ConversationModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        Log.d("MessageFragment ", "OnViewWCreated")
        viewModel.getListingData().observe(viewLifecycleOwner, {
            lifecycleScope.launch {
                val convoInit = async(Dispatchers.IO) {
                    Log.d("MessageFragment ", it.listingName)
                    db = DatabaseManager.getInstance()

                    val query = db!!
                        .collection(MyFirebaseReferences.MESSAGES_COLLECTION)
                        .whereEqualTo(
                            MyFirebaseReferences.MESSAGE_CONVO_FIELD,
                            it.id
                        )
                        .orderBy(MyFirebaseReferences.TIME_FIELD)

                    convoModel = it
                    loggedUser = Firebase.auth.currentUser!!

                    val getListing = launch(Dispatchers.IO) {
                        listing =
                            DatabaseManager.getListingFromId(viewModel.getListingData().value!!.listingId)
                    }
                    getListing.join()

                    Log.d("MESSAGE FRAGMENT", listing!!.description)

                    withContext(Dispatchers.Main) {
                        // Hide offer/order buttons if recipient
                        if (convoModel.recipientEmail == loggedUser.email) {
                            linearLayout2.visibility = View.GONE
                            makeOfferBtn.visibility = View.GONE
                            makeOrderBtn.visibility = View.GONE
                        }
                    }

                    val options: FirestoreRecyclerOptions<MessageModel> =
                        FirestoreRecyclerOptions.Builder<MessageModel>()
                            .setQuery(query, MessageModel::class.java)
                            .build()

                    myFirestoreRecyclerAdapter =
                        MessageAdapter(options, requireContext(), loggedUser!!.email!!)
                    Log.i("MessageFragment", "Adapter Adapted")

                    withContext(Dispatchers.Main) {
                        val linearLayoutManager = LinearLayoutManager(requireContext())
                        messageRecyclerView!!.layoutManager = linearLayoutManager

                        myFirestoreRecyclerAdapter.registerAdapterDataObserver(
                            MyScrollToBottomObserver(
                                messageRecyclerView,
                                myFirestoreRecyclerAdapter
                            )
                        )

                        Log.d("MessageFragment ", "OnViewWCreated2")
                        messageRecyclerView.adapter = myFirestoreRecyclerAdapter

                        Log.d("MessageFragment ", "OnViewWCreated3")
                        sendMessageBtn.setOnClickListener { view ->
                            Log.i("Messages", "sending a message ON 1")
                            if (messageEtv!!.text.toString().isNotEmpty()) {
                                sendMessage(it, viewModel.getIsFirst(), false, false, -1, -1)
                            }
                        }

                        // Listen to order result
                        requireActivity().supportFragmentManager
                            .setFragmentResultListener(
                                CustomOfferDialogFragment.MODAL_ORDER_RESULT,
                                viewLifecycleOwner
                            ) { key, bundle ->
                                val result =
                                    bundle.getString(CustomOfferDialogFragment.MODAL_SUCCESS_KEY)
                                val listingPrice =
                                    bundle.getLong(CustomOfferDialogFragment.MODAL_LISTING_PRICE_KEY)
                                val quantityOrdered =
                                    bundle.getLong(CustomOfferDialogFragment.MODAL_QUANTITY_ORDERED_KEY)
                                if (result == "ok") {
                                    sendMessage(
                                        it,
                                        viewModel.getIsFirst(),
                                        true,
                                        true,
                                        listingPrice,
                                        quantityOrdered
                                    )
                                }
                            }

                        // Listen to offer result
                        requireActivity().supportFragmentManager
                            .setFragmentResultListener(
                                CustomOfferDialogFragment.MODAL_OFFER_RESULT,
                                viewLifecycleOwner
                            ) { key, bundle ->
                                val result =
                                    bundle.getString(CustomOfferDialogFragment.MODAL_SUCCESS_KEY)
                                val listingPrice =
                                    bundle.getLong(CustomOfferDialogFragment.MODAL_LISTING_PRICE_KEY)
                                val quantityOrdered =
                                    bundle.getLong(CustomOfferDialogFragment.MODAL_QUANTITY_ORDERED_KEY)
                                if (result == "ok") {
                                    sendMessage(
                                        it,
                                        viewModel.getIsFirst(),
                                        true,
                                        false,
                                        listingPrice,
                                        quantityOrdered
                                    )
                                }
                            }

                        myFirestoreRecyclerAdapter!!.startListening()
                    }

                }
                convoInit.await()
            }
        })
        makeOfferBtn.setOnClickListener {
            val dialog = CustomOfferDialogFragment()
            // optionally pass arguments to the dialog fragment
            var args = Bundle()
            args.putString(CustomOfferDialogFragment.MODAL_LISTING_ID_KEY, listing?.listingId)
            args.putString(
                CustomOfferDialogFragment.MODAL_TYPE_KEY,
                CustomOfferDialogFragment.MODAL_OFFER
            )
            args.putString(CustomOfferDialogFragment.MODAL_LISTING_NAME_KEY, listing?.name)
            args.putLong(CustomOfferDialogFragment.MODAL_LISTING_STOCK_KEY, listing?.stock!!)
            dialog.arguments = args
            dialog.show(requireActivity().supportFragmentManager, "Make Offer")
        }

        makeOrderBtn.setOnClickListener {
            val dialog = CustomOfferDialogFragment()
            // optionally pass arguments to the dialog fragment
            var args = Bundle()
            args.putString(CustomOfferDialogFragment.MODAL_LISTING_ID_KEY, listing?.listingId)
            args.putString(
                CustomOfferDialogFragment.MODAL_TYPE_KEY,
                CustomOfferDialogFragment.MODAL_ORDER
            )
            args.putString(CustomOfferDialogFragment.MODAL_LISTING_NAME_KEY, listing?.name)
            args.putLong(CustomOfferDialogFragment.MODAL_LISTING_PRICE_KEY, listing?.unitPrice!!)
            args.putLong(CustomOfferDialogFragment.MODAL_LISTING_STOCK_KEY, listing?.stock!!)
            dialog.arguments = args
            dialog.show(requireActivity().supportFragmentManager, "Make Order")
        }
    }

    fun sendMessage(
        convo: ConversationModel,
        isFirst: Boolean,
        offer: Boolean,
        order: Boolean,
        price: Long,
        quantity: Long
    ) {
        val message = messageEtv!!.text.toString()
        val messageId = UUID.randomUUID().toString()
        val timeNow = Date()

        if (isFirst) {
//            val convoID = UUID.randomUUID().toString()
            val convoHash = hashMapOf(
                MyFirebaseReferences.RECIPIENT_FIELD to convo.recipientEmail,
                MyFirebaseReferences.SENDER_FIELD to loggedUser.email!!,
                MyFirebaseReferences.LISTING_ID_FIELD to convo.listingId,
                MyFirebaseReferences.LISTING_NAME_FIELD to convo.listingName,
                MyFirebaseReferences.LISTING_PHOTO_FIELD to convo.listingPhoto,
                MyFirebaseReferences.CONVO_ID_FIELD to convo.id,
                MyFirebaseReferences.CONVO_TIMESTAMP_FIELD to Date(),
                MyFirebaseReferences.CONVO_USERS_FIELD to arrayListOf(
                    loggedUser.email!!,
                    convo.recipientEmail
                ),
                MyFirebaseReferences.CONVO_MESSAGE_FIELD to "",

                )

            for ((key, value) in convoHash.entries) {
                Log.i("ViewListingFragment", "${key} => ${value}")
            }

            db!!.collection(MyFirebaseReferences.CONVERSATIONS_COLLECTION)
                .document(convo.id)
                .set(convoHash)
        }

        Log.i("MEssageFragment Convo.id", "${convo.id}")
        Log.i("MEssageFragment ConvoModel.id", "${convoModel.id}")

        println(quantity)
        println(price)
        // Ready the values of the message
        val data = hashMapOf(
            MyFirebaseReferences.MESSAGE_CONVO_FIELD to convo.id,
            MyFirebaseReferences.MESSAGE_SENDER_FIELD to loggedUser.email,
            MyFirebaseReferences.MESSAGE_CONVO_FIELD to convoModel.id,
            MyFirebaseReferences.MESSAGE_FIELD to if (offer) if (order) "New Order" else "New Offer" else message,
            MyFirebaseReferences.TIME_FIELD to timeNow,
            MyFirebaseReferences.MESSAGE_OFFER_FIELD to offer,
            MyFirebaseReferences.MESSAGE_OFFER_QUANTITY_FIELD to if (offer) quantity else -1,
            MyFirebaseReferences.MESSAGE_OFFER_AMOUNT_FIELD to if (offer) price else -1,
            MyFirebaseReferences.MESSAGE_ADDRESSED_FIELD to !offer,
            MyFirebaseReferences.MESSAGE_ID_FIELD to messageId,
        )
        lifecycleScope.launch {
            val dataUpdate = async {
                Log.i("MssgFrag", "Looking for messages to update")
                //If the new message is an offer, udpate previous offers to addressed = true
                if (offer) {
                    val msgRef = db!!
                        .collection(MyFirebaseReferences.MESSAGES_COLLECTION)
                        .whereEqualTo(MyFirebaseReferences.MESSAGE_CONVO_FIELD, convo.id)
                        .whereEqualTo(MyFirebaseReferences.MESSAGE_OFFER_FIELD, true)
                        .whereEqualTo(MyFirebaseReferences.MESSAGE_ADDRESSED_FIELD, false)

                    msgRef
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                val indivMsgRef = db!!
                                    .collection(MyFirebaseReferences.MESSAGES_COLLECTION)
                                    .document(document.id)
                                lifecycleScope.launch {
                                    val update = async {
                                        indivMsgRef
                                            .update(
                                                MyFirebaseReferences.MESSAGE_ADDRESSED_FIELD,
                                                true
                                            )
                                            .addOnSuccessListener {
                                                Log.i("MessageFragment", "Done updating one")
                                            }
                                    }
                                    update.await()
                                }
                            }

                            val messageRef =
                                db!!.collection(MyFirebaseReferences.MESSAGES_COLLECTION)

                            Log.i("MssgFrag", "Sending a messagenow")
                            data.forEach { (key, value) -> println("$key = $value") }
                            messageRef
                                .document(messageId)
                                .set(data)
                                .addOnSuccessListener {
                                    Log.d(
                                        "DB SUCCESS",
                                        "DocumentSnapshot posted"
                                    )

                                    // "Reset" the message in the EditText
                                    messageEtv!!.setText("")

                                    //Update conversation timestamp
                                    val convoRef = db!!
                                        .collection(MyFirebaseReferences.CONVERSATIONS_COLLECTION)
                                        .document(convo.id)

                                    convoRef
                                        .update(
                                            mapOf(
                                                MyFirebaseReferences.CONVO_MESSAGE_FIELD to if (offer) if (order) "New Order" else "New Offer" else message,
                                                MyFirebaseReferences.CONVO_LSENDER_FIELD to loggedUser.email,
                                                MyFirebaseReferences.CONVO_TIMESTAMP_FIELD to timeNow
                                            )
                                        )
                                        .addOnSuccessListener {
                                            Log.i(
                                                "DB Updated SUCCESS",
                                                "DocumentSnapshot updated"
                                            )
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w(
                                                "DB Error",
                                                "Error updating document",
                                                e
                                            )
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.w(
                                        "DB FAIL",
                                        "Error adding document",
                                        e
                                    )
                                }
                        }
                    Log.i("MessageFragment", "Done Here ")
                } else {
                    val messageRef = db!!.collection(MyFirebaseReferences.MESSAGES_COLLECTION)

                    Log.i("MssgFrag", "Sending a messagenow")
                    data.forEach { (key, value) -> println("$key = $value") }
                    messageRef
                        .add(data)
                        .addOnSuccessListener {
                            Log.d(
                                "DB SUCCESS",
                                "DocumentSnapshot posted"
                            )

                            // "Reset" the message in the EditText
                            messageEtv!!.setText("")

                            //Update conversation timestamp
                            val convoRef = db!!
                                .collection(MyFirebaseReferences.CONVERSATIONS_COLLECTION)
                                .document(convo.id)

                            convoRef
                                .update(
                                    mapOf(
                                        MyFirebaseReferences.CONVO_MESSAGE_FIELD to message,
                                        MyFirebaseReferences.CONVO_LSENDER_FIELD to loggedUser.email,
                                        MyFirebaseReferences.CONVO_TIMESTAMP_FIELD to timeNow
                                    )
                                )
                                .addOnSuccessListener {
                                    Log.i(
                                        "DB Updated SUCCESS",
                                        "DocumentSnapshot updated"
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.w(
                                        "DB Error",
                                        "Error updating document",
                                        e
                                    )
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.w(
                                "DB FAIL",
                                "Error adding document",
                                e
                            )
                        }
                }
            }
            dataUpdate.await()
        }
    }

    override fun onResume() {
        super.onResume()

        Log.i("MessageFragment", "onResume")
//        myFirestoreRecyclerAdapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        myFirestoreRecyclerAdapter!!.stopListening()
    }
}