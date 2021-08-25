package com.mobdeve.s15.animall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ListingSharedViewModel : ViewModel() {
    var listingData : MutableLiveData<ListingModel> = MutableLiveData()

    fun setListingData(data: ListingModel) {
        listingData.value = data
    }

    fun getListingData() : LiveData<ListingModel> {
        return listingData
    }

}