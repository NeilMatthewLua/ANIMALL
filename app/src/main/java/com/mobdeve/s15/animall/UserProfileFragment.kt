package com.mobdeve.s15.animall

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.ArrayList

class UserProfileFragment : Fragment() {
    // Db data
    var listingData: ArrayList<ListingModel> = ArrayList<ListingModel>()
    var orderData: ArrayList<ListingModel> = ArrayList<ListingModel>()

    // RecyclerView components
    lateinit var profileListingAdapter: ProfileListingAdapter

    var currentUser: String = ""
    var hasRetrieved: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Compare firebase user with profile user
        lifecycleScope.launch {
            val dataInit = async(Dispatchers.IO) {
                currentUser = DatabaseManager.getUserName("carlos_shi@dlsu.edu.ph")
                listingData = DatabaseManager.getUserListings("carlos_shi@dlsu.edu.ph")
            }
            dataInit.await()
            userProfileTv.text = currentUser

            profileListingAdapter = ProfileListingAdapter(listingData!!, this@UserProfileFragment)
            profileRecyclerView!!.adapter = profileListingAdapter
            profileListingAdapter.notifyDataSetChanged()
//             Adapter
//            myAdapter = MyAdapter(data!!, this@UserProfileFragment)
//            landingRecyclerView!!.adapter = myAdapter
//            myAdapter.notifyDataSetChanged()
            hasRetrieved = true
            profileDimBackgroundV.visibility = View.GONE
            profilePb.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: replace user with firebase auth

        if (hasRetrieved) {
            profileDimBackgroundV.visibility = View.GONE
            profilePb.visibility = View.GONE
        }
        // Layout manager
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        profileRecyclerView!!.layoutManager = linearLayoutManager
        // Adapter
        profileListingAdapter = ProfileListingAdapter(listingData!!, this@UserProfileFragment)
        profileRecyclerView!!.adapter = profileListingAdapter
    }
}