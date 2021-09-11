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
import java.util.ArrayList

class ProfileListingAdapter(private val data: ArrayList<ListingModel>, private val fragment: UserProfileFragment) :
    RecyclerView.Adapter<ProfileListingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileListingViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_profile_listing_layout, parent, false)

        fragment.requireActivity().supportFragmentManager
            .setFragmentResultListener(CustomDialogFragment.MODAL_LISTING_CLOSE_RESULT, fragment.viewLifecycleOwner) { key, bundle ->
                val result = bundle.getString(CustomDialogFragment.MODAL_SUCCESS_KEY)
                val id = bundle.getString(CustomDialogFragment.MODAL_LISTING_ID_KEY)
                if (result == "ok") {
                    fragment.lifecycleScope.launch {
                        fragment.profileDimBackgroundV.visibility = View.VISIBLE
                        fragment.profilePb.visibility = View.VISIBLE
                        var result = "false"
                        val closeListing = async(Dispatchers.IO) {
                            result = DatabaseManager.closeListing(id!!)
                        }
                        closeListing.await()
                        fragment.profileDimBackgroundV.visibility = View.GONE
                        fragment.profilePb.visibility = View.GONE
                        if (result == "pending_orders") {
                            Toast.makeText(fragment.requireContext(),"Pending orders. Cannot close listing.", Toast.LENGTH_SHORT).show()
                        } else if (result == "true") {
                            for (i in 0..data.size) {
                                var item = data.get(i)
                                if (item.listingId == id) {
                                    data.get(i).isOpen = false
                                    break
                                }
                            }
                            notifyDataSetChanged()
                        } else if (result == "false") {
                            Toast.makeText(fragment.requireContext(),"Server error. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        return ProfileListingViewHolder(v)
    }

    override fun onBindViewHolder(holder: ProfileListingViewHolder, position: Int) {
        holder.bindData(data[position])
        // TODO: Attach onclick listener to navigate to corresponding listing
        holder.setCloseBtnListener(fragment.requireActivity()!!.supportFragmentManager, fragment)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}