package com.mobdeve.s15.animall

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

class UserProfileFragment : Fragment() {
    // Db data
    var listingData: ArrayList<ListingModel> = ArrayList<ListingModel>()
    var orderData: ArrayList<OrderModel> = ArrayList<OrderModel>()

    // RecyclerView components
    lateinit var profileListingAdapter: ProfileListingAdapter
    lateinit var profileOrderAdapter: ProfileOrderAdapter

    var currentUser: String = ""
    var isOwnProfile: Boolean = true
    var hasRetrieved: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            if (arguments?.get(SELLER_EMAIL).toString().isNotBlank()) {
                val sellerEmail = arguments?.get(SELLER_EMAIL).toString()
                val dataInit = async(Dispatchers.IO) {
                    currentUser = DatabaseManager.getUserName(sellerEmail)
                    listingData = DatabaseManager.getUserListings(sellerEmail, true)
                }
                dataInit.await()
                isOwnProfile = false
                profileImageIv.visibility = View.GONE
            } else {
                val loggedUser = Firebase.auth.currentUser
                val dataInit = async(Dispatchers.IO) {
                    currentUser = DatabaseManager.getUserName(loggedUser?.email!!)
                    listingData = DatabaseManager.getUserListings(loggedUser?.email!!)
                    orderData = DatabaseManager.getUserOrders(loggedUser?.email!!)
                }
                dataInit.await()
                Picasso.get().load(loggedUser?.photoUrl)
                    .error(R.drawable.ic_error)
                    .placeholder(R.drawable.progress_animation)
                    .into(profileImageIv);
            }

            userProfileTv.text = currentUser

            if (!isOwnProfile) {
                profileListingBtn.visibility = View.GONE
                profileOrderBtn.visibility = View.GONE
                profileEditLocationBtn.visibility = View.GONE
            } else {
                profileListingBtn.setOnClickListener {
                    profileListingBtn.setBackgroundColor(resources.getColor(R.color.primary_green))
                    profileListingBtn.setTextColor(resources.getColor(R.color.white))
                    profileOrderBtn.setBackgroundColor(resources.getColor(R.color.white))
                    profileOrderBtn.setTextColor(resources.getColor(R.color.black))
                    profileListingAdapter =
                        ProfileListingAdapter(listingData!!, this@UserProfileFragment, isOwnProfile)
                    profileRecyclerView!!.adapter = profileListingAdapter
                    profileListingAdapter.notifyDataSetChanged()
                }

                profileOrderBtn.setOnClickListener {
                    profileOrderBtn.setBackgroundColor(resources.getColor(R.color.primary_green))
                    profileOrderBtn.setTextColor(resources.getColor(R.color.white))
                    profileListingBtn.setBackgroundColor(resources.getColor(R.color.white))
                    profileListingBtn.setTextColor(resources.getColor(R.color.black))
                    profileOrderAdapter = ProfileOrderAdapter(orderData!!, this@UserProfileFragment)
                    profileRecyclerView!!.adapter = profileOrderAdapter
                    profileOrderAdapter.notifyDataSetChanged()
                }

                profileEditLocationBtn.setOnClickListener {
                    getLocation.launch(Intent(context, LocationActivity::class.java))
                }

                profileEditLocationBtn.visibility = View.VISIBLE
                profileListingBtn.visibility = View.VISIBLE
                profileOrderBtn.visibility = View.VISIBLE
                profileLogoutBtn.visibility = View.VISIBLE
                profileImageContainerCv.visibility = View.VISIBLE
            }

            profileListingAdapter =
                ProfileListingAdapter(listingData!!, this@UserProfileFragment, isOwnProfile)
            profileRecyclerView!!.adapter = profileListingAdapter
            profileListingAdapter.notifyDataSetChanged()

            hasRetrieved = true
            profileDimBackgroundV.visibility = View.GONE
            profilePb.visibility = View.GONE
        }
    }

    private val getLocation = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val value = intent?.getStringExtra("PREF_LOC")

            val userRef = Firebase.auth.currentUser?.let {
                DatabaseManager.getInstance().collection("users").document(it.uid)
            }
            userRef?.get()?.addOnCompleteListener { documentTask ->
                if (documentTask.isSuccessful) {
                    if (documentTask.result.data != null) {
                        userRef.update("preferredLocation", value)
                    }
                }
            }
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
        profileListingAdapter =
            ProfileListingAdapter(listingData!!, this@UserProfileFragment, isOwnProfile)
        profileRecyclerView!!.adapter = profileListingAdapter

        profileLogoutBtn.setOnClickListener {
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

    companion object {
        const val SELLER_EMAIL = "sellerEmail"
        const val TAG: String = "USER PROFILE"
    }
}