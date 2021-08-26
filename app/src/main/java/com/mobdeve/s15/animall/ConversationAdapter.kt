package com.mobdeve.s15.animall

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class ConversationAdapter(private val data: ArrayList<ConversationModel>, private val fragment: ConversationFragment) :
    RecyclerView.Adapter<ConversationViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val v =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.single_message_layout, parent, false)
        return ConversationViewHolder(v)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bindData(data[position])

        holder.conversationLayout.setOnClickListener {
            val viewModel : MessageSharedViewModel by fragment.activityViewModels()
            viewModel.setListingData(data[position])
//            for (i in 0 until fragment?.requireActivity().supportFragmentManager?.backStackEntryCount) {
//                fragment.activity?.supportFragmentManager?.popBackStack()
//            }
            it.findNavController().navigate(R.id.messageFragment)
//            val transaction = fragment?.requireActivity().supportFragmentManager.beginTransaction()
//            transaction?.remove(ViewListingFragment())
//            transaction?.replace(R.id.nav_host_fragment, ViewListingFragment())
//            transaction?.addToBackStack(null);
//            transaction?.commit()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}