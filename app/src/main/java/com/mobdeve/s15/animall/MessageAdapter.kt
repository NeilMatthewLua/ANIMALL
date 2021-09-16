package com.mobdeve.s15.animall

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView

/*
 * The FirestoreRecyclerAdapter is is a modification of the regular Adapter and is able to integrate
 * with Firestore. According to the documentation: [The Adapter] binds a Query to a RecyclerView and
 * responds to all real-time events included items being added, removed, moved, or changed. Best
 * used with small result sets since all results are loaded at once.
 * See https://firebaseopensource.com/projects/firebase/firebaseui-android/firestore/readme/
 * */

/*
 * The FirestoreRecyclerAdapter is is a modification of the regular Adapter and is able to integrate
 * with Firestore. According to the documentation: [The Adapter] binds a Query to a RecyclerView and
 * responds to all real-time events included items being added, removed, moved, or changed. Best
 * used with small result sets since all results are loaded at once.
 * See https://firebaseopensource.com/projects/firebase/firebaseui-android/firestore/readme/
 * */
class MessageAdapter(
    options: FirestoreRecyclerOptions<MessageModel>, // We need to know who the current user is so that we can adjust their message to the right of
    // the screen and those who aren't to the left of the screen,
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
            view = LayoutInflater.from(context).inflate(R.layout.message_offer_layout, viewGroup, false)
            MessageOfferViewHolder(view)
        } else { // for text layout
            view = LayoutInflater.from(context).inflate(R.layout.message_bubble_layout, viewGroup, false)
            MyViewHolder(view)
        }
    }


    // Good old onCreateViewHolder. Nothing different here.
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
//        val v: View =
//            LayoutInflater.from(parent.context)
//                .inflate(R.layout.message_bubble_layout, parent, false)
//        return MyViewHolder(v)
//    }


    override fun getItemViewType(position: Int): Int {
//        return super.getItemViewType(position)
        if (getItem(position).offer) {
            return TYPE_OFFER
        }
        else {
            return TYPE_TEXT
        }
    }

    // The onBindViewHolder is slightly different as you also get the "model". It was clear from the
    // documentation, but it seems that its discouraging the use of the position parameter. The
    // model passed in is actually the respective model that is about to be bound. Hence, why we
    // don't use position, and directly get the information from the model parameter.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: MessageModel) {
        if (getItemViewType(position) == TYPE_TEXT) {
            (holder as MyViewHolder).bindData(model)

            // Change alignment depending on whether the message is of the current user or not
            if (model.sender.equals(sender)) { // Right align
                holder.rightAlignText()
                Log.i("MessageAdapter isOffer:", "${model.offer}")
            } else { // Left align
                holder.leftAlignText()
            }
        }
        else {
            (holder as MessageOfferViewHolder).bindData(model)

            // Change alignment depending on whether the message is of the current user or not
            if (model.sender.equals(sender)) { // Right align
                holder.rightAlignText(model)
                Log.i("MessageAdapter isOffer:", "${model.offer}")
            } else { // Left align
                holder.leftAlignText(model)
            }
        }
    }
}
