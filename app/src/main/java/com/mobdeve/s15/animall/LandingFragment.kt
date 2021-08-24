package com.mobdeve.s15.animall

import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_landing.*
import kotlinx.coroutines.*
import org.w3c.dom.Text
import java.util.*

class LandingFragment : Fragment() {
    lateinit var data: ArrayList<ListingModel>
    // RecyclerView components
    lateinit var recyclerView: RecyclerView
    lateinit var myAdapter: MyAdapter
    // Sort/Filter Adapters
    private var filterAdapter: ArrayAdapter<String>? = null
    private var sortAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val dataInit = async(Dispatchers.IO) {
                data = DataHelper.initializeData()
            }
            dataInit.await()

            initializeSpinners()
            // Layout manager
            val linearLayoutManager = LinearLayoutManager(activity)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            landingRecyclerView!!.layoutManager = linearLayoutManager

            Log.d("LANDING: ", "pass to adapter")
            Log.d("LANDING: ", data.size.toString())
            // Adapter
            myAdapter = MyAdapter(data!!)
            landingRecyclerView!!.adapter = myAdapter
            myAdapter.notifyDataSetChanged()
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
    }

    fun initializeSpinners() {
        // Filter spinner
        val filterOptions = resources.getStringArray(R.array.filter_options).toList()
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
                    view.setTextColor(resources.getColor(R.color.grey))
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
                    view.setTextColor(resources.getColor(R.color.grey))
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