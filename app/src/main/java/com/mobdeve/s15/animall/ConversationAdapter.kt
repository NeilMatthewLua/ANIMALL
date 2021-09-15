package com.mobdeve.s15.animall

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ConversationAdapter(
    options: FirestoreRecyclerOptions<ConversationModel>,
    private val fragment: ConversationFragment,
    ) :
    FirestoreRecyclerAdapter<ConversationModel, ConversationViewHolder?>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val v =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.single_message_layout, parent, false)
        return ConversationViewHolder(v)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int, model: ConversationModel) {
        var message: MessageModel? = null

        Log.i("ConvoAdapter", "will be getting their latest messages")
        Log.i("ConvoAdapter", "${model.id}")

//        if (model.id.equals("0")) {
            Log.i("ConvoAdapter", "getting latest message ${model.id}")
            fragment.lifecycleScope.launch {
                val dataGet = async(Dispatchers.IO) {
                    message = DatabaseManager.getLatestMessage(model.id)!!
                }
                dataGet.await()

                holder.bindData(model, message!!)
            }
//        }

        holder.conversationLayout.setOnClickListener {
            val viewModel : MessageSharedViewModel by fragment.activityViewModels()
            viewModel.setListingData(model, false)
            it.findNavController().navigate(R.id.messageFragment)
        }
    }
}