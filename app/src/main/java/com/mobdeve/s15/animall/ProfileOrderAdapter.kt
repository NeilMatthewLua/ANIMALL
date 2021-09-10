package com.mobdeve.s15.animall

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class ProfileOrderAdapter(private val data: ArrayList<OrderModel>, private val fragment: UserProfileFragment) :
    RecyclerView.Adapter<ProfileOrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileOrderViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_profile_order_layout, parent, false)
        return ProfileOrderViewHolder(v)
    }

    override fun onBindViewHolder(holder: ProfileOrderViewHolder, position: Int) {
        holder.bindData(data[position])
        holder.setConfirmBtnListener(fragment.requireActivity()!!.supportFragmentManager, fragment)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}