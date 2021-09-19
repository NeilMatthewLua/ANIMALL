package com.mobdeve.s15.animall

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import kotlinx.android.synthetic.main.fragment_view_listing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

class ViewListingFragment : Fragment() {
    private val viewModel: ListingSharedViewModel by activityViewModels()
    lateinit var sliderView: SliderView
    lateinit var adapterListing: ListingSliderAdapter
    lateinit var loggedUser: FirebaseUser
    var user: UserModel? = null
    var conversation: ConversationModel? = null
    lateinit var listing: ListingModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_listing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getListingData().observe(viewLifecycleOwner, {
            // Update the list UI
            Log.d("LANDING: ", it.name)

            sliderView = requireActivity().findViewById<SliderView>(R.id.imageSlider)

            adapterListing = ListingSliderAdapter(requireContext())
            adapterListing.renewItems(it.photos)

            sliderView.setSliderAdapter(adapterListing)
            sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM) //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!

            sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
            sliderView.indicatorSelectedColor = Color.WHITE
            sliderView.indicatorUnselectedColor = Color.GRAY

            sliderView.setOnIndicatorClickListener {
                Log.i(
                    "GGG",
                    "onIndicatorClicked: " + sliderView.getCurrentPagePosition()
                )
            }

            // Update details
            listingNameTv.text = it.name
            listingStockTv.text = "${it.stock} in Stock"
            listingPriceTv.text = it.unitPrice.toString()
            listingCategoryC.text = it.category
            listingLocationTv.text = it.preferredLocation
            listingClosedC.visibility = if (it.isOpen) View.GONE else View.VISIBLE
            listingDescriptionTv.text = it.description

            listing = it

            lifecycleScope.launch {
                val userInit = async(Dispatchers.IO) {
                    user = DatabaseManager.getUserViaEmail(it.seller)
                }
                userInit.await()
                loggedUser = Firebase.auth.currentUser!!

                if (user != null) {
                    listingSellerTv.text = user!!.name
                }

                viewSellerProfileBtn.setOnClickListener {
                    val sellerProfilePage = UserProfileFragment()
                    val args = Bundle()
                    args.putString(UserProfileFragment.SELLER_EMAIL, user!!.email)
                    sellerProfilePage.arguments = args
                    it.findNavController().navigate(R.id.profileFragment, args)
                }

                adapterListing.renewItems(it.photos)

                if (loggedUser.email!! != user!!.email) {
                    listingContactBtn.setOnClickListener { view ->
                        lifecycleScope.launch {
                            val convoInit = async(Dispatchers.IO) {
                                conversation = DatabaseManager.getConversation(
                                    it.listingId,
                                    loggedUser.email!!
                                )
                            }
                            convoInit.await()

                            //If no conversation has been made yet
                            if (conversation == null) {
                                val convoID = UUID.randomUUID().toString()
                                val viewModel: MessageSharedViewModel by activityViewModels()

                                viewModel.setListingData(
                                    ConversationModel(
                                        user!!.email,
                                        loggedUser.email!!,
                                        listing.listingId,
                                        listing.name,
                                        listing.photos[0],
                                        convoID
                                    ), true
                                )

                                view.findNavController().navigate(R.id.messageFragment)
                            } else {
                                val viewModel: MessageSharedViewModel by activityViewModels()
                                viewModel.setListingData(conversation!!, false)

                                view.findNavController().navigate(R.id.messageFragment)
                            }
                        }
                    }
                } else {
                    listingActionLinearLayout.visibility = View.GONE
                }
            }

        })
    }

    companion object {
        const val TAG = "ViewListingActivity"
    }
}