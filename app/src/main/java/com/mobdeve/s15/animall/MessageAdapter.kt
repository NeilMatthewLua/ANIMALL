package com.mobdeve.s15.animall

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class MessageAdapter(
    options: FirestoreRecyclerOptions<MessageModel>,
    private val context: Context,
    private val sender: String
) :
    FirestoreRecyclerAdapter<MessageModel, RecyclerView.ViewHolder?>(options) {

    companion object {
        const val TYPE_OFFER = 1
        const val TYPE_TEXT = 2
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return if (viewType == TYPE_OFFER) { // for offer layout
            view = LayoutInflater.from(context)
                .inflate(R.layout.message_offer_layout, viewGroup, false)
            MessageOfferViewHolder(view)
        } else { // for text layout
            view = LayoutInflater.from(context)
                .inflate(R.layout.message_bubble_layout, viewGroup, false)
            MyViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (getItem(position).offer) {
            return TYPE_OFFER
        } else {
            return TYPE_TEXT
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        model: MessageModel
    ) {
        if (getItemViewType(position) == TYPE_TEXT) {
            (holder as MyViewHolder).bindData(model)

            // Change alignment depending on whether the message is of the current user or not
            if (model.sender == sender) { // Right align
                holder.rightAlignText()
                Log.i("MessageAdapter isOffer:", "${model.offer}")
            } else { // Left align
                holder.leftAlignText()
            }
        } else {
            (holder as MessageOfferViewHolder).bindData(model)

            // Change alignment depending on whether the message is of the current user or not
            if (model.sender == sender) { // Right align
                holder.rightAlignText(model)
                Log.i("MessageAdapter isOffer:", "${model.offer}")
            } else { // Left align
                holder.leftAlignText(model)
            }
        }
    }
}
