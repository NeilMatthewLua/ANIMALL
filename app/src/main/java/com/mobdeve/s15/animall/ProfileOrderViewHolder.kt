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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ProfileOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    lateinit var orderData: OrderModel
    var profileOrderImageIv: ImageView
    var profileOrderNameTv: TextView
    var profileOrderQuantityTv: TextView
    var receivedOrderBtn: Button
    var profileOrderId: String = ""
    val USER_PROFILE_FRAGMENT = 1

    // TODO: Update this when hooking to db, also add in linking to listing page when clicked
    fun bindData(order: OrderModel) {
        orderData = order
        profileOrderId = order.orderId
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

    fun setConfirmBtnListener(manager: FragmentManager, fragment: UserProfileFragment) {
        receivedOrderBtn.setOnClickListener{
            val dialog = CustomDialogFragment()
            // optionally pass arguments to the dialog fragment
            var args = Bundle()
            args.putString("orderName", orderData.listingName)
            dialog.arguments = args
            dialog.show(manager, "Confirm Order")
        }
    }

    init {
        profileOrderImageIv = itemView.findViewById(R.id.profileOrderImageIv)
        profileOrderNameTv = itemView.findViewById(R.id.profileOrderNameTv)
        profileOrderQuantityTv = itemView.findViewById(R.id.profileOrderQuantityTv)
        receivedOrderBtn = itemView.findViewById(R.id.receivedOrderBtn)
    }
}