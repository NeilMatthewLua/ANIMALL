package com.mobdeve.s15.animall

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import android.util.Log
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_landing_layout.view.*
import java.util.*


class MyAdapter(private val data: ArrayList<ListingModel>, private val fragment: LandingFragment) :
    RecyclerView.Adapter<LandingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LandingViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_landing_layout, parent, false)

        return LandingViewHolder(v)
    }

    override fun onBindViewHolder(holder: LandingViewHolder, position: Int) {
        holder.bindData(data[position])
        val viewModel: ListingSharedViewModel by fragment.activityViewModels()
//        viewModel.getListingData().observe(fragment.viewLifecycleOwner, Observer {
//            // Update the list UI
//            Log.d("LANDING ADAPTER: ",)
//        })
        viewModel.setListingData(data[position])
        val listingData : ListingModel = data[position]
        // Attach onclick listener to holder
        holder.itemContainerLayout.setOnClickListener {
            val viewModel : ListingSharedViewModel by fragment.activityViewModels()
            viewModel.setListingData(data[position])
//            for (i in 0 until fragment?.requireActivity().supportFragmentManager?.backStackEntryCount) {
//                fragment.activity?.supportFragmentManager?.popBackStack()
//            }
            it.findNavController().navigate(R.id.viewListingFragment)
//            val transaction = fragment?.requireActivity().supportFragmentManager.beginTransaction()
//            transaction?.remove(ViewListingFragment())
//            transaction?.replace(R.id.nav_host_fragment, ViewListingFragment())
//            transaction?.addToBackStack(null);
//            transaction?.commit()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}