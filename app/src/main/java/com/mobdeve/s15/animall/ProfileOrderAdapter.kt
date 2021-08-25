package com.mobdeve.s15.animall

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class ProfileOrderAdapter(private val data: ArrayList<ListingModel>, private val fragment: UserProfileFragment) :
    RecyclerView.Adapter<LandingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LandingViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_profile_order_layout, parent, false)
        return LandingViewHolder(v)
    }

    override fun onBindViewHolder(holder: LandingViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }
}