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

class ProfileListingAdapter(private val data: ArrayList<ListingModel>, private val fragment: UserProfileFragment, private val isOwnProfile: Boolean) :
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
                            Toast.makeText(fragment.requireContext(),"Listing closed.", Toast.LENGTH_SHORT).show()
                            notifyDataSetChanged()
                        } else if (result == "false") {
                            Toast.makeText(fragment.requireContext(),"Server error. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        fragment.requireActivity().supportFragmentManager
            .setFragmentResultListener(CustomDialogFragment.MODAL_LISTING_DELETE_RESULT, fragment.viewLifecycleOwner) { key, bundle ->
                val result = bundle.getString(CustomDialogFragment.MODAL_SUCCESS_KEY)
                val id = bundle.getString(CustomDialogFragment.MODAL_LISTING_ID_KEY)
                if (result == "ok") {
                    fragment.lifecycleScope.launch {
                        fragment.profileDimBackgroundV.visibility = View.VISIBLE
                        fragment.profilePb.visibility = View.VISIBLE
                        var result = "false"
                        val deleteListing = async(Dispatchers.IO) {
                            result = DatabaseManager.deleteListing(id!!)
                        }
                        deleteListing.await()
                        fragment.profileDimBackgroundV.visibility = View.GONE
                        fragment.profilePb.visibility = View.GONE
                        if (result == "pending_orders") {
                            Toast.makeText(fragment.requireContext(),"Pending orders. Cannot delete listing.", Toast.LENGTH_LONG).show()
                        } else if (result == "true") {
                            for (i in 0..data.size) {
                                var item = data.get(i)
                                if (item.listingId == id) {
                                    data.removeAt(i)
                                    break
                                }
                            }
                            Toast.makeText(fragment.requireContext(),"Listing deleted.", Toast.LENGTH_LONG).show()
                            notifyDataSetChanged()
                        } else if (result == "false") {
                            Toast.makeText(fragment.requireContext(),"Server error. Please try again.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

        fragment.requireActivity().supportFragmentManager
            .setFragmentResultListener(CustomDialogFragment.MODAL_LISTING_EDIT_RESULT, fragment.viewLifecycleOwner) { key, bundle ->
                val result = bundle.getString(CustomDialogFragment.MODAL_SUCCESS_KEY)
                val id = bundle.getString(CustomDialogFragment.MODAL_LISTING_ID_KEY)
                val newStock = bundle.getLong(CustomDialogFragment.MODAL_LISTING_STOCK_KEY)
                if (result == "ok") {
                    fragment.lifecycleScope.launch {
                        fragment.profileDimBackgroundV.visibility = View.VISIBLE
                        fragment.profilePb.visibility = View.VISIBLE
                        var result = false
                        val closeListing = async(Dispatchers.IO) {
                            result = DatabaseManager.editListing(id!!, newStock)
                        }
                        closeListing.await()
                        fragment.profileDimBackgroundV.visibility = View.GONE
                        fragment.profilePb.visibility = View.GONE
                        if (result) {
                            for (i in 0..data.size) {
                                var item = data.get(i)
                                if (item.listingId == id) {
                                    data.get(i).stock = newStock
                                    break
                                }
                            }
                            Toast.makeText(fragment.requireContext(),"Listing edited.", Toast.LENGTH_LONG).show()
                            notifyDataSetChanged()
                        } else {
                            Toast.makeText(fragment.requireContext(),"Server error. Please try again.", Toast.LENGTH_LONG).show()
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
        holder.setDeleteBtnListener(fragment.requireActivity()!!.supportFragmentManager, fragment)
        holder.setEditBtnListener(fragment.requireActivity()!!.supportFragmentManager, fragment)
        if (!isOwnProfile) {
            holder.hideBtns()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}