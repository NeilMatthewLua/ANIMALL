package com.mobdeve.s15.animall

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.smarteist.autoimageslider.SliderViewAdapter

class SliderAdapter(context: Context) :
    SliderViewAdapter<SliderViewHolder>() {

    private var mSliderItems: MutableList<Listing> = ArrayList()
    private var context: Context = context

    fun renewItems(sliderItems: MutableList<Listing>) {
        mSliderItems = sliderItems
        notifyDataSetChanged()
    }

    fun emptyItems() {
        mSliderItems.clear()
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int) {
        mSliderItems.removeAt(position)
        notifyDataSetChanged()
    }

    fun addItem(sliderItem: Listing) {
        mSliderItems.add(sliderItem)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup): SliderViewHolder {
        val inflate: View =
            LayoutInflater.from(parent.context).inflate(R.layout.listing_image_display, null)
        return SliderViewHolder(inflate, context)
    }

    override fun onBindViewHolder(viewHolder: SliderViewHolder, position: Int) {
        val sliderItem: Listing = mSliderItems[position]
        viewHolder.bindData(sliderItem)
        viewHolder.setDeleteBtnOnClickListener {
            deleteItem(position)
        }
    }

    override fun getCount(): Int {
        //slider view count could be dynamic size
        return mSliderItems.size
    }
}