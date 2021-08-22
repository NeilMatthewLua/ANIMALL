package com.mobdeve.s15.animall

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.smarteist.autoimageslider.SliderViewAdapter

class SliderViewHolder(itemView: View, context: Context) : SliderViewAdapter.ViewHolder(itemView)  {
    private val imageViewBackground: ImageView = itemView.findViewById(R.id.imageView3)
    private var context:Context = context

    fun bindData(sliderItem: Listing) {
        imageViewBackground.setImageResource(sliderItem.posterId)
//        Glide.with(context).load(sliderItem.posterId).into(imageViewBackground)
    }
}