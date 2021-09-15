package com.mobdeve.s15.animall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MessageSharedViewModel : ViewModel() {
    var conversationData : MutableLiveData<ConversationModel> = MutableLiveData()
    var isFirstData: Boolean = true

    fun setListingData(data: ConversationModel, isFirst: Boolean) {
        conversationData.value = data
        isFirstData = isFirst
    }

    fun getListingData() : LiveData<ConversationModel> {
        return conversationData
    }

    fun getIsFirst() : Boolean {
        return isFirstData
    }
}