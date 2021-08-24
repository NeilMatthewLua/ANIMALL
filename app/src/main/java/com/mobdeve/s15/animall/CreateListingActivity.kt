package com.mobdeve.s15.animall

import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import java.io.ByteArrayOutputStream
import java.io.IOException
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
    private lateinit var productLocationEtv: EditText
    private lateinit var productLocationErrorTv: TextView
    private lateinit var productUploadBtn: Button
    private lateinit var productUploadErrorTv: TextView

    // Image tryouts
    private lateinit var imageSliderCv: CardView
    private lateinit var imageView: ImageView
    private lateinit var sliderView: SliderView
    private lateinit var sliderAdapter: SliderAdapter
    private var mSliderItems: MutableList<Listing> = ArrayList()

    private lateinit var addListingBtn: Button
    private lateinit var listingProcessPb: ProgressBar
    private lateinit var dimBackgroundV: ConstraintLayout

    // instance for firebase storage and StorageReference
    lateinit var storage: FirebaseStorage
    lateinit var storageReference: StorageReference

    // file upload
    lateinit var files: ArrayList<String>
    lateinit var status: ArrayList<String>
    lateinit var byteArrayUpload: ArrayList<ByteArray>

    // categories document
    var categoriesHashMap: HashMap<String, String>
            = HashMap<String, String> ()
    lateinit var categoryId: String
    lateinit var photoURLs: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_listing)

        //setup database
        storage = Firebase.storage
        storageReference = storage.reference

        // file upload
        files = ArrayList()
        status = ArrayList()
        byteArrayUpload = ArrayList()
        photoURLs = ArrayList()

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

        productLocationEtv = findViewById(R.id.productLocationEtv)
        productLocationErrorTv = findViewById(R.id.productLocationErrorTv)

        productUploadBtn = findViewById(R.id.productUploadBtn)
        productUploadErrorTv = findViewById(R.id.productUploadErrorTv)

        addListingBtn = findViewById(R.id.addListingBtn)
        listingProcessPb = findViewById(R.id.listingProcessPb)
        dimBackgroundV = findViewById(R.id.dimBackgroundV)

        // Set up cancel button
        cancelBtn.setOnClickListener{
            finish()
        }

        // Set up add listing
        addListingBtn.setOnClickListener{
            var valid: Boolean = validateInformation()

            Log.i("Valid: ", valid.toString())

            if(valid) {
                dimBackgroundV.visibility = View.VISIBLE
                listingProcessPb.visibility = View.VISIBLE
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                uploadPhotos()
                Log.i(TAG, "Files Uploaded!")
            }
        }

        // Photo upload button
        productUploadBtn.setOnClickListener {
            selectImages()
        }

        loadCategories()

        // Image Tryouts
        imageSliderCv = findViewById(R.id.imageSliderCv)
        sliderView = findViewById<SliderView>(R.id.imageSlider)

        sliderAdapter = SliderAdapter(this)
        sliderView.setSliderAdapter(sliderAdapter)
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM) //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!

        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
        sliderView.indicatorSelectedColor = Color.WHITE
        sliderView.indicatorUnselectedColor = Color.GRAY

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
                    categoriesHashMap.put(document.data.get("category").toString(), document.id)
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
//        categoryId = categoriesHashMap.get(parent.getItemAtPosition(pos).toString())!!
        categoryId = parent.getItemAtPosition(pos).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
        Log.i(TAG, "NOTHING SELECTED")
    }

    private fun validateInformation(): Boolean {
        var invalid: Int = 0

        //XML Rule: Input must be <= 30 characters
        //Validation Rule: Not empty
        if(TextUtils.isEmpty(productNameEtv.text.toString())) {
            productNameErrorTv.visibility = View.VISIBLE
            invalid += 1
        }
        else {
            productNameErrorTv.visibility = View.GONE
        }

        //XML Rule: Input must be a positive numberDecimal
        //Validation Rule: Not empty and <= 100,000
        if(TextUtils.isEmpty(productPriceEtv.text.toString())) {
            productPriceErrorTv.text = "Product price is required."
            productPriceErrorTv.visibility = View.VISIBLE
            invalid += 1
        }
        else if (productPriceEtv.text.toString().toDouble() > 100000) {
            productPriceErrorTv.text = "Product price must not exceed 100,000."
            productPriceErrorTv.visibility = View.VISIBLE
            invalid += 1
        }
        else {
            productPriceErrorTv.visibility = View.GONE
        }

        //XML Rule: Input must be a positive number (Integer)
        //Validation Rule: Not empty and <= 1,000
        if(TextUtils.isEmpty(productQuantityEtv.text.toString())) {
            productQuantityErrorTv.text = "Product quantity is required."
            productQuantityErrorTv.visibility = View.VISIBLE
            invalid += 1
        }
        else if (productQuantityEtv.text.toString().toInt() > 1000) {
            productQuantityErrorTv.text = "Product quantity must not exceed 1,000."
            productQuantityErrorTv.visibility = View.VISIBLE
            invalid += 1
        }
        else {
            productQuantityErrorTv.visibility = View.GONE
        }
//        //Category will always have a preset value on dropdown so cannot be empty
//        if(TextUtils.isEmpty(productCategorySp.selectedItem.toString())) {
//            productCategoryErrorTv.visibility = View.VISIBLE
//            invalid += 1
//        }
//        else {
//            productCategoryErrorTv.visibility = View.GONE
//        }

        //XML Rule: Input must be <= 200 characters
        //Validation Rule: Not empty
        if(TextUtils.isEmpty(productDescriptionEtv.text.toString())) {
            productDescriptionErrorTv.visibility = View.VISIBLE
            invalid += 1
        }
        else {
            productDescriptionErrorTv.visibility = View.GONE
        }

        //Validation Rule: Not empty
        if(byteArrayUpload.size < 1) {
            productUploadErrorTv.visibility = View.VISIBLE
            invalid += 1
        }
        else {
            productUploadErrorTv.visibility = View.GONE
        }

        return invalid == 0
    }

    private fun selectImages() {
        val intent = Intent()
        intent.type = "image/*"
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT

        fileUploadLauncher.launch(Intent.createChooser(
            intent,
            "Please Select Multiple Files"
        ))
    }

    var fileUploadLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            mSliderItems.clear()
            status.clear()
            var validUpload = true
            byteArrayUpload = ArrayList<ByteArray>()

            if (result.data!!.clipData != null) {
                Log.i("Total Items:", "${result.data!!.clipData!!.itemCount}")

                if (result.data!!.clipData!!.itemCount < 6) {
                    for (i in 0 until result.data!!.clipData!!.itemCount) {
                        val fileuri = result.data!!.clipData!!.getItemAt(i).uri
                        val filename: String = getfilenamefromuri(fileuri)
                        files.add(filename)
                        status.add("loading")
                        val bitImage = checkUpload(filename, fileuri)

                        if(bitImage != null) {
                            mSliderItems.add(Listing(filename, fileuri, bitImage))
                        }
                    }
                }
                else {
                    productUploadErrorTv.text = "Only a maximum of 5 images are allowed"
                    productUploadErrorTv.visibility = View.VISIBLE
                    validUpload = false
                }
            }
            // One Image
            else {
                val single_fileuri = result.data!!.data
                val filename: String = getfilenamefromuri(single_fileuri!!)
                files.add(filename)
                status.add("loading")
                val bitImage = checkUpload(filename, single_fileuri)

                if(bitImage != null) {
                    mSliderItems.add(Listing(filename, single_fileuri, bitImage))
                }
            }

            if(validUpload) {
                productUploadErrorTv.visibility = View.GONE
                sliderAdapter.renewItems(mSliderItems)
                imageSliderCv.visibility = View.VISIBLE
            }
            else {
                imageSliderCv.visibility = View.GONE
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

    private fun checkUpload(filename:String, fileuri: Uri): Bitmap? {
        //Calculate size of images
        val fullBitmap =
            MediaStore.Images.Media.getBitmap(this.contentResolver, fileuri)
        val stream = ByteArrayOutputStream()
        fullBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val imageInByte: ByteArray = stream.toByteArray()
        val lengthbmp = imageInByte.size.toLong().toDouble() / 1024

        Log.i("Image size: ", "${filename}: ${lengthbmp}kb")

        // if file uploaded is more than 1Mb
        if (lengthbmp > 1024) {
            // Downsizing image
            // +500 fattie: https://stackoverflow.com/questions/18573774/how-to-reduce-an-image-file-size-before-uploading-to-a-server
            // https://stackoverflow.com/questions/40885860/how-to-save-bitmap-to-firebase/40886397#40886397
            try {
                Log.i("IMAGE fullWidth: ", "${fullBitmap.width}")
                Log.i("IMAGE fullHeight: ", "${fullBitmap.height}")
                val scaleDivider = fullBitmap.width.toDouble() / lengthbmp
                val scaleWidth = fullBitmap.width.toDouble() / scaleDivider
                val scaleHeight = fullBitmap.height.toDouble() / scaleDivider
                Log.i("IMAGE scaleWidth: ", "${scaleWidth}")
                Log.i("IMAGE scaleHeight: ", "${scaleHeight}")

                val (downsizedImageBytes, downsizedBitmap) =
                    getDownsizedImageBytes(
                        fullBitmap,
                        scaleWidth.toInt(),
                        scaleHeight.toInt()
                    )

                byteArrayUpload.add(downsizedImageBytes!!)

                return downsizedBitmap
            } catch (ioEx: IOException) {
                ioEx.printStackTrace()
                return null
            }
        }

        byteArrayUpload.add(imageInByte)
        return fullBitmap
    }

    // Downsize images
    @Throws(IOException::class)
    fun getDownsizedImageBytes(
        fullBitmap: Bitmap?,
        scaleWidth: Int,
        scaleHeight: Int
    ): Pair<ByteArray, Bitmap> {
        val scaledBitmap =
            Bitmap.createScaledBitmap(fullBitmap!!, scaleWidth, scaleHeight, true)

        val baos = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return Pair(baos.toByteArray(), scaledBitmap)
    }

    private fun uploadPhotos() {
        val listingID = UUID.randomUUID().toString()

        //Upload ByteArray
        byteArrayUpload.forEach {
            val uploader: StorageReference = storageReference
                .child(
                    "images/${listingID}/"
                            + UUID.randomUUID().toString()
                )
            uploader.putBytes(it).addOnSuccessListener {
                //            status.add(i, "done")
                uploader.downloadUrl.addOnSuccessListener {
                    Log.i("URL", it.toString())
                    photoURLs.add(it.toString())
                    status.removeAt(0)
                    if (status.size == 0) {
                        postListing(listingID)
                    }
                }
            }
        }
    }

    fun postListing(listingID: String) {
        val db = Firebase.firestore

        photoURLs.forEach{
            Log.i(TAG, it.toString())
        }
        Log.i("POSTING", "POSTING")
        val listing = hashMapOf(
            "category" to categoryId,
            "description" to productDescriptionEtv.text.toString(),
            "name" to productNameEtv.text.toString(),
            "preferredLocation" to productLocationEtv.text.toString(),
            "seller" to "7igRri2b0HUHIxCKNWGGIVbuvbu2",
            "stock" to productQuantityEtv.text.toString().toInt(),
            "unitPrice" to productPriceEtv.text.toString().toDouble(),
            "photos" to photoURLs
        )

        db.collection("listings").document(listingID)
            .set(listing)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Listing added")
                listingProcessPb.visibility = View.GONE
                dimBackgroundV.visibility = View.GONE
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Toast.makeText(this, "Listing Added", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                listingProcessPb.visibility = View.GONE
                dimBackgroundV.visibility = View.GONE
            }
//      //Upload File (Won't be used since if we're downsizing images if needed so we use the bytes instead)
//        uploader.putFile(fileuri)
//            .addOnSuccessListener {
//                status.removeAt(i)
//                status.add(i, "done")
//                adapter.notifyDataSetChanged()
//        }
    }
}