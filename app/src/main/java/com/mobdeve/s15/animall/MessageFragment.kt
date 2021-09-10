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

                    //TODO username here is the logged user email, but now we'll use carlos_shi
                    myFirestoreRecyclerAdapter = MessageAdapter(options, loggedUser!!.email!!)
                    Log.i("MessageFragment", "Adapter Adapted")
                    val linearLayoutManager = LinearLayoutManager(requireContext())
                    messageRecyclerView!!.layoutManager = linearLayoutManager

                    myFirestoreRecyclerAdapter.registerAdapterDataObserver(
                        MyScrollToBottomObserver(messageRecyclerView, myFirestoreRecyclerAdapter)
                    )

                    Log.d("MessageFragment ", "OnViewWCreated2")
                    messageRecyclerView.adapter = myFirestoreRecyclerAdapter
                    Log.d("MessageFragment ", "OnViewWCreated3")
                    sendMessageBtn.setOnClickListener {
                        Log.i("Messages", "sending a message")
                        if (messageEtv!!.text.toString().isNotEmpty()) {
                            sendMessage()
                        }
                    }

                    //TODO Possible faulty thing here
                    //This adapater is supposely for the onStart(), but I
                    //placed it here it seems like the onStart calls first
                    //before the initialization of myadapter finishes
                    //Im not sure just yet how to fix that tho, so I placed it here
                    //to make sure the function calls after initialization
                    myFirestoreRecyclerAdapter!!.startListening()
                }
                convoInit.await()
            }
        })
    }

    fun sendMessage() {
        val message = messageEtv!!.text.toString()

        // Ready the values of the message
        val data: MutableMap<String, Any?> = HashMap()
        //TODO change username to logged user, for now use carlos_shi
        data[MyFirestoreReferences.MESSAGE_SENDER_FIELD] = loggedUser.email
        //TODO hardcoded convoId
        data[MyFirestoreReferences.MESSAGE_CONVO_FIELD] = convoModel.id
        data[MyFirestoreReferences.MESSAGE_FIELD] = message
        data[MyFirestoreReferences.TIME_FIELD] = Date()

        val messageRef = db!!.collection(MyFirestoreReferences.MESSAGES_COLLECTION)

        messageRef
            .add(data)
            .addOnSuccessListener {
                Log.d(
                    "DB SUCCESS",
                    "DocumentSnapshot updated"
                )
                // "Reset" the message in the EditText
                messageEtv!!.setText("")

            }
            .addOnFailureListener { e ->
                Log.w(
                    "DB FAIL",
                    "Error adding document",
                    e
                )
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