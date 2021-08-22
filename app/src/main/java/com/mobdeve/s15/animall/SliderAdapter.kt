package com.mobdeve.s15.animall

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.smarteist.autoimageslider.SliderViewAdapter

class SliderAdapter(context: Context) :
    SliderViewAdapter<SliderViewHolder>() {

    private var mSliderItems: MutableList<Listing> = ArrayList()
    private var context:Context = context

    fun renewItems(sliderItems: MutableList<Listing>) {
        mSliderItems = sliderItems
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
            LayoutInflater.from(parent.context).inflate(R.layout.anime_promotion, null)
        return SliderViewHolder(inflate, context)
    }

    override fun onBindViewHolder(viewHolder: SliderViewHolder, position: Int) {
        val sliderItem: Listing = mSliderItems[position]
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
