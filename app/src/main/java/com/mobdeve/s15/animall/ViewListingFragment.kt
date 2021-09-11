package com.mobdeve.s15.animall

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.IndicatorView.draw.controller.DrawController
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import kotlinx.android.synthetic.main.fragment_view_listing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ViewListingFragment : Fragment() {
    val TAG = "ViewListingActivity"
    // TODO: Add in a close listing functionality
    // TODO: Update location
    // TODO: Make order/contact seller
    private val viewModel: ListingSharedViewModel by activityViewModels()
    lateinit var sliderView: SliderView
    lateinit var adapterListing: ListingSliderAdapter
    lateinit var loggedUser: FirebaseUser
    var user: UserModel? = null
    var conversation: ConversationModel? = null
    lateinit var listing: ListingModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
//        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH)
            sliderView.setIndicatorSelectedColor(Color.WHITE)
            sliderView.setIndicatorUnselectedColor(Color.GRAY)
//        sliderView.setScrollTimeInSec(3)
//        sliderView.setAutoCycle(true)
//        sliderView.startAutoCycle()

            sliderView.setOnIndicatorClickListener(DrawController.ClickListener {
                Log.i(
                    "GGG",
                    "onIndicatorClicked: " + sliderView.getCurrentPagePosition()
                )
            })

            // Update details
            listingNameTv.text = it.name
            listingStockTv.text = "${it.stock} in Stock"
            listingPriceTv.text = it.unitPrice.toString()
            listingCategoryC.text = it.category
            listingLocationTv.text = it.preferredLocation
            listingClosedC.visibility = if(it.isOpen) View.GONE else View.VISIBLE
            listingDescriptionTv.text = it.description

            listing = it

            lifecycleScope.launch {
                val userInit = async(Dispatchers.IO) {
                    user = DatabaseManager.getUserViaEmail(it.seller)
                }
                userInit.await()
                loggedUser = Firebase.auth.currentUser!!

                if(user != null) {
                    listingSellerTv.text = user!!.name
                }
                adapterListing.renewItems(it.photos)

                if (loggedUser.email!! != user!!.email) {
                    listingContactBtn.setOnClickListener { view ->
                        lifecycleScope.launch {
                            val convoInit = async(Dispatchers.IO) {
                                conversation = DatabaseManager.getConversation(it.id, loggedUser.email!!)
                            }
                            convoInit.await()

                            //If no conversation has been made yet
                            if (conversation == null) {
                                val db = DatabaseManager.getInstance()

                                db.collection(MyFirestoreReferences.CONVERSATIONS_COLLECTION)
                                    .add(
                                        ConversationModel(
                                            user!!.email,
                                            loggedUser.email!!,
                                            listing.id,
                                            listing.name,
                                            listing.photos[0]
                                        )
                                    )
                                    .addOnSuccessListener {
                                        val viewModel : MessageSharedViewModel by activityViewModels()
                                        viewModel.setListingData(ConversationModel(
                                            user!!.email,
                                            loggedUser.email!!,
                                            listing.id,
                                            listing.name,
                                            listing.photos[0],
                                            it.id
                                        ))

                                        view.findNavController().navigate(R.id.messageFragment)
                                    }
                                    .addOnFailureListener { e ->
                                        Log.i("ViewListingFragment", e.toString())
                                    }
                            }
                            else {
                                val viewModel : MessageSharedViewModel by activityViewModels()
                                viewModel.setListingData(conversation!!)

                                view.findNavController().navigate(R.id.messageFragment)
                            }
                        }
                    }
                }
                else {
                    listingActionLinearLayout.visibility = View.GONE
                }
            }

        })

    }
}