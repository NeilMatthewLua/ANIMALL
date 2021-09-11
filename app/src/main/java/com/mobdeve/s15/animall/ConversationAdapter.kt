package com.mobdeve.s15.animall

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ConversationAdapter(
    private val data: ArrayList<ConversationModel>,
    private val fragment: ConversationFragment,
    private val lifestyleScope: LifecycleCoroutineScope
    ) :
    RecyclerView.Adapter<ConversationViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val v =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.single_message_layout, parent, false)
        return ConversationViewHolder(v)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        var message: MessageModel? = null

        Log.i("ConvoAdapter", "will be getting their latest messages")
        fragment.lifecycleScope.launch {
            Log.i("ConvoAdapter", "getting latest message")
            val dataGet = async(Dispatchers.IO) {
//                TODO query for the latest message for each present convoId
                message = DatabaseManager.getLatestMessage(data[position].id)!!
            }
            dataGet.await()

            holder.bindData(data[position], message!!)
        }

        holder.conversationLayout.setOnClickListener {
            val viewModel : MessageSharedViewModel by fragment.activityViewModels()
            viewModel.setListingData(data[position])
            it.findNavController().navigate(R.id.messageFragment)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}