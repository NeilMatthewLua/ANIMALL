package com.mobdeve.s15.animall

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_user_profile.*
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.ArrayList

class UserProfileFragment : Fragment() {
    val TAG: String = "USER PROFILE"
    // Db data
    var listingData: ArrayList<ListingModel> = ArrayList<ListingModel>()
    var orderData: ArrayList<OrderModel> = ArrayList<OrderModel>()

    // RecyclerView components
    lateinit var profileListingAdapter: ProfileListingAdapter
    lateinit var profileOrderAdapter: ProfileOrderAdapter

    var currentUser: String = ""
    var hasRetrieved: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Compare firebase user with profile user
        lifecycleScope.launch {
            val loggedUser = Firebase.auth.currentUser
            val dataInit = async(Dispatchers.IO) {
                currentUser = DatabaseManager.getUserName(loggedUser?.email!!)
                listingData = DatabaseManager.getUserListings(loggedUser?.email!!)
                orderData = DatabaseManager.getUserOrders(loggedUser?.email!!)
            }
            dataInit.await()
            Picasso.get().
            load(loggedUser?.photoUrl)
                .error(R.drawable.ic_error)
                .placeholder(R.drawable.progress_animation)
                .into(profileImageIv);

            userProfileTv.text = currentUser

            profileListingAdapter = ProfileListingAdapter(listingData!!, this@UserProfileFragment)
            profileRecyclerView!!.adapter = profileListingAdapter
            profileListingAdapter.notifyDataSetChanged()

            profileListingBtn.setOnClickListener{
                profileListingBtn.setBackgroundColor(getResources().getColor(R.color.primary_green))
                profileListingBtn.setTextColor(getResources().getColor(R.color.white))
                profileOrderBtn.setBackgroundColor(getResources().getColor(R.color.white))
                profileOrderBtn.setTextColor(getResources().getColor(R.color.black))
                profileListingAdapter = ProfileListingAdapter(listingData!!, this@UserProfileFragment)
                profileRecyclerView!!.adapter = profileListingAdapter
                profileListingAdapter.notifyDataSetChanged()
            }

            profileOrderBtn.setOnClickListener{
                profileOrderBtn.setBackgroundColor(getResources().getColor(R.color.primary_green))
                profileOrderBtn.setTextColor(getResources().getColor(R.color.white))
                profileListingBtn.setBackgroundColor(getResources().getColor(R.color.white))
                profileListingBtn.setTextColor(getResources().getColor(R.color.black))
                profileOrderAdapter = ProfileOrderAdapter(orderData!!, this@UserProfileFragment)
                profileRecyclerView!!.adapter = profileOrderAdapter
                profileOrderAdapter.notifyDataSetChanged()
            }

            hasRetrieved = true
            profileDimBackgroundV.visibility = View.GONE
            profilePb.visibility = View.GONE

            profileEditLocationBtn.visibility = View.VISIBLE
            profileListingBtn.visibility = View.VISIBLE
            profileOrderBtn.visibility = View.VISIBLE
            profileLogoutBtn.visibility = View.VISIBLE
            profileImageContainerCv.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "SETTING")
        requireActivity().supportFragmentManager
            .setFragmentResultListener("dialogResult", viewLifecycleOwner) { key, bundle ->
                val result = bundle.getString("orderConfirmed")
                if (result == "ok") {
                    Log.d(TAG, "RESULT OK")
                }
            }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: replace user with firebase auth

        if (hasRetrieved) {
            profileDimBackgroundV.visibility = View.GONE
            profilePb.visibility = View.GONE
            profileEditLocationBtn.visibility = View.VISIBLE
            profileListingBtn.visibility = View.VISIBLE
            profileOrderBtn.visibility = View.VISIBLE
            profileLogoutBtn.visibility = View.VISIBLE
            profileImageContainerCv.visibility = View.VISIBLE
        }
        // Layout manager
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        profileRecyclerView!!.layoutManager = linearLayoutManager
        // Adapter
        profileListingAdapter = ProfileListingAdapter(listingData!!, this@UserProfileFragment)
        profileRecyclerView!!.adapter = profileListingAdapter

        profileLogoutBtn.setOnClickListener{
            // Logout of auth
            Firebase.auth.signOut()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            googleSignInClient.signOut()
            // Redirect back to sign up page
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
    }
}