package com.mobdeve.s15.animall

object DataHelper {
    fun initializeData(): ArrayList<ListingModel> {
        val usernames = arrayOf("Cool", "Beans")
        val listingImages = arrayOf("https://images.unsplash.com/photo-1627735754418-3b581127eab1?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=700&q=80",
            "https://images.unsplash.com/photo-1629658742161-74f6fa2a9576?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=634&q=80")
        val listingImages2 = arrayOf("https://images.unsplash.com/photo-1629368713374-31b95085d63e?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=634&q=80")

        val data = ArrayList<ListingModel>()
        data.add(ListingModel(
            "Clothing",
            "These are socks.",
            "Sock on Deez",
            "Makati City",
            usernames[0],
            69,
            128.68,
            listingImages
        ))
        data.add(ListingModel(
            "Clothing",
            "These are socks.",
            "Sock on Deez",
            "Makati City",
            usernames[1],
            69,
            128.70,
            listingImages2
        ))

        return data;
    }
}