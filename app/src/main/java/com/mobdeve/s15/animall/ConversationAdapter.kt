package com.mobdeve.s15.animall

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.coroutines.launch

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
        Log.i("ConvoAdapter", "will be getting their latest messages")
        Log.i("ConvoAdapter", "${model.id}")

        Log.i("ConvoAdapter", "getting latest message ${model.id}")
        fragment.lifecycleScope.launch {
//            try {
//                val dataGet = async(Dispatchers.IO) {
//                    message = DatabaseManager.getLatestMessage(model.id)!!
//                }
//                dataGet.await()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }

            holder.bindData(model)
            holder.conversationLayout.setOnClickListener {
                val viewModel : MessageSharedViewModel by fragment.activityViewModels()
                viewModel.setListingData(model, false)
                it.findNavController().navigate(R.id.messageFragment)
            }
        }

    }
}