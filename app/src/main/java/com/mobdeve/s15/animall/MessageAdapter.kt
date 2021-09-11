package com.mobdeve.s15.animall

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions


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
    // the screen and those who aren't to the left of the screen
    private val sender: String
) :
    FirestoreRecyclerAdapter<MessageModel, MyViewHolder?>(options) {
    // Good old onCreateViewHolder. Nothing different here.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.message_bubble_layout, parent, false)
        return MyViewHolder(v)
    }

    // The onBindViewHolder is slightly different as you also get the "model". It was clear from the
    // documentation, but it seems that its discouraging the use of the position parameter. The
    // model passed in is actually the respective model that is about to be bound. Hence, why we
    // don't use position, and directly get the information from the model parameter.
    override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: MessageModel) {
        holder.bindData(model)

        // Change alignment depending on whether the message is of the current user or not
        if (model.sender.equals(sender)) { // Right align
            holder.rightAlignText()
        } else { // Left align
            holder.leftAlignText()
        }
    }
}
