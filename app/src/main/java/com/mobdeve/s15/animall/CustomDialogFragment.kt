package com.mobdeve.s15.animall

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import kotlinx.android.synthetic.main.fragment_custom_dialog.*

class CustomDialogFragment: DialogFragment() {
    var orderName: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.curved_rectangle)
        orderName = arguments?.getString("orderName", "Order Name")!!
        return inflater.inflate(R.layout.fragment_custom_dialog, container, false)
    }

    override fun onStart() {
        super.onStart()
//        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
//        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
//        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        confirmationTextTv.text = "Are you sure you have received: \n" + orderName
        dialogCancelBtn.setOnClickListener {
            dismiss()
        }
        dialogConfirmBtn.setOnClickListener {
            var bundle = Bundle()
            bundle.putString("orderConfirmed", "ok")
            requireActivity().supportFragmentManager
                .setFragmentResult("dialogResult", bundle)
            dismiss()
        }
    }

}