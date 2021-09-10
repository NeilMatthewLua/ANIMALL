package com.mobdeve.s15.animall

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_landing.*
import androidx.lifecycle.lifecycleScope
import com.github.satoshun.coroutine.autodispose.view.autoDisposeScope
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*


class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var conversationLayout: ConstraintLayout
    var conversationNameTv: TextView
    var conversationMessageTv: TextView
    var conversationTimeTv: TextView
    var conversationImageIv: ImageView
    lateinit var message: MessageModel

    fun bindData(conversation: ConversationModel) {
        if (conversation.listingPhoto != null) {
            Picasso.get().load(conversation.listingPhoto)
                .error(R.drawable.ic_error)
                .placeholder(R.drawable.progress_animation)
                .into(conversationImageIv);
        }

//        itemView.lifecycleScope.launch {
//            val dataGet = async(Dispatchers.IO) {
//                TODO query for the latest message for each present convoId
//                message = DatabaseManager.getLatestMessage(conversation.id)!!
//            }
//            dataGet.await()
//
//            val sdf3: SimpleDateFormat =
//                SimpleDateFormat("MMM-dd-yyyy hh:mm:ss")
//            sdf3.timeZone = TimeZone.getTimeZone("Asia/Singapore")
//
//            var date = message.timestamp
//
//            var dateString = sdf3.format(date)
//
//            conversationTimeTv.text = dateString
            conversationNameTv.text = conversation.listingName
//        }

//        if(conversation.messages.size > 0) {
//            //TODO Compare email with logged user, for now use carlos_shi
//            if(conversation.messages[0].sender == "carlos_shi@dlsu.edu.ph") {
//                conversationMessageTv.text = "You: ${conversation.messages[0].message}"
//            }
//            else {
//                conversationMessageTv.text = "Seller: ${conversation.messages[0].message}"
//            }
//        }
    }

    init {
        conversationLayout = itemView.findViewById(R.id.conversationLayout)
        conversationNameTv = itemView.findViewById(R.id.conversationNameTv)
        conversationMessageTv = itemView.findViewById(R.id.conversationMessageTv)
        conversationTimeTv = itemView.findViewById(R.id.conversationTimeTv)
        conversationImageIv = itemView.findViewById(R.id.conversationImageIv)
    }
}