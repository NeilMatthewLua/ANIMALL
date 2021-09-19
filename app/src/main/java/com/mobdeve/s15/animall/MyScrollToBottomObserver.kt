package com.mobdeve.s15.animall

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver

class MyScrollToBottomObserver(
    private val recycler: RecyclerView,
    private val adapter: MessageAdapter
) : AdapterDataObserver() {
    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        super.onItemRangeInserted(positionStart, itemCount)

        recycler.smoothScrollToPosition(adapter.itemCount - 1);
    }
}