package com.mobdeve.s15.animall

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_message.*
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MessageFragment : Fragment() {
    // Replacement of the base adapter view
    private lateinit var myFirestoreRecyclerAdapter: MessageAdapter

    // DB reference
    private var db: FirebaseFirestore? = null

    private val convoId = "ppM1zG50BJypCp3frVEu"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = DatabaseManager.getInstance()

        //TODO Get ConvoID From viewHolder,for now we query it here
        val query = db!!
            .collection(MyFirestoreReferences.MESSAGES_COLLECTION)
            .whereEqualTo(
                MyFirestoreReferences.MESSAGE_CONVO_FIELD,
                convoId)
            .orderBy(MyFirestoreReferences.TIME_FIELD)

        val options: FirestoreRecyclerOptions<MessageModel> = FirestoreRecyclerOptions.Builder<MessageModel>()
            .setQuery(query, MessageModel::class.java)
            .build()

        //TODO username here is the logged user email, but now we'll use carlos_shi
        myFirestoreRecyclerAdapter = MessageAdapter(options, "carlos_shi@dlsu.edu.ph")

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

        val linearLayoutManager = LinearLayoutManager(requireContext())
        messageRecyclerView!!.layoutManager = linearLayoutManager

        myFirestoreRecyclerAdapter.registerAdapterDataObserver(
            MyScrollToBottomObserver(messageRecyclerView, myFirestoreRecyclerAdapter)
        )

        messageRecyclerView.adapter = myFirestoreRecyclerAdapter

        sendMessageBtn.setOnClickListener{
            Log.i("Messages", "sending a message")
            if (messageEtv!!.text.toString().isNotEmpty()) {
                sendMessage()
            }
        }
    }

    fun sendMessage() {
        val message = messageEtv!!.text.toString()

        // Ready the values of the message
        val data: MutableMap<String, Any?> = HashMap()
        //TODO change username to logged user, for now use carlos_shi
        data[MyFirestoreReferences.MESSAGE_SENDER_FIELD] = "carlos_shi@dlsu.edu.ph"
        //TODO hardcoded convoId
        data[MyFirestoreReferences.MESSAGE_CONVO_FIELD] = convoId
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

    override fun onStart() {
        super.onStart()
        // When our app is open, we need to have the adapter listening for any changes in the data.
        // To do so, we'd want to turn on the listening using the appropriate method in the onStart
        // or onResume (basically before the start but within the loop)
        myFirestoreRecyclerAdapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        // We want to eventually stop the listening when we're about to exit an app as we don't need
        // something listening all the time in the background.
        myFirestoreRecyclerAdapter!!.stopListening()
    }
}