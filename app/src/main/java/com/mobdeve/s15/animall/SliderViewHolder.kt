package com.mobdeve.s15.animall

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.smarteist.autoimageslider.SliderViewAdapter

class SliderViewHolder(itemView: View, context: Context) : SliderViewAdapter.ViewHolder(itemView)  {
    private val imageViewBackground: ImageView = itemView.findViewById(R.id.productImageIv)
    private val deleteBtn: Button = itemView.findViewById(R.id.deleteBtn)

    fun bindData(sliderItem: Listing) {
//        imageViewBackground.setImageURI(sliderItem.imageURI)
        imageViewBackground.setImageBitmap(sliderItem.imageBitmap)
//        Glide.with(context).load(sliderItem.posterId).into(imageViewBackground)
    }

    fun setDeleteBtnOnClickListener(onClickListener: View.OnClickListener) {
        deleteBtn.setOnClickListener(onClickListener)
    }
}