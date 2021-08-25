package com.mobdeve.s15.animall

import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.squareup.picasso.Picasso


class LandingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    lateinit var listingData: ListingModel
    var itemContainerLayout: ConstraintLayout
    var productNameTv: TextView
    var productPriceTv: TextView
    var productLocationChip: Chip
    var productCategoryChip: Chip
    var productImageIv: ImageView

    // TODO: Update this when hooking to db, also add in linking to listing page when clicked
    fun bindData(listing: ListingModel) {
        listingData = listing
        productNameTv.text = listing.name
        productPriceTv.text = "₱" + listing.unitPrice.toString()
        productLocationChip.text = listing.preferredLocation
        productCategoryChip.text = listing.category

        if (listing.photos.size > 0) {
            Picasso.get().
            load(listing.photos[0])
                .error(R.drawable.ic_error)
                .placeholder( R.drawable.progress_animation)
                .into(productImageIv);

            // Create rounded bottom image
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val curveRadius = 20F
                productImageIv.outlineProvider = object : ViewOutlineProvider() {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun getOutline(view: View?, outline: Outline?) {
                        outline?.setRoundRect(0, 0, view!!.width, view.height, curveRadius)
                    }
                }
                productImageIv.clipToOutline = true
            }
        }
    }

    init {
        productNameTv = itemView.findViewById(R.id.productNameTv)
        productPriceTv = itemView.findViewById(R.id.productPriceTv)
        productLocationChip = itemView.findViewById(R.id.productLocationChip)
        productCategoryChip = itemView.findViewById(R.id.productCategoryChip)
        productImageIv = itemView.findViewById(R.id.productImageIv)
        itemContainerLayout = itemView.findViewById(R.id.itemContainerLayout)
    }
}