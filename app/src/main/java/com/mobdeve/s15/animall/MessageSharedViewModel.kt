package com.mobdeve.s15.animall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MessageSharedViewModel : ViewModel() {
    var conversationData : MutableLiveData<ConversationModel> = MutableLiveData()

    fun setListingData(data: ConversationModel) {
        conversationData.value = data
    }

    fun getListingData() : LiveData<ConversationModel> {
        return conversationData
    }
}