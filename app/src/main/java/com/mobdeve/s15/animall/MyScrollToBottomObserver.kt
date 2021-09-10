package com.mobdeve.s15.animall

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import kotlinx.android.synthetic.main.fragment_message.*

class MyScrollToBottomObserver(
    private val recycler: RecyclerView,
    private val adapter: MessageAdapter,
    private val manager: LinearLayoutManager
) : AdapterDataObserver() {
    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        super.onItemRangeInserted(positionStart, itemCount)

        //Sauce: https://stackoverflow.com/questions/40797430/android-firebase-chat-recyclerview-auto-scroll-to-bottom-when-new-item-is-added
        //Udpated code on the inside with this source https://stackoverflow.com/questions/43045057/firebase-automatically-scroll-listview-when-data-is-added
//        val count = adapter.itemCount
//        val lastVisiblePosition = manager.findLastCompletelyVisibleItemPosition()
//        // If the recycler view is initially being loaded or the
//        // user is at the bottom of the list, scroll to the bottom
//        // of the list to show the newly added message.
//        val loading = lastVisiblePosition == -1
//        val atBottom = positionStart >= count - 1 && lastVisiblePosition == positionStart - 1
//        if (loading || atBottom) {
//            recycler.scrollToPosition(positionStart)
//        }

        recycler.smoothScrollToPosition(adapter.itemCount -1);
    }
}