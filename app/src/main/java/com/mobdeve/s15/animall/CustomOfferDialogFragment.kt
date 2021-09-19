package com.mobdeve.s15.animall

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_offer_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CustomOfferDialogFragment: DialogFragment() {
    var modalType: String = ""
    var listingId: String = ""
    var listingName: String = ""
    var listingQuantity: Long = 1
    var listingPrice: Long = 0
    var listingStock: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.curved_rectangle)
        // Check the modal type to be displayed
        modalType = arguments?.getString(MODAL_TYPE_KEY, "Modal Type")!!
        listingName = arguments?.getString(MODAL_LISTING_NAME_KEY, "Listing Name")!!
        listingStock = arguments?.getLong(MODAL_LISTING_STOCK_KEY, 0)!!
        listingId = arguments?.getString(MODAL_LISTING_ID_KEY, "ID")!!
        Log.d("MODAL",listingName)
        if (modalType == MODAL_ORDER) {
            listingPrice = arguments?.getLong(MODAL_LISTING_PRICE_KEY, 0)!!
            Log.d("MODAL", listingPrice.toString())
        }

        lifecycleScope.launch {
            val job = launch (Dispatchers.IO) {
                listingStock = DatabaseManager.getListingFromId(listingId)!!.stock
            }
            job.join()
        }

        return inflater.inflate(R.layout.fragment_offer_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialogProductNameTv.text = listingName
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
            if (listingQuantity < listingStock) {
                minusCountBtn.isEnabled = true
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
        updateDialogCount()
        productNameEtv2.addTextChangedListener (object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().toIntOrNull() != null && s.toString().toLong() >= 0) {
                    listingPrice = s.toString().toLong()
                    updateDialogCount()
                }
            }
        })
        profileDeleteListingBtn.setOnClickListener {
            dismiss()
        }
        profileEditListingBtn.setOnClickListener {
            var bundle = Bundle()
            if (modalType == MODAL_ORDER) {
                if (messageQuantityTv.text.toString().toLong() > 0) {
                    bundle.putString(MODAL_SUCCESS_KEY, "ok")
                    bundle.putLong(MODAL_QUANTITY_ORDERED_KEY, listingQuantity)
                    bundle.putLong(MODAL_LISTING_PRICE_KEY, listingPrice)
                    requireActivity().supportFragmentManager
                        .setFragmentResult(MODAL_ORDER_RESULT, bundle)
                    dismiss()
                } else {
                    Toast.makeText(requireContext(),"Please input a non-zero quantity.", Toast.LENGTH_LONG).show()
                }
            } else if(modalType == MODAL_OFFER) {
                if (productNameEtv2.text.toString().toIntOrNull() != null && productNameEtv2.text.toString().toLong() > 0
                    && messageQuantityTv.text.toString().toLong() > 0) {
                    bundle.putString(MODAL_SUCCESS_KEY, "ok")
                    bundle.putLong(MODAL_QUANTITY_ORDERED_KEY, listingQuantity)
                    bundle.putLong(MODAL_LISTING_PRICE_KEY, listingPrice)
                    requireActivity().supportFragmentManager
                        .setFragmentResult(MODAL_OFFER_RESULT, bundle)
                    dismiss()
                } else {
                    if (productNameEtv2.text.toString().toIntOrNull() == null)
                        Toast.makeText(requireContext(),"Please input numbers only.", Toast.LENGTH_LONG).show()
                    else if (productNameEtv2.text.toString().toLong() == 0.toLong())
                        Toast.makeText(requireContext(),"Please input a non-zero price.", Toast.LENGTH_LONG).show()
                    else if (listingQuantity == 0.toLong())
                        Toast.makeText(requireContext(),"Please input a non-zero quantity.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun updateDialogCount() {
        messageQuantityTv.text = listingQuantity.toString()
        messageQuantityTotalTv.text = listingQuantity.toString()
        messagePriceTv.text = listingPrice.toString()
        messageTotalPriceTv.text = "= " + (listingQuantity * listingPrice).toString()
    }

    companion object {
        val MODAL_TYPE_KEY = "modalType"

        // Types of modals
        val MODAL_ORDER = "modalOrder"
        val MODAL_OFFER = "modalOffer"

        val MODAL_ORDER_RESULT = "modalOrderResult"
        val MODAL_OFFER_RESULT = "modalOfferResult"

        val MODAL_LISTING_ID_KEY = "modalListingIdKey"
        val MODAL_LISTING_NAME_KEY = "modalListingNameKey"
        val MODAL_LISTING_STOCK_KEY = "modalListingStockKey"
        val MODAL_LISTING_PRICE_KEY = "modalListingPriceKey"
        val MODAL_QUANTITY_ORDERED_KEY = "modalQuantityOrderedKey"
        val MODAL_SUCCESS_KEY = "modalSuccessKey"
    }
}