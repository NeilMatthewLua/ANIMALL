package com.mobdeve.s15.animall

import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.IndicatorView.draw.controller.DrawController.ClickListener
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import java.util.*

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    //AniMall
    lateinit var listingDescriptionTv: TextView
    lateinit var listingSellerTv: TextView

    lateinit var sliderView: SliderView
    lateinit var adapter: SliderAdapter

    private fun getYukino(): MutableList<Listing> {
        val items = mutableListOf<Listing>()
        items.add(Listing("1", R.drawable.yukino1))
        items.add(Listing("2", R.drawable.yukino2))
        items.add(Listing("3", R.drawable.yukino3))
        items.add(Listing("4", R.drawable.yukino4))
        items.add(Listing("5", R.drawable.yukino5))
        return items
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listing)

//        storage = Firebase.storage
//        storageReference = storage.reference
//
//        files = ArrayList()
//        status = ArrayList()
//
//        btn_upload = findViewById(R.id.btn_upload);
//
//        // on pressing btnSelect SelectImage() is called
//        btn_upload.setOnClickListener {
//            SelectImages();
//        };

        //AniMall

        sliderView = findViewById<SliderView>(R.id.imageSlider)

        adapter = SliderAdapter(this)
        sliderView.setSliderAdapter(adapter)
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM) //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!

        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
//        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH)
        sliderView.setIndicatorSelectedColor(Color.WHITE)
        sliderView.setIndicatorUnselectedColor(Color.GRAY)
//        sliderView.setScrollTimeInSec(3)
//        sliderView.setAutoCycle(true)
//        sliderView.startAutoCycle()


        sliderView.setOnIndicatorClickListener(ClickListener {
            Log.i(
                "GGG",
                "onIndicatorClicked: " + sliderView.getCurrentPagePosition()
            )
        })

        adapter.renewItems(getYukino())

        listingDescriptionTv = findViewById(R.id.listingDescriptionTv)
        listingSellerTv = findViewById(R.id.listingSellerTv)

        listingDescriptionTv.text = "The greatest side character you’ll ever encounter\n" +
                "Will never:\n" +
                "Give you up \n" +
                "Let you down\n" +
                "*Something something*\n" +
                "Desert you\n" +
                "I actually don’t know what’s next "
    }
}
