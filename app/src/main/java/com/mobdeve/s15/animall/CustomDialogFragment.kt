package com.mobdeve.s15.animall

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_custom_dialog.*

class CustomDialogFragment : DialogFragment() {
    var modalType: String = ""
    var orderId: String = ""
    var orderName: String = ""
    var listingId: String = ""
    var listingName: String = ""
    var listingStock: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.curved_rectangle)
        // Check the modal type to be displayed
        modalType = arguments?.getString(MODAL_TYPE_KEY, "Modal Type")!!
        if (modalType == MODAL_ORDER_CONFIRM) {
            orderId = arguments?.getString(MODAL_ORDER_ID_KEY, "Order ID")!!
            orderName = arguments?.getString(MODAL_ORDER_NAME_KEY, "Order Name")!!
        } else if (modalType == MODAL_LISTING_CLOSE || modalType == MODAL_LISTING_DELETE
            || modalType == MODAL_LISTING_EDIT
        ) {
            listingId = arguments?.getString(MODAL_LISTING_ID_KEY, "Listing ID")!!
            listingName = arguments?.getString(MODAL_LISTING_NAME_KEY, "Listing Name")!!
            // Include stock count for edit listing
            if (modalType == MODAL_LISTING_EDIT)
                listingStock = arguments?.getLong(MODAL_LISTING_STOCK_KEY, 0)!!
        }

        return inflater.inflate(R.layout.fragment_custom_dialog, container, false)
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        editListingCl.visibility = View.GONE
        if (modalType == MODAL_ORDER_CONFIRM) {
            confirmationTextTv.text = "Are you sure you have received: \n" + orderName
        } else if (modalType == MODAL_LISTING_CLOSE) {
            confirmationTextTv.text = "Are you sure you want to close: \n" + listingName
        } else if (modalType == MODAL_LISTING_DELETE) {
            confirmationTextTv.text = "Are you sure you want to delete: \n" + listingName
        } else if (modalType == MODAL_LISTING_EDIT) {
            confirmationTextTv.text = "Update the stock count for: \n" + listingName
            editListingCl.visibility = View.VISIBLE
            currentStockCountTv.text = "Current stock count: " + listingStock.toString()
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
            } else if (modalType == MODAL_LISTING_CLOSE) {
                bundle.putString(MODAL_SUCCESS_KEY, "ok")
                bundle.putString(MODAL_LISTING_ID_KEY, listingId)
                requireActivity().supportFragmentManager
                    .setFragmentResult(MODAL_LISTING_CLOSE_RESULT, bundle)
            } else if (modalType == MODAL_LISTING_DELETE) {
                bundle.putString(MODAL_SUCCESS_KEY, "ok")
                bundle.putString(MODAL_LISTING_ID_KEY, listingId)
                requireActivity().supportFragmentManager
                    .setFragmentResult(MODAL_LISTING_DELETE_RESULT, bundle)
            } else if (modalType == MODAL_LISTING_EDIT) {
                var newStockCount = updatedStockCountTv.text.toString()
                if (newStockCount.toIntOrNull() != null && newStockCount.toLong() > 0) {
                    bundle.putString(MODAL_SUCCESS_KEY, "ok")
                    bundle.putString(MODAL_LISTING_ID_KEY, listingId)
                    bundle.putLong(MODAL_LISTING_STOCK_KEY, newStockCount.toLong())
                    requireActivity().supportFragmentManager
                        .setFragmentResult(MODAL_LISTING_EDIT_RESULT, bundle)
                } else {
                    if (newStockCount.toIntOrNull() == null)
                        Toast.makeText(
                            requireContext(),
                            "Please input numbers only.",
                            Toast.LENGTH_LONG
                        ).show()
                    else if (newStockCount.toLong() > 0)
                        Toast.makeText(
                            requireContext(),
                            "Please input non-negative numbers only.",
                            Toast.LENGTH_LONG
                        ).show()
                }

            }
            dismiss()
        }
    }

    companion object {
        val MODAL_TYPE_KEY = "modalType"

        // Types of modals
        val MODAL_ORDER_CONFIRM = "modalOrderConfirm"
        val MODAL_LISTING_CLOSE = "modalListingClose"
        val MODAL_LISTING_DELETE = "modalListingDelete"
        val MODAL_LISTING_EDIT = "modalListingEdit"

        // Results from modals
        val MODAL_ORDER_CONFIRM_RESULT = "modalOrderConfirmResult"
        val MODAL_LISTING_CLOSE_RESULT = "modalListingCloseResult"
        val MODAL_LISTING_DELETE_RESULT = "modalListingDeleteResult"
        val MODAL_LISTING_EDIT_RESULT = "modalListingEditResult"

        // Items to be returned in result
        val MODAL_ORDER_ID_KEY = "modalOrderIDKey"
        val MODAL_ORDER_NAME_KEY = "modalOrderName"
        val MODAL_LISTING_ID_KEY = "modalListingIDKey"
        val MODAL_LISTING_NAME_KEY = "modalListingNameKey"
        val MODAL_LISTING_STOCK_KEY = "modalListingStockKey"
        val MODAL_SUCCESS_KEY = "modalOrderConfirmedKey"
    }
}