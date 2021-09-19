package com.mobdeve.s15.animall

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_landing.*
import kotlinx.android.synthetic.main.fragment_message.*
import kotlinx.android.synthetic.main.fragment_messages.*
import kotlinx.android.synthetic.main.fragment_messages.landingPb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

class ConversationFragment : Fragment() {
    var data: ArrayList<ConversationModel> = ArrayList<ConversationModel>()
    var message_data: ArrayList<MessageModel> = ArrayList<MessageModel>()
    var hasRetrieved: Boolean = false

    // RecyclerView components
    lateinit var myAdapter: ConversationAdapter
    // Replacement of the base adapter view
    private lateinit var myFirestoreRecyclerAdapter: ConversationAdapter

    // DB reference
    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater
            .inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        if (hasRetrieved) {
            dimBackgroundV.visibility = View.GONE
            landingPb.visibility = View.GONE
        }

        lifecycleScope.launch {
            val loggedUser = Firebase.auth.currentUser
            val dataInit = async(Dispatchers.IO) {
                db = DatabaseManager.getInstance()

                val query = db!!
                    .collection(MyFirebaseReferences.CONVERSATIONS_COLLECTION)
                    .whereArrayContains("users", loggedUser?.email!!)
                    .orderBy(MyFirebaseReferences.TIME_FIELD, Query.Direction.DESCENDING)

                val options: FirestoreRecyclerOptions<ConversationModel> =
                    FirestoreRecyclerOptions.Builder<ConversationModel>()
                        .setQuery(query, ConversationModel::class.java)
                        .build()
                Log.i("ConvoFragment", "I will try getting some conversations")
                db!!
                    .collection(MyFirebaseReferences.CONVERSATIONS_COLLECTION)
                    .whereArrayContains("users", loggedUser?.email!!)
                    .orderBy(MyFirebaseReferences.TIME_FIELD, Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener {
                        it.forEach {
                            Log.i("ConvoFragment", "${it.id} => ${it.data}")
                        }
                    }
                    .addOnFailureListener {
                        Log.i("ConvoFragment", "Failed Milord")
                    }
                myFirestoreRecyclerAdapter = ConversationAdapter(options, this@ConversationFragment)
                Log.i("ConversationFragment", "Adapter Made")

                myFirestoreRecyclerAdapter!!.startListening()

                getActivity()?.runOnUiThread {
                    hasRetrieved = true
                    dimBackgroundV.visibility = View.GONE
                    landingPb.visibility = View.GONE

                    val linearLayoutManager = LinearLayoutManager(requireContext())
                    conversationRecyclerView!!.layoutManager = linearLayoutManager
                    conversationRecyclerView.adapter = myFirestoreRecyclerAdapter
                    Log.i("ConversationFragment", "Adapter Assigned")
                }
            }
            dataInit.await()

        }
    }

    override fun onStop() {
        super.onStop()
        // We want to eventually stop the listening when we're about to exit an app as we don't need
        // something listening all the time in the background.
        myFirestoreRecyclerAdapter!!.stopListening()
    }

    companion object {
        const val TAG: String = "CONVERSATION FRAGMENT"
    }
}