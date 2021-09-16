package com.mobdeve.s15.animall

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import kotlinx.android.synthetic.main.fragment_add_listing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class AddListingFragment : Fragment(), AdapterView.OnItemSelectedListener {
    val TAG = "ADD LISTING ACTIVTY"

    // Image tryouts
    private lateinit var sliderView: SliderView
    private lateinit var sliderAdapter: SliderAdapter
    private var mSliderItems: MutableList<Listing> = ArrayList()

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

    lateinit var currentUser: UserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setup database
        storage = Firebase.storage
        storageReference = storage.reference

        // file upload
        files = ArrayList()
        status = ArrayList()
        byteArrayUpload = ArrayList()
        photoURLs = ArrayList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            val loggedUser = Firebase.auth.currentUser
            val dataInit = async(Dispatchers.IO) {
                currentUser = DatabaseManager.getUserViaEmail(loggedUser?.email!!)!!
            }
            dataInit.await()

            // Set up cancel button
            cancelBtn.setOnClickListener{
                (requireActivity().findViewById<View>(R.id.bottom_navigatin_view) as BottomNavigationView).selectedItemId = R.id.landingFragment
            }

            // Set up add listing
            addListingBtn.setOnClickListener{
                var valid: Boolean = validateInformation()

                Log.i("Valid: ", valid.toString())

                if(valid) {
                    listingDimBackgroundV.visibility = View.VISIBLE
                    listingProcessPb.visibility = View.VISIBLE
                    getActivity()?.getWindow()?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    uploadPhotos()
                    Log.i(TAG, "Files Uploaded!")
                }
            }


        // Photo upload button
        productUploadBtn.setOnClickListener {
//            selectImages()
            requestPermissions()
        }

            loadCategories()
            sliderView = activity?.findViewById(R.id.imageSlider)!!

            sliderAdapter = SliderAdapter(requireContext())
            sliderView.setSliderAdapter(sliderAdapter)
            sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM) //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!

            sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
            sliderView.indicatorSelectedColor = Color.WHITE
            sliderView.indicatorUnselectedColor = Color.GRAY
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_listing, container, false)
    }

    fun loadCategories() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.listing_category_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            productCategorySp.adapter = adapter
            productCategorySp.onItemSelectedListener = this
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        parent.getItemAtPosition(pos)
        Log.i(TAG, parent.getItemAtPosition(pos).toString())
        productCategorySp.setSelection(pos, true)
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

    // Request permissions if not granted before
    private fun requestPermissions() {
        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

//    private fun checkPermissions(): Boolean {
//        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
//        ) {
//            return true
//        }
//        return false
//    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            selectImages()
        }
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
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
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
            val cursor: Cursor? = activity?.contentResolver?.query(filepath, null, null, null, null)
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
            MediaStore.Images.Media.getBitmap(activity?.contentResolver, fileuri)
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
        val db = DatabaseManager.getInstance()

        photoURLs.forEach{
            Log.i(TAG, it.toString())
        }
        Log.i("POSTING", "POSTING")

        val listing = hashMapOf(
            MyFirestoreReferences.CATEGORY_FIELD to categoryId,
            MyFirestoreReferences.LISTING_IS_OPEN to true,
            MyFirestoreReferences.DESCRIPTION_FIELD to productDescriptionEtv.text.toString(),
            MyFirestoreReferences.PRODUCT_NAME_FIELD to productNameEtv.text.toString(),
            MyFirestoreReferences.LOCATION_FIELD to productLocationEtv.text.toString(),
            MyFirestoreReferences.SELLER_FIELD to currentUser.email,
            MyFirestoreReferences.STOCK_FIELD to productQuantityEtv.text.toString().toInt(),
            MyFirestoreReferences.PRICE_FIELD to productPriceEtv.text.toString().toDouble(),
            MyFirestoreReferences.PHOTOS_FIELD to photoURLs
        )

        db.collection("listings").document(listingID)
            .set(listing)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Listing added")
                listingProcessPb.visibility = View.GONE
                listingDimBackgroundV.visibility = View.GONE
                getActivity()?.getWindow()?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Toast.makeText(context, "Listing Added", Toast.LENGTH_SHORT).show()
                // Redirect back to home
                (requireActivity().findViewById<View>(R.id.bottom_navigatin_view) as BottomNavigationView).selectedItemId = R.id.landingFragment
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                listingProcessPb.visibility = View.GONE
                listingDimBackgroundV.visibility = View.GONE
            }
    }
}