package com.mobdeve.s15.animall

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class ConversationAdapter(private val data: ArrayList<ConversationModel>) :
    RecyclerView.Adapter<ConversationViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val v =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.single_message_layout, parent, false)
        return ConversationViewHolder(v)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }
}