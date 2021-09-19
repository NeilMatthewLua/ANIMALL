package com.mobdeve.s15.animall

import android.graphics.Outline
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
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

    fun bindData(listing: ListingModel) {
        listingData = listing
        profileListingNameTv.text = listing.name
        profileListingQuantityTv.text = listing.stock.toString() + " in stock"
        profileListingPriceTv.text = "₱" + listing.unitPrice.toString()

        if (!listing.isOpen) {
            profileEditListingBtn.visibility = View.GONE
            profileCloseListingBtn.visibility = View.GONE
        }

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

    fun setCloseBtnListener(manager: FragmentManager, fragment: UserProfileFragment) {
        profileCloseListingBtn.setOnClickListener {
            val dialog = CustomDialogFragment()
            // optionally pass arguments to the dialog fragment
            val args = Bundle()
            args.putString(
                CustomDialogFragment.MODAL_TYPE_KEY,
                CustomDialogFragment.MODAL_LISTING_CLOSE
            )
            args.putString(CustomDialogFragment.MODAL_LISTING_ID_KEY, listingData.listingId)
            args.putString(CustomDialogFragment.MODAL_LISTING_NAME_KEY, listingData.name)
            dialog.arguments = args
            dialog.show(manager, "Close Listing")
        }
    }

    fun setDeleteBtnListener(manager: FragmentManager, fragment: UserProfileFragment) {
        profileDeleteListingBtn.setOnClickListener {
            val dialog = CustomDialogFragment()
            // optionally pass arguments to the dialog fragment
            val args = Bundle()
            args.putString(
                CustomDialogFragment.MODAL_TYPE_KEY,
                CustomDialogFragment.MODAL_LISTING_DELETE
            )
            args.putString(CustomDialogFragment.MODAL_LISTING_ID_KEY, listingData.listingId)
            args.putString(CustomDialogFragment.MODAL_LISTING_NAME_KEY, listingData.name)
            dialog.arguments = args
            dialog.show(manager, "Delete Listing")
        }
    }

    fun setEditBtnListener(manager: FragmentManager, fragment: UserProfileFragment) {
        profileEditListingBtn.setOnClickListener {
            val dialog = CustomDialogFragment()
            // optionally pass arguments to the dialog fragment
            val args = Bundle()
            args.putString(
                CustomDialogFragment.MODAL_TYPE_KEY,
                CustomDialogFragment.MODAL_LISTING_EDIT
            )
            args.putString(CustomDialogFragment.MODAL_LISTING_ID_KEY, listingData.listingId)
            args.putString(CustomDialogFragment.MODAL_LISTING_NAME_KEY, listingData.name)
            args.putLong(CustomDialogFragment.MODAL_LISTING_STOCK_KEY, listingData.stock)
            dialog.arguments = args
            dialog.show(manager, "Edit Listing")
        }
    }

    fun hideBtns() {
        profileDeleteListingBtn.visibility = View.GONE
        profileCloseListingBtn.visibility = View.GONE
        profileEditListingBtn.visibility = View.GONE
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