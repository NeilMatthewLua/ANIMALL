package com.mobdeve.s15.animall

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class ProfileListingAdapter(private val data: ArrayList<ListingModel>, private val fragment: UserProfileFragment) :
    RecyclerView.Adapter<ProfileListingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileListingViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_profile_listing_layout, parent, false)
        return ProfileListingViewHolder(v)
    }

    override fun onBindViewHolder(holder: ProfileListingViewHolder, position: Int) {
        holder.bindData(data[position])
        // TODO: Attach onclick listener to navigate to corresponding listing
        // TODO: Attach edit/delete/close buttons listeners
    }

    override fun getItemCount(): Int {
        return data.size
    }
}