package com.mobdeve.s15.animall

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.smarteist.autoimageslider.SliderViewAdapter
import com.squareup.picasso.Picasso

class ListingSliderViewHolder(itemView: View, context: Context) :
    SliderViewAdapter.ViewHolder(itemView) {
    private val imageViewBackground: ImageView = itemView.findViewById(R.id.imageView3)

    fun bindData(sliderItem: String) {
        Picasso.get().load(sliderItem)
            .error(R.drawable.ic_error)
            .placeholder(R.drawable.progress_animation)
            .into(imageViewBackground)
    }
}