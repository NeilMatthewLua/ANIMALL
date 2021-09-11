package com.mobdeve.s15.animall

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import kotlinx.android.synthetic.main.fragment_custom_dialog.*

class CustomDialogFragment: DialogFragment() {
    var modalType: String = ""
    var orderId: String = ""
    var orderName: String = ""
    var listingId: String = ""
    var listingName: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.curved_rectangle)
        modalType = arguments?.getString(MODAL_TYPE_KEY, "Modal Type")!!
        if (modalType == MODAL_ORDER_CONFIRM) {
            orderId = arguments?.getString(MODAL_ORDER_ID_KEY, "Order ID")!!
            orderName = arguments?.getString(MODAL_ORDER_NAME_KEY, "Order Name")!!
        } else if (modalType == MODAL_LISTING_CLOSE) {
            listingId = arguments?.getString(MODAL_LISTING_ID_KEY, "Listing ID")!!
            listingName = arguments?.getString(MODAL_LISTING_NAME_KEY, "Listing Name")!!
        }

        return inflater.inflate(R.layout.fragment_custom_dialog, container, false)
    }

    override fun onStart() {
        super.onStart()
//        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
//        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
//        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        if (modalType == MODAL_ORDER_CONFIRM) {
            confirmationTextTv.text = "Are you sure you have received: \n" + orderName
        } else if (modalType == MODAL_LISTING_CLOSE) {
            confirmationTextTv.text = "Are you sure you want to close: \n" + listingName
        }
        dialogCancelBtn.setOnClickListener {
            dismiss()
        }
        dialogConfirmBtn.setOnClickListener {
            var bundle = Bundle()
            if (modalType == MODAL_ORDER_CONFIRM) {
                bundle.putString(MODAL_SUCCESS_KEY, "ok")
                bundle.putString(MODAL_ORDER_ID_KEY, orderId)
                requireActivity().supportFragmentManager
                    .setFragmentResult(MODAL_ORDER_CONFIRM_RESULT, bundle)
            } else if(modalType == MODAL_LISTING_CLOSE) {
                bundle.putString(MODAL_SUCCESS_KEY, "ok")
                bundle.putString(MODAL_LISTING_ID_KEY, listingId)
                requireActivity().supportFragmentManager
                    .setFragmentResult(MODAL_LISTING_CLOSE_RESULT, bundle)
            }
            dismiss()
        }
    }
    companion object {
        val MODAL_TYPE_KEY = "modalType"

        // Types of modals
        val MODAL_ORDER_CONFIRM = "modalOrderConfirm"
        val MODAL_LISTING_CLOSE = "modalListingClose"
        // Results from modals
        val MODAL_ORDER_CONFIRM_RESULT = "modalOrderConfirmResult"
        val MODAL_LISTING_CLOSE_RESULT = "modalListingCloseResult"
        // Items to be returned in result
        val MODAL_ORDER_ID_KEY = "modalOrderIDKey"
        val MODAL_ORDER_NAME_KEY = "modalOrderName"
        val MODAL_LISTING_ID_KEY = "modalListingIDKey"
        val MODAL_LISTING_NAME_KEY = "modalListingNameKey"
        val MODAL_SUCCESS_KEY = "modalOrderConfirmedKey"
    }
}