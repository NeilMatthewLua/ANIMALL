package com.mobdeve.s15.animall

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_offer_dialog.*

class CustomOfferDialogFragment: DialogFragment() {
    var modalType: String = ""
    var listingId: String = ""
    var listingName: String = ""
    var listingQuantity: Long = 0
    var listingPrice: Long = 0
    var listingTotal: Double = 0.0
    var listingStock: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.curved_rectangle)
        // Check the modal type to be displayed
        modalType = arguments?.getString(MODAL_TYPE_KEY, "Modal Type")!!
        listingName = arguments?.getString(MODAL_LISTING_NAME_KEY, "Listing Name")!!
        listingStock = arguments?.getLong(MODAL_LISTING_STOCK_KEY, 0)!!

        if (modalType == MODAL_ORDER) {
            listingPrice = arguments?.getLong(MODAL_LISTING_PRICE_KEY, 0)!!
        }
        updateDialogCount()
        return inflater.inflate(R.layout.fragment_custom_dialog, container, false)
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        messageProductNameTv.text = listingName
        if (modalType == MODAL_OFFER) {
            profileListingNameTv.text = "Make Offer"
            profileEditListingBtn.text = "Place Offer"
        } else if (modalType == MODAL_ORDER) {
            messageNewAmountLayout.visibility = View.GONE
            profileListingNameTv.text = "Make Order"
            profileEditListingBtn.text = "Place Order"
        }
        minusCountBtn.isEnabled = false
        addCountBtn.setOnClickListener {
            if (listingQuantity + 1 < listingStock) {
                minusCountBtn.isEnabled = false
                listingQuantity += 1
                updateDialogCount()
            } else {
                Toast.makeText(requireContext(),"Cannot input more than stock count.", Toast.LENGTH_LONG).show()
            }
        }
        minusCountBtn.setOnClickListener {
            listingQuantity -= 1
            updateDialogCount()
            if (listingQuantity == 0.toLong()) {
                minusCountBtn.isEnabled = false
            }
        }
        profileDeleteListingBtn.setOnClickListener {
            dismiss()
        }
        profileEditListingBtn.setOnClickListener {
            var bundle = Bundle()
            if (modalType == MODAL_ORDER) {
//                bundle.putString(MODAL_SUCCESS_KEY, "ok")
//                bundle.putString(MODAL_ORDER_ID_KEY, orderId)
//                requireActivity().supportFragmentManager
//                    .setFragmentResult(MODAL_ORDER_CONFIRM_RESULT, bundle)
            } else if(modalType == MODAL_OFFER) {
//                bundle.putString(MODAL_SUCCESS_KEY, "ok")
//                bundle.putString(MODAL_LISTING_ID_KEY, listingId)
//                requireActivity().supportFragmentManager
//                    .setFragmentResult(MODAL_LISTING_CLOSE_RESULT, bundle)
            }
            dismiss()
        }
    }

    fun updateDialogCount() {
        messageQuantityTv.text = listingQuantity.toString()
        messageQuantityTotalTv.text = listingQuantity.toString()
        messagePriceTv.text = listingPrice.toString()
        messageTotalPriceTv.text = (listingQuantity * listingPrice).toString()
    }

    companion object {
        val MODAL_TYPE_KEY = "modalType"

        // Types of modals
        val MODAL_ORDER = "modalOrder"
        val MODAL_OFFER = "modalOffer"

        val MODAL_LISTING_NAME_KEY = "modalListingNameKey"
        val MODAL_LISTING_STOCK_KEY = "modalListingStockKey"
        val MODAL_LISTING_PRICE_KEY = "modalListingPriceKey"
    }
}