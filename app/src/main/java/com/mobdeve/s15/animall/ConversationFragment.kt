package com.mobdeve.s15.animall

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_landing.*
import kotlinx.android.synthetic.main.fragment_messages.*
import kotlinx.android.synthetic.main.fragment_messages.dimBackgroundV
import kotlinx.android.synthetic.main.fragment_messages.landingPb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.ArrayList

class ConversationFragment : Fragment() {
    val TAG: String = "CONVERSATION FRAGMENT"
    var data: ArrayList<ConversationModel> = ArrayList<ConversationModel>()
    var message_data: ArrayList<MessageModel> = ArrayList<MessageModel>()
    var hasRetrieved: Boolean = false
    // RecyclerView components
    lateinit var myAdapter: ConversationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val loggedUser = Firebase.auth.currentUser
            val dataInit = async(Dispatchers.IO) {
                data = DatabaseManager.initializeConversationData(loggedUser?.email!!)
//                message_data = DatabaseManager.initializeLatestMessageData()
            }
            dataInit.await()

            // Layout manager
            val linearLayoutManager = LinearLayoutManager(activity)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            conversationRecyclerView!!.layoutManager = linearLayoutManager

            // Adapter
            myAdapter = ConversationAdapter(data!!, this@ConversationFragment)
            conversationRecyclerView!!.adapter = myAdapter
            myAdapter.notifyDataSetChanged()
            hasRetrieved = true
            dimBackgroundV.visibility = View.GONE
            landingPb.visibility = View.GONE
        }
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

        // Layout manager
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        conversationRecyclerView!!.layoutManager = linearLayoutManager

        // Adapter
        myAdapter = ConversationAdapter(data!!, this@ConversationFragment)
        conversationRecyclerView!!.adapter = myAdapter
    }
}