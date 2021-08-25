package com.mobdeve.s15.animall

import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_landing.*
import kotlinx.android.synthetic.main.activity_landing.dimBackgroundV
import kotlinx.android.synthetic.main.fragment_add_listing.*
import kotlinx.coroutines.*
import org.w3c.dom.Text
import java.util.*

class LandingFragment : Fragment() {
    val TAG: String = "LANDING FRAGMENT"
    var data: ArrayList<ListingModel> = ArrayList<ListingModel>()
    var hasRetrieved: Boolean = false
    // RecyclerView components
    lateinit var myAdapter: MyAdapter
    // Sort/Filter Adapters
    private var filterAdapter: ArrayAdapter<String>? = null
    private var sortAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        lifecycleScope.launch {
            val dataInit = async(Dispatchers.IO) {
                data = DataHelper.initializeData()
            }
            Log.d(TAG, "ON CREATE")
            dataInit.await()
            initializeSpinners()
            // Adapter
            myAdapter = MyAdapter(data!!, this@LandingFragment)
            landingRecyclerView!!.adapter = myAdapter
            myAdapter.notifyDataSetChanged()
            hasRetrieved = true
            dimBackgroundV.visibility = View.GONE
            landingPb.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_landing, container, false)
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        if (hasRetrieved) {
            initializeSpinners()
            dimBackgroundV.visibility = View.GONE
            landingPb.visibility = View.GONE
        }
        // Layout manager
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        landingRecyclerView!!.layoutManager = linearLayoutManager
        // Adapter
        myAdapter = MyAdapter(data!!, this@LandingFragment)
        landingRecyclerView!!.adapter = myAdapter
    }

    fun initializeSpinners() {
        // Filter spinner
        val filterOptions = resources.getStringArray(R.array.category_options).toList()
        filterAdapter = object: ArrayAdapter<String>(requireContext(),android.R.layout.simple_spinner_item,filterOptions) {
            override fun isEnabled(position: Int): Boolean {
                // Disable the first item from Spinner
                // First item will be used for hint
                return position != 0
            }
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view: TextView = super.getDropDownView(position, convertView, parent) as TextView
                //set the color of first item in the drop down list to gray
                if(position == 0) {
                    view.setTextColor(resources.getColor(R.color.primary_gray))
                }
                return view
            }
        }
        // Specify the layout to use when the list of choices appears
        filterAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        filterSpinner.adapter = filterAdapter
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // TODO: update the listings
                if (position != 0) {
                    clearSelectionChip.visibility = View.VISIBLE
                }
            }
        }

        // Sort spinner
        val sortOptions = resources.getStringArray(R.array.sort_options).toList()
        sortAdapter = object: ArrayAdapter<String>(requireContext(),android.R.layout.simple_spinner_item,sortOptions) {
            override fun isEnabled(position: Int): Boolean {
                // Disable the first item from Spinner
                // First item will be used for hint
                return position != 0
            }
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view: TextView = super.getDropDownView(position, convertView, parent) as TextView
                //set the color of first item in the drop down list to gray
                if(position == 0) {
                    view.setTextColor(resources.getColor(R.color.primary_gray))
                }
                return view
            }
        }
        // Specify the layout to use when the list of choices appears
        sortAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        sortSpinner.adapter = sortAdapter
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // TODO: update the listings
                if (position != 0) {
                    clearSelectionChip.visibility = View.VISIBLE
                }
            }
        }

        // Clear selection button
        clearSelectionChip.setOnClickListener{
            filterSpinner.setSelection(0)
            sortSpinner.setSelection(0)
            clearSelectionChip.visibility = View.GONE
        }
    }
}