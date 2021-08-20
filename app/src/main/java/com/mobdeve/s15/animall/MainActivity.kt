package com.mobdeve.s15.animall

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    // views for button
//    private lateinit var btnSelect: Button
    private lateinit var btnUpload: Button

    // view for image view
    private lateinit var imageView: ImageView

    // Uri indicates, where the image will be picked from
    private lateinit var filePath: Uri

    // request code
    private var PICK_IMAGE_REQUEST = 22

    // instance for firebase storage and StorageReference
    lateinit var storage: FirebaseStorage
    lateinit var storageReference: StorageReference


    lateinit var files: ArrayList<String>
    lateinit var status: ArrayList<String>

    lateinit var recview: RecyclerView
    lateinit var btn_upload: ImageView

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
    }

    // Select Image method
    private fun SelectImages() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Please Select Multiple Files"
            ), 101
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (requestCode == 101 && resultCode == RESULT_OK) {
            if (data!!.clipData != null) {
                for (i in 0 until data.clipData!!.itemCount) {
                    val fileuri = data.clipData!!.getItemAt(i).uri
                    val filename: String = getfilenamefromuri(fileuri)
                    files.add(filename)
                    status.add("loading")
//                    adapter.notifyDataSetChanged()
//                    val uploader: StorageReference =
//                        CellTypeState.ref.child("/multiuploads").child(filename)
                    val uploader: StorageReference = storageReference
                        .child(
                            "images/69696969/"
                                    + UUID.randomUUID().toString()
                        )
                    uploader.putFile(fileuri)
                        .addOnSuccessListener {
                            status.removeAt(i)
                            status.add(i, "done")
//                            adapter.notifyDataSetChanged()
                        }
                }
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
//        {
//        val actionBar: ActionBar?
//        actionBar = supportActionBar
//        val colorDrawable = ColorDrawable(
//            Color.parseColor("#0F9D58")
//        )
//        actionBar!!.setBackgroundDrawable(colorDrawable)
//
//        // initialise views
//
//        // initialise views
//        btnSelect = findViewById(R.id.btnChoose)
//        btnUpload = findViewById(R.id.btnUpload)
//        imageView = findViewById(R.id.imgView)
//
//        // get the Firebase  storage reference
//        storage = Firebase.storage
//        storageReference = storage.reference
//
//        // on pressing btnSelect SelectImage() is called
//        btnSelect.setOnClickListener{
//            SelectImage();
//        };
//
//        // on pressing btnUpload uploadImage() is called
//        btnUpload.setOnClickListener{
//            uploadImage();
//        };
    }

//    // Select Image method
//    private fun SelectImage() {
//        // Defining Implicit Intent to mobile gallery
//        val intent = Intent()
//        intent.type = "image/*"
//        intent.action = Intent.ACTION_GET_CONTENT
//        startActivityForResult(
//            Intent.createChooser(
//                intent,
//                "Select Image from here..."
//            ),
//            PICK_IMAGE_REQUEST
//        )
//    }

//    // Override onActivityResult method
//    override fun onActivityResult(
//        requestCode: Int,
//        resultCode: Int,
//        data: Intent?
//    ) {
//        super.onActivityResult(
//            requestCode,
//            resultCode,
//            data
//        )
//
//        // checking request code and result code
//        // if request code is PICK_IMAGE_REQUEST and
//        // resultCode is RESULT_OK
//        // then set image in the image view
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
//
//            // Get the Uri of data
//            filePath = data.data!!
//            try {
//
//                // Setting image on image view using Bitmap
//                val bitmap = MediaStore.Images.Media
//                    .getBitmap(
//                        contentResolver,
//                        filePath
//                    )
//                imageView.setImageBitmap(bitmap)
//            } catch (e: IOException) {
//                // Log the exception
//                e.printStackTrace()
//            }
//        }
//    }
//
//    // UploadImage method
//    private fun uploadImage() {
//        if (filePath != null) {
//
//            // Code for showing progressDialog while uploading
//            val progressDialog = ProgressDialog(this)
//            progressDialog.setTitle("Uploading...")
//            progressDialog.show()
//
//            // Defining the child of storageReference
//            val ref: StorageReference = storageReference
//                .child(
//                    "images/69696969/"
//                            + UUID.randomUUID().toString()
//                )
//
//            // adding listeners on upload
//            // or failure of image
//            ref.putFile(filePath)
//                .addOnSuccessListener { // Image uploaded successfully
//                    // Dismiss dialog
//                    progressDialog.dismiss()
//                    Toast
//                        .makeText(
//                            this@MainActivity,
//                            "Image Uploaded!!",
//                            Toast.LENGTH_SHORT
//                        )
//                        .show()
//                }
//                .addOnFailureListener { e -> // Error, Image not uploaded
//                    progressDialog.dismiss()
//                    Toast
//                        .makeText(
//                            this@MainActivity,
//                            "Failed " + e.message,
//                            Toast.LENGTH_SHORT
//                        )
//                        .show()
//                }
//                .addOnProgressListener { taskSnapshot ->
//
//                    // Progress Listener for loading
//                    // percentage on the dialog box
//                    val progress = (100.0
//                            * taskSnapshot.bytesTransferred
//                            / taskSnapshot.totalByteCount)
//                    progressDialog.setMessage(
//                        "Uploaded "
//                                + progress.toInt() + "%"
//                    )
//                }
//        }
//    }
