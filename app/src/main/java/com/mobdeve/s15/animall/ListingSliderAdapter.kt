package com.mobdeve.s15.animall

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.smarteist.autoimageslider.SliderViewAdapter

class ListingSliderAdapter(context: Context) :
    SliderViewAdapter<ListingSliderViewHolder>() {

    private var mSliderItems: MutableList<String> = ArrayList()
    private var context:Context = context

    fun renewItems(sliderItems: MutableList<String>) {
        mSliderItems = sliderItems
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int) {
        mSliderItems.removeAt(position)
        notifyDataSetChanged()
    }

    fun addItem(sliderItem: String) {
        mSliderItems.add(sliderItem)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup): ListingSliderViewHolder {
        val inflate: View =
            LayoutInflater.from(parent.context).inflate(R.layout.listing_slider_layout, null)
        return ListingSliderViewHolder(inflate, context)
    }

    override fun onBindViewHolder(viewHolder: ListingSliderViewHolder, position: Int) {
        val sliderItem: String = mSliderItems[position]
        viewHolder.bindData(sliderItem)
//        viewHolder.textViewDescription.setText(sliderItem.getDescription())
//        viewHolder.textViewDescription.textSize = 16f
//        viewHolder.textViewDescription.setTextColor(Color.WHITE)
//        Glide.with(viewHolder.itemView)
//            .load(sliderItem.getImageUrl)
//            .fitCenter()
//            .into(viewHolder.imageViewBackground)
        viewHolder.itemView.setOnClickListener{
            Toast.makeText(context,"This is item in position $position", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun getCount(): Int {
        //slider view count could be dynamic size
        return mSliderItems.size
    }
}
