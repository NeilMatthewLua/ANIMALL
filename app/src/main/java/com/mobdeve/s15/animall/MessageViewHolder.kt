package com.mobdeve.s15.animall

import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*


class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val usernameTv: TextView
    private val messageTv: TextView
    private val messageTimeTv: TextView
    private val messageLinearLayout: LinearLayout

    fun bindData(m: MessageModel) {
        usernameTv.text = m.sender
        messageTv.text = m.message

        val sdf3 =
                SimpleDateFormat("hh:mm:ss")
        sdf3.timeZone = TimeZone.getTimeZone("Asia/Singapore")

        var date = m.timestamp
        var dateString = sdf3.format(date)

        messageTimeTv.text = dateString
    }

    fun leftAlignText() {
        usernameTv.gravity = Gravity.LEFT
        messageTv.gravity = Gravity.LEFT
        messageTimeTv.gravity = Gravity.LEFT
        messageLinearLayout.gravity = Gravity.LEFT

//        Log.i("L Viewholder", messageTv.text.toString())
//        Log.i("L ViewHolder this message was set to ", messageTimeTv.gravity.toString())
    }

    fun rightAlignText() {
        usernameTv.gravity = Gravity.RIGHT
        messageTv.gravity = Gravity.RIGHT
        messageTimeTv.gravity = Gravity.RIGHT
        messageLinearLayout.gravity = Gravity.RIGHT

//        Log.i("R Viewholder", messageTv.text.toString())
//        Log.i("R ViewHolder this message was set to ", messageTimeTv.gravity.toString())
    }

    fun makeRed(){
        messageTv.setTextColor(Color.parseColor("#E32323"))
    }

    fun makeGreen(){
        messageTv.setTextColor(Color.parseColor("#287C26"))
    }

    init {
        usernameTv = itemView.findViewById(R.id.messageUsernameTv)
        messageTv = itemView.findViewById(R.id.messageMessageTv)
        messageTimeTv = itemView.findViewById(R.id.messageTimeTv)
        messageLinearLayout = itemView.findViewById(R.id.messageLinearLayout)
    }
}