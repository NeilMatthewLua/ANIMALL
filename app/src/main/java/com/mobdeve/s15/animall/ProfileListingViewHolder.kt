package com.mobdeve.s15.animall

import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ProfileListingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    lateinit var listingData: ListingModel
    var profileListingImageIv: ImageView
    var profileListingNameTv: TextView
    var profileListingQuantityTv: TextView
    var profileListingPriceTv: TextView
    var profileDeleteListingBtn: Button
    var profileCloseListingBtn: Button
    var profileEditListingBtn: Button

    // TODO: Update this when hooking to db, also add in linking to listing page when clicked
    fun bindData(listing: ListingModel) {
        listingData = listing
        profileListingNameTv.text = listing.name
        profileListingQuantityTv.text = listing.stock.toString()
        profileListingPriceTv.text = "₱" + listing.unitPrice.toString()

        if (listing.photos.size > 0) {
            Picasso.get().load(listing.photos[0])
                .error(R.drawable.ic_error)
                .placeholder(R.drawable.progress_animation)
                .into(profileListingImageIv);

            // Create rounded bottom image
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val curveRadius = 10F
                profileListingImageIv.outlineProvider = object : ViewOutlineProvider() {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun getOutline(view: View?, outline: Outline?) {
                        outline?.setRoundRect(0, 0, view!!.width, view.height, curveRadius)
                    }
                }
                profileListingImageIv.clipToOutline = true
            }
        }
    }

    init {
        profileListingImageIv = itemView.findViewById(R.id.profileListingImageIv)
        profileListingNameTv = itemView.findViewById(R.id.profileListingNameTv)
        profileListingQuantityTv = itemView.findViewById(R.id.profileListingQuantityTv)
        profileListingPriceTv = itemView.findViewById(R.id.profileListingPriceTv)
        profileDeleteListingBtn = itemView.findViewById(R.id.profileDeleteListingBtn)
        profileCloseListingBtn = itemView.findViewById(R.id.profileCloseListingBtn)
        profileEditListingBtn = itemView.findViewById(R.id.profileEditListingBtn)
    }
}