package com.mobdeve.s15.animall

import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val usernameTv: TextView
    private val messageTv: TextView
    fun bindData(m: MessageModel) {
        usernameTv.text = m.sender
        messageTv.text = m.message
    }

    fun leftAlignText() {
        usernameTv.gravity = Gravity.LEFT
        messageTv.gravity = Gravity.LEFT
    }

    fun rightAlignText() {
        usernameTv.gravity = Gravity.RIGHT
        messageTv.gravity = Gravity.RIGHT
    }

    init {
        usernameTv = itemView.findViewById(R.id.messageUsernameTv)
        messageTv = itemView.findViewById(R.id.messageMessageTv)
    }
}