package com.mobdeve.s15.animall

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_message.*
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
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

                    //TODO Get ConvoID From viewHolder,for now we query it here
                    val query = db!!
                        .collection(MyFirestoreReferences.MESSAGES_COLLECTION)
                        .whereEqualTo(
                            MyFirestoreReferences.MESSAGE_CONVO_FIELD,
                            it.id
                        )
                        .orderBy(MyFirestoreReferences.TIME_FIELD)

                    convoModel = it
                    loggedUser = Firebase.auth.currentUser!!

                    val options: FirestoreRecyclerOptions<MessageModel> =
                        FirestoreRecyclerOptions.Builder<MessageModel>()
                            .setQuery(query, MessageModel::class.java)
                            .build()

                    myFirestoreRecyclerAdapter = MessageAdapter(options, requireContext(), loggedUser!!.email!!)
                    Log.i("MessageFragment", "Adapter Adapted")

                    withContext(Dispatchers.Main) {
                        val linearLayoutManager = LinearLayoutManager(requireContext())
                        messageRecyclerView!!.layoutManager = linearLayoutManager

                        myFirestoreRecyclerAdapter.registerAdapterDataObserver(
                            MyScrollToBottomObserver(messageRecyclerView, myFirestoreRecyclerAdapter)
                        )

                        Log.d("MessageFragment ", "OnViewWCreated2")
                        messageRecyclerView.adapter = myFirestoreRecyclerAdapter

                        Log.d("MessageFragment ", "OnViewWCreated3")
                        sendMessageBtn.setOnClickListener { view ->
                            Log.i("Messages", "sending a message ON 1")
                            if (messageEtv!!.text.toString().isNotEmpty()) {
                                sendMessage(it, viewModel.getIsFirst(), false)
                            }
                        }
                        sendMessageBtn2.setOnClickListener { view ->
                            Log.i("Messages", "sending a message ON 2")
                            if (messageEtv!!.text.toString().isNotEmpty()) {
                                sendMessage(it, viewModel.getIsFirst(), true)
                            }
                        }

                        myFirestoreRecyclerAdapter!!.startListening()
                    }

                }
                convoInit.await()
            }
        })
    }

    fun sendMessage(convo: ConversationModel, isFirst: Boolean, offer: Boolean) {
        val message = messageEtv!!.text.toString()
        val messageId = UUID.randomUUID().toString()
        val timeNow = Date()

        if (isFirst) {
//            val convoID = UUID.randomUUID().toString()
            val convoHash = hashMapOf(
                MyFirestoreReferences.RECIPIENT_FIELD to convo.recipientEmail,
                MyFirestoreReferences.SENDER_FIELD to loggedUser.email!!,
                MyFirestoreReferences.LISTING_ID_FIELD to convo.listingId,
                MyFirestoreReferences.LISTING_NAME_FIELD to convo.listingName,
                MyFirestoreReferences.LISTING_PHOTO_FIELD to convo.listingPhoto,
                MyFirestoreReferences.CONVO_ID_FIELD to convo.id,
                MyFirestoreReferences.CONVO_TIMESTAMP_FIELD to Date(),
                MyFirestoreReferences.CONVO_USERS_FIELD to arrayListOf(loggedUser.email!!, convo.recipientEmail)
            )

            for ((key, value) in convoHash.entries) {
                Log.i("ViewListingFragment", "${key} => ${value}")
            }

            db!!.collection(MyFirestoreReferences.CONVERSATIONS_COLLECTION)
                .document(convo.id)
                .set(convoHash)
        }

        Log.i("MEssageFragment Convo.id", "${convo.id}")
        Log.i("MEssageFragment ConvoModel.id", "${convoModel.id}")
        // Ready the values of the message
        val data = hashMapOf(
            MyFirestoreReferences.MESSAGE_CONVO_FIELD to convo.id,
            MyFirestoreReferences.MESSAGE_SENDER_FIELD to loggedUser.email,
            MyFirestoreReferences.MESSAGE_CONVO_FIELD to convoModel.id,
            MyFirestoreReferences.MESSAGE_FIELD to if (offer) "New Order/Offer" else message,
            MyFirestoreReferences.TIME_FIELD to timeNow,
            MyFirestoreReferences.MESSAGE_OFFER_FIELD to offer,
            MyFirestoreReferences.MESSAGE_OFFER_QUANTITY_FIELD to if (offer) 5 else -1,
            MyFirestoreReferences.MESSAGE_OFFER_AMOUNT_FIELD to if (offer) 25 else -1,
            MyFirestoreReferences.MESSAGE_ADDRESSED_FIELD to !offer,
            MyFirestoreReferences.MESSAGE_ID_FIELD to messageId,
        )
        lifecycleScope.launch {
            val dataUpdate = async {
                Log.i("MssgFrag", "Looking for messages to update")
                //If the new message is an offer, udpate previous offers to addressed = true
                if (offer) {
                    val msgRef = db!!
                        .collection(MyFirestoreReferences.MESSAGES_COLLECTION)
                        .whereEqualTo(MyFirestoreReferences.MESSAGE_CONVO_FIELD, convo.id)
                        .whereEqualTo(MyFirestoreReferences.MESSAGE_OFFER_FIELD, true)
                        .whereEqualTo(MyFirestoreReferences.MESSAGE_ADDRESSED_FIELD, false)

                    msgRef
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                val indivMsgRef = db!!
                                    .collection(MyFirestoreReferences.MESSAGES_COLLECTION)
                                    .document(document.id)
                                lifecycleScope.launch {
                                    val update = async {
                                        indivMsgRef
                                            .update(
                                                MyFirestoreReferences.MESSAGE_ADDRESSED_FIELD,
                                                true
                                            )
                                            .addOnSuccessListener {
                                                Log.i("MessageFragment", "Done updating one")
                                            }
                                    }
                                    update.await()
                                }
                            }

                            val messageRef = db!!.collection(MyFirestoreReferences.MESSAGES_COLLECTION)

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
                                        .collection(MyFirestoreReferences.CONVERSATIONS_COLLECTION)
                                        .document(convo.id)

                                    convoRef
                                        .update(MyFirestoreReferences.CONVO_TIMESTAMP_FIELD, timeNow)
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
                    Log.i("DDDDDDDDDDDDDDDDDDDDDDDD", "OUTTA HERE")
                }
                else {
                    val messageRef = db!!.collection(MyFirestoreReferences.MESSAGES_COLLECTION)

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
                                .collection(MyFirestoreReferences.CONVERSATIONS_COLLECTION)
                                .document(convo.id)

                            convoRef
                                .update(MyFirestoreReferences.CONVO_TIMESTAMP_FIELD, timeNow)
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
        // When our app is open, we need to have the adapter listening for any changes in the data.
        // To do so, we'd want to turn on the listening using the appropriate method in the onStart
        // or onResume (basically before the start but within the loop)
//        myFirestoreRecyclerAdapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        // We want to eventually stop the listening when we're about to exit an app as we don't need
        // something listening all the time in the background.
        myFirestoreRecyclerAdapter!!.stopListening()
    }
}