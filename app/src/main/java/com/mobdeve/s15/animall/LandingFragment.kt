package com.mobdeve.s15.animall

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_landing.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

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
    // Bottom navbar
    lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        lifecycleScope.launch {
            try {
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
        bottomNav = requireActivity().findViewById(R.id.bottom_navigatin_view)
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
                bottomNav.visibility = View.INVISIBLE
                lifecycleScope.launch {
                    try {
                        val dataInit = async(Dispatchers.IO) {
                            data = DatabaseManager.filteredListingData(filterChoice, sortChoice, searchChoice, userCity, requireContext())
                        }
                        dataInit.await()

                        landingAdapter = LandingAdapter(data!!, this@LandingFragment)
                        landingRecyclerView!!.adapter = landingAdapter
                        landingAdapter.notifyDataSetChanged()
                        landingDimBackgroundV.visibility = View.GONE
                        landingPb.visibility = View.GONE
                        bottomNav.visibility = View.VISIBLE
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
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
        // Add a scroll listener to show load more button when at the end
        landingRecyclerView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (!landingRecyclerView.canScrollVertically(1) && data.size > 0){
                loadMoreBtn.visibility = View.VISIBLE
                loadMoreBtn.animate().alpha(1.0f)
            } else {
                loadMoreBtn.animate().alpha(0.0f);
                loadMoreBtn.visibility = View.GONE
            }
        }

        loadMoreBtn.setOnClickListener {
            lifecycleScope.launch {
                try {
                    landingDimBackgroundV.visibility = View.VISIBLE
                    landingPb.visibility = View.VISIBLE
                    var newData: ArrayList<ListingModel> = ArrayList()
                    var previousSize : Int = data.size
                    val dataInit = async(Dispatchers.IO) {
                        var lastDocId = data.get(data.size - 1).listingId
                        newData = DatabaseManager.filteredListingData(filterChoice, sortChoice, searchChoice, userCity, requireContext(), lastDocId)
                    }
                    dataInit.await()
                    if (newData.size == 0) {
                        Toast.makeText(requireContext(),"No more listings found.", Toast.LENGTH_LONG).show()
                    } else {
                        data.addAll(newData)
                    }

                    landingAdapter.notifyItemRangeChanged(previousSize, newData.size)
                    landingDimBackgroundV.visibility = View.GONE
                    landingPb.visibility = View.GONE
                } catch (e : Exception) {
                    e.printStackTrace()
                }
            }
        }
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
                        try {
                            val dataInit = async(Dispatchers.IO) {
                                data = DatabaseManager.filteredListingData(filterChoice, sortChoice, searchChoice, userCity, requireContext())
                            }
                            dataInit.await()

                            landingAdapter = LandingAdapter(data!!, this@LandingFragment)
                            landingRecyclerView!!.adapter = landingAdapter
                            landingAdapter.notifyDataSetChanged()
                            landingDimBackgroundV.visibility = View.GONE
                            landingPb.visibility = View.GONE
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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
                        try {
                            val dataInit = async(Dispatchers.IO) {
                                data = DatabaseManager.filteredListingData(filterChoice, sortChoice, searchChoice, userCity, requireContext())
                            }
                            dataInit.await()

                            landingAdapter = LandingAdapter(data!!, this@LandingFragment)
                            landingRecyclerView!!.adapter = landingAdapter
                            landingAdapter.notifyDataSetChanged()
                            landingDimBackgroundV.visibility = View.GONE
                            landingPb.visibility = View.GONE
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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
                try {
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
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}