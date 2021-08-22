package com.mobdeve.s15.animall

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.smarteist.autoimageslider.SliderViewAdapter

class SliderViewHolder(itemView: View, context: Context) : SliderViewAdapter.ViewHolder(itemView)  {
    private val imageViewBackground: ImageView = itemView.findViewById(R.id.productImageIv)
    private var context: Context = context

    fun bindData(sliderItem: Listing) {
        imageViewBackground.setImageURI(sliderItem.imageURI)
//        Glide.with(context).load(sliderItem.posterId).into(imageViewBackground)
    }
}