package com.mobdeve.s15.animall

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import java.util.*
import kotlin.collections.ArrayList

class CreateListingActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener  {

    val TAG = "ADD LISTING ACITIVTY"

    private lateinit var cancelBtn: Button
    private lateinit var productNameEtv: EditText
    private lateinit var productNameErrorTv: TextView
    private lateinit var productQuantityEtv: EditText
    private lateinit var productQuantityErrorTv: TextView
    private lateinit var productPriceEtv: EditText
    private lateinit var productPriceErrorTv: TextView
    private lateinit var productCategoryErrorTv: TextView
    private lateinit var productCategorySp: Spinner
    private lateinit var productDescriptionEtv: EditText
    private lateinit var productDescriptionErrorTv: TextView
    private lateinit var productUploadBtn: Button

    // Image tryouts
    private lateinit var imageSliderCv: CardView
    private lateinit var imageView: ImageView
    private lateinit var sliderView: SliderView
    private lateinit var sliderAdapter: SliderAdapter
    private var mSliderItems: MutableList<Listing> = ArrayList()

    private lateinit var addListingBtn: Button

    // instance for firebase storage and StorageReference
    lateinit var storage: FirebaseStorage
    lateinit var storageReference: StorageReference

    // file upload
    lateinit var files: ArrayList<String>
    lateinit var status: ArrayList<String>

//    private lateinit var : EditText
//    private lateinit var : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_listing)

        //setup database
        storage = Firebase.storage
        storageReference = storage.reference

        // file upload
        files = ArrayList()
        status = ArrayList()

        // Initialize the elements
        cancelBtn = findViewById(R.id.cancelBtn)

        productNameEtv = findViewById(R.id.productNameEtv)
        productNameErrorTv = findViewById(R.id.productNameErrorTv)

        productQuantityEtv = findViewById(R.id.productQuantityEtv)
        productQuantityErrorTv = findViewById(R.id.productQuantityErrorTv)

        productPriceEtv = findViewById(R.id.productPriceEtv)
        productPriceErrorTv = findViewById(R.id.productPriceErrorTv)

        productCategorySp = findViewById(R.id.productCategorySp)
        productCategoryErrorTv = findViewById(R.id.productCategoryErrorTv)

        productDescriptionEtv = findViewById(R.id.productDescriptionEtv)
        productDescriptionErrorTv = findViewById(R.id.productDescriptionErrorTv)

        productUploadBtn = findViewById(R.id.productUploadBtn)

        addListingBtn = findViewById(R.id.addListingBtn)

        // Set up cancel button
        cancelBtn.setOnClickListener{
            finish()
        }

        // Set up add listing
        addListingBtn.setOnClickListener{
            var valid: Boolean = validateInformation()

            Log.i(TAG, valid.toString())

            if(valid) {
                postListing()
            }
        }

        // Photo upload button
        productUploadBtn.setOnClickListener {
            selectImages();
        };

        loadCategories()

        // Image Tryouts
        imageSliderCv = findViewById(R.id.imageSliderCv)
        sliderView = findViewById<SliderView>(R.id.imageSlider)

        sliderAdapter = SliderAdapter(this)
        sliderView.setSliderAdapter(sliderAdapter)
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM) //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!

        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
        sliderView.setIndicatorSelectedColor(Color.WHITE)
        sliderView.setIndicatorUnselectedColor(Color.GRAY)
    }

    fun loadCategories() {
        val db = Firebase.firestore
        var categories: ArrayList<String> = ArrayList<String>()

        db.collection("categories")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.i(TAG, "${document.id} => ${document.data}")
                    categories.add(document.data.get("category").toString())
                }

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    categories
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                productCategorySp.adapter = adapter
                productCategorySp.onItemSelectedListener = this
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        parent.getItemAtPosition(pos)
        Log.i(TAG, parent.getItemAtPosition(pos).toString())
        productCategorySp.setSelection(pos, true)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
        Log.i(TAG, "NOT HERE")
    }

    private fun validateInformation(): Boolean {
        var valid: Int = 0

        if(TextUtils.isEmpty(productNameEtv.text.toString())) {
            productNameErrorTv.visibility = View.VISIBLE
            valid += 1
        }
        else {
            productNameErrorTv.visibility = View.GONE
        }

        if(TextUtils.isEmpty(productPriceEtv.text.toString())) {
            productPriceErrorTv.visibility = View.VISIBLE
            valid += 1
        }
        else {
            productPriceErrorTv.visibility = View.GONE
        }

        if(TextUtils.isEmpty(productQuantityEtv.text.toString())) {
            productQuantityErrorTv.visibility = View.VISIBLE
            valid += 1
        }
        else {
            productQuantityErrorTv.visibility = View.GONE
        }

        if(TextUtils.isEmpty(productCategorySp.selectedItem.toString())) {
            productCategoryErrorTv.visibility = View.VISIBLE
            valid += 1
        }
        else {
            productCategoryErrorTv.visibility = View.GONE
        }

        if(TextUtils.isEmpty(productDescriptionEtv.text.toString())) {
            productDescriptionErrorTv.visibility = View.VISIBLE
            valid += 1
        }
        else {
            productDescriptionErrorTv.visibility = View.GONE
        }

        return valid > 0
    }

    // Select Image method
    private fun selectImages() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT

        fileUploadLauncher.launch(Intent.createChooser(
            intent,
            "Please Select Multiple Files"
        ))
    }

    var fileUploadLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            if (result.data!!.clipData != null) {
                mSliderItems.clear()
                for (i in 0 until result.data!!.clipData!!.itemCount) {
                    val fileuri = result.data!!.clipData!!.getItemAt(i).uri
                    val filename: String = getfilenamefromuri(fileuri)
                    files.add(filename)
                    status.add("loading")

                    mSliderItems.add(Listing(filename, fileuri))

                    //TODO: Check if total items are less than 5
                    //TODO: Double check file sizes
                    //TODO: Add remove image function in the slider
                    // fattie: https://stackoverflow.com/questions/18573774/how-to-reduce-an-image-file-size-before-uploading-to-a-server
                    // https://stackoverflow.com/questions/40885860/how-to-save-bitmap-to-firebase/40886397#40886397
                    //TODO: Make a new function that will do the actual file storing

                    //CODE TO SAVE TO FIRESTORE
//                    adapter.notifyDataSetChanged()
//                    val uploader: StorageReference =
//                        CellTypeState.ref.child("/multiuploads").child(filename)
//                    val uploader: StorageReference = storageReference
//                        .child(
//                            "images/69696969/"
//                                    + UUID.randomUUID().toString()
//                        )
//                    uploader.putFile(fileuri)
//                        .addOnSuccessListener {
//                            status.removeAt(i)
//                            status.add(i, "done")
////                            adapter.notifyDataSetChanged()
//                        }
                }

                sliderAdapter.renewItems(mSliderItems)
                imageSliderCv.visibility = View.VISIBLE
            }
        }
    }

    fun getfilenamefromuri(filepath: Uri): String {
        var result: String? = null
        if (filepath.scheme == "content") {
            val cursor: Cursor? = contentResolver.query(filepath, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = filepath.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    private fun postListing() {

    }
}