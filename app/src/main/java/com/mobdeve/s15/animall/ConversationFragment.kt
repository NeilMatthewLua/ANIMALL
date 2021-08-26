package com.mobdeve.s15.animall

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_messages.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.ArrayList

class ConversationFragment : Fragment() {

    lateinit var data: ArrayList<ConversationModel>
    // RecyclerView components
    lateinit var myAdapter: ConversationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val dataInit = async(Dispatchers.IO) {
                data = DatabaseManager.initializeConversationData()
            }
            dataInit.await()

            // Layout manager
            val linearLayoutManager = LinearLayoutManager(activity)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            conversationRecyclerView!!.layoutManager = linearLayoutManager

            // Adapter
            myAdapter = ConversationAdapter(data!!)
            conversationRecyclerView!!.adapter = myAdapter
            myAdapter.notifyDataSetChanged()
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
}