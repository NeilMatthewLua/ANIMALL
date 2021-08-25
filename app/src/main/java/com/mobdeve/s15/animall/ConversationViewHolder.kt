package com.mobdeve.s15.animall

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var conversationNameTv: TextView
    var conversationMessageTv: TextView
    var conversationTimeTv: TextView
    var conversationImageIv: ImageView

    fun bindData(conversation: ConversationModel) {
        if (conversation.listingPhoto != null) {
            Picasso.get().load(conversation.listingPhoto)
                .error(R.drawable.ic_error)
                .placeholder(R.drawable.progress_animation)
                .into(conversationImageIv);
        }
        if(conversation.messages.size > 0) {
            //TODO Compare email with logged user, for now use carlos_shi
            if(conversation.messages[0].sender == "carlos_shi@dlsu.edu.ph") {
                conversationMessageTv.text = "You: ${conversation.messages[0].message}"
            }
            else {
                conversationMessageTv.text = "Seller: ${conversation.messages[0].message}"
            }

            val sdf3: SimpleDateFormat =
                SimpleDateFormat("MMM-dd-yyyy hh:mm:ss")

            var date = conversation.messages[0].timestamp
            System.out.println(date.javaClass.kotlin)

            var dateString = sdf3.format(date)

            conversationTimeTv.text = dateString
        }
        conversationNameTv.text = conversation.listingName
    }

    init {
        conversationNameTv = itemView.findViewById(R.id.conversationNameTv)
        conversationMessageTv = itemView.findViewById(R.id.conversationMessageTv)
        conversationTimeTv = itemView.findViewById(R.id.conversationTimeTv)
        conversationImageIv = itemView.findViewById(R.id.conversationImageIv)
    }
}