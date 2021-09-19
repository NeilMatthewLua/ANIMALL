package com.mobdeve.s15.animall

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

class ProfileOrderAdapter(
    private var data: ArrayList<OrderModel>,
    private val fragment: UserProfileFragment
) :
    RecyclerView.Adapter<ProfileOrderViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileOrderViewHolder {
        val v =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_profile_order_layout, parent, false)

        fragment.requireActivity().supportFragmentManager
            .setFragmentResultListener(
                CustomDialogFragment.MODAL_ORDER_CONFIRM_RESULT,
                fragment.viewLifecycleOwner
            ) { key, bundle ->
                val result = bundle.getString(CustomDialogFragment.MODAL_SUCCESS_KEY)
                val id = bundle.getString(CustomDialogFragment.MODAL_ORDER_ID_KEY)
                if (result == "ok") {
                    fragment.lifecycleScope.launch {
                        fragment.profileDimBackgroundV.visibility = View.VISIBLE
                        fragment.profilePb.visibility = View.VISIBLE
                        var success = false
                        val confirmOrder = async(Dispatchers.IO) {
                            success = DatabaseManager.confirmOrder(id!!)
                        }
                        confirmOrder.await()
                        fragment.profileDimBackgroundV.visibility = View.GONE
                        fragment.profilePb.visibility = View.GONE
                        if (!success) {
                            Toast.makeText(
                                fragment.requireContext(),
                                "Failed to confirm error. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            for (i in 0..data.size) {
                                var item = data.get(i)
                                if (item.orderId == id) {
                                    data.get(i).isConfirmed = true
                                    break
                                }
                            }
                            Toast.makeText(
                                fragment.requireContext(),
                                "Order confirmed.",
                                Toast.LENGTH_SHORT
                            ).show()
                            notifyDataSetChanged()
                        }
                    }
                }
            }

        return ProfileOrderViewHolder(v)
    }

    override fun onBindViewHolder(holder: ProfileOrderViewHolder, position: Int) {
        holder.bindData(data[position])
        holder.setConfirmBtnListener(fragment.requireActivity()!!.supportFragmentManager, fragment)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    companion object {
        const val TAG: String = "PROFILE ORDER ADAPTER: "
    }
}