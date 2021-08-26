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

class ProfileOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    lateinit var orderData: OrderModel
    var profileOrderImageIv: ImageView
    var profileOrderNameTv: TextView
    var profileOrderQuantityTv: TextView
    var receivedOrderBtn: Button

    // TODO: Update this when hooking to db, also add in linking to listing page when clicked
    fun bindData(order: OrderModel) {
        orderData = order
        val totalPrice = order.quantity * order.soldPrice
        profileOrderNameTv.text = order.listingName
        profileOrderQuantityTv.text = "₱" + order.soldPrice.toString() + " x " + order.quantity.toString() + " = " + totalPrice.toString()

        Picasso.get().
        load(order.photosId)
            .error(R.drawable.ic_error)
            .placeholder( R.drawable.progress_animation)
            .into(profileOrderImageIv)

        // Create rounded bottom image
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val curveRadius = 10F
            profileOrderImageIv.outlineProvider = object : ViewOutlineProvider() {
                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(0, 0, view!!.width, view.height, curveRadius)
                }
            }
            profileOrderImageIv.clipToOutline = true
        }
    }

    init {
        profileOrderImageIv = itemView.findViewById(R.id.profileOrderImageIv)
        profileOrderNameTv = itemView.findViewById(R.id.profileOrderNameTv)
        profileOrderQuantityTv = itemView.findViewById(R.id.profileOrderQuantityTv)
        receivedOrderBtn = itemView.findViewById(R.id.receivedOrderBtn)
    }
}