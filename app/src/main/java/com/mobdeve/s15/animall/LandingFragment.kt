package com.mobdeve.s15.animall

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_landing.*
import kotlinx.coroutines.*
import java.util.*

class LandingFragment : Fragment() {
    val TAG: String = "LANDING FRAGMENT"
    var data: ArrayList<ListingModel> = ArrayList<ListingModel>()
    var hasRetrieved: Boolean = false
    // RecyclerView components
    lateinit var landingAdapter: LandingAdapter
    // Sort/Filter Adapters
    private var filterAdapter: ArrayAdapter<String>? = null
    private var sortAdapter: ArrayAdapter<String>? = null
    private var sortChoice: String = ""
    private var filterChoice: String = ""
    private var searchChoice: String = ""
    private var userCity: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        lifecycleScope.launch {
            val loggedUser = Firebase.auth.currentUser
            val dataInit = async(Dispatchers.IO) {
                data = DatabaseManager.initializeListingData()
                userCity = DatabaseManager.getUserCity(loggedUser?.email!!)
            }
            Log.d(TAG, "ON CREATE")
            dataInit.await()
            initializeSpinners()
            // Adapter
            landingAdapter = LandingAdapter(data!!, this@LandingFragment)
            landingRecyclerView!!.adapter = landingAdapter
            landingAdapter.notifyDataSetChanged()
            hasRetrieved = true
            landingDimBackgroundV.visibility = View.GONE
            landingPb.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_landing, container, false)
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        if (hasRetrieved) {
            initializeSpinners()
            landingDimBackgroundV.visibility = View.GONE
            landingPb.visibility = View.GONE
        }
        searchBoxTv.setOnEditorActionListener { v, actionId, event ->
            if (event?.action == KeyEvent.ACTION_DOWN) {
                searchChoice = searchBoxTv.text.trim().toString()
                landingDimBackgroundV.visibility = View.VISIBLE
                landingPb.visibility = View.VISIBLE
                lifecycleScope.launch {
                    val dataInit = async(Dispatchers.IO) {
                        data = DatabaseManager.filteredListingData(filterChoice, sortChoice, searchChoice, userCity, requireContext())
                    }
                    dataInit.await()

                    // Adapter
                    landingAdapter = LandingAdapter(data!!, this@LandingFragment)
                    landingRecyclerView!!.adapter = landingAdapter
                    landingAdapter.notifyDataSetChanged()
                    hasRetrieved = true
                    landingDimBackgroundV.visibility = View.GONE
                    landingPb.visibility = View.GONE
                }
                true
            }
            false
        }
        // Layout manager
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        landingRecyclerView!!.layoutManager = linearLayoutManager
        // Adapter
        landingAdapter = LandingAdapter(data!!, this@LandingFragment)
        landingRecyclerView!!.adapter = landingAdapter
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
                    filterChoice = filterOptions.get(position)
                    landingDimBackgroundV.visibility = View.VISIBLE
                    landingPb.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        val dataInit = async(Dispatchers.IO) {
                            data = DatabaseManager.filteredListingData(filterChoice, sortChoice, searchChoice, userCity, requireContext())
                        }
                        dataInit.await()

                        // Adapter
                        landingAdapter = LandingAdapter(data!!, this@LandingFragment)
                        landingRecyclerView!!.adapter = landingAdapter
                        landingAdapter.notifyDataSetChanged()
                        hasRetrieved = true
                        landingDimBackgroundV.visibility = View.GONE
                        landingPb.visibility = View.GONE
                    }
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
                    // TODO CHANGE TO REFERENCES
                    sortChoice = sortOptions.get(position)
                    landingDimBackgroundV.visibility = View.VISIBLE
                    landingPb.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        val dataInit = async(Dispatchers.IO) {
                            data = DatabaseManager.filteredListingData(filterChoice, sortChoice, searchChoice, userCity, requireContext())
                        }
                        dataInit.await()

                        // Adapter
                        landingAdapter = LandingAdapter(data!!, this@LandingFragment)
                        landingRecyclerView!!.adapter = landingAdapter
                        landingAdapter.notifyDataSetChanged()
                        hasRetrieved = true
                        landingDimBackgroundV.visibility = View.GONE
                        landingPb.visibility = View.GONE
                    }
                }
            }
        }

        // Clear selection button
        clearSelectionChip.setOnClickListener{
            filterSpinner.setSelection(0)
            sortSpinner.setSelection(0)
            clearSelectionChip.visibility = View.GONE
            landingDimBackgroundV.visibility = View.VISIBLE
            landingPb.visibility = View.VISIBLE
            filterChoice = ""
            sortChoice = ""
            searchChoice = ""
            lifecycleScope.launch {
                val dataInit = async(Dispatchers.IO) {
                    data = DatabaseManager.initializeListingData()
                }
                Log.d(TAG, "ON CREATE")
                dataInit.await()
                initializeSpinners()
                // Adapter
                landingAdapter = LandingAdapter(data!!, this@LandingFragment)
                landingRecyclerView!!.adapter = landingAdapter
                landingAdapter.notifyDataSetChanged()
                hasRetrieved = true
                landingDimBackgroundV.visibility = View.GONE
                landingPb.visibility = View.GONE
            }
        }
    }
}