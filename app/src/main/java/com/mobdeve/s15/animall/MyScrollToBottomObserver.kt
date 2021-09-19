package com.mobdeve.s15.animall

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver

class MyScrollToBottomObserver(
    private val recycler: RecyclerView,
    private val adapter: MessageAdapter
) : AdapterDataObserver() {
    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        super.onItemRangeInserted(positionStart, itemCount)

        //Sauce: https://stackoverflow.com/questions/40797430/android-firebase-chat-recyclerview-auto-scroll-to-bottom-when-new-item-is-added
        //Udpated code on the inside with this source https://stackoverflow.com/questions/43045057/firebase-automatically-scroll-listview-when-data-is-added

        recycler.smoothScrollToPosition(adapter.itemCount -1);
    }
}