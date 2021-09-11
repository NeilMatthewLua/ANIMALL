package com.mobdeve.s15.animall

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_landing.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var conversationLayout: ConstraintLayout
    var conversationNameTv: TextView
    var conversationMessageTv: TextView
    var conversationTimeTv: TextView
    var conversationImageIv: ImageView
    var message: MessageModel? = null

    fun bindData(conversation: ConversationModel, message: MessageModel) {
        if (conversation.listingPhoto != null) {
            Picasso.get().load(conversation.listingPhoto)
                .error(R.drawable.ic_error)
                .placeholder(R.drawable.progress_animation)
                .into(conversationImageIv);
        }
//        Log.i("CVHolder", "getting latest messageeeee")
//        lifecycleScope.launch {
//            Log.i("CVHolder", "getting latest message")
//            val dataGet = async(Dispatchers.IO) {
////                TODO query for the latest message for each present convoId
//                message = DatabaseManager.getLatestMessage(conversation.id)!!
//            }
//            dataGet.await()
//
//
//            Log.i("ConvoVHolder", "${message == null}")
            if (message != null){
                Log.i("ConvoVHolder", message!!.message)

                var date = message!!.timestamp

                val sdf3: SimpleDateFormat =
                    SimpleDateFormat("MMM-dd-yyyy hh:mm:ss")
                sdf3.timeZone = TimeZone.getTimeZone("Asia/Singapore")

                var dateString = sdf3.format(date)

                conversationTimeTv.text = dateString
                conversationNameTv.text = conversation.listingName

                //TODO Compare email with logged user, for now use carlos_shi
                if(message!!.sender == "carlos_shi@dlsu.edu.ph") {
                    conversationMessageTv.text = "You: ${message!!.message}"
                }
                else {
                    conversationMessageTv.text = "Seller: ${message!!.message}"
                }
            }
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
