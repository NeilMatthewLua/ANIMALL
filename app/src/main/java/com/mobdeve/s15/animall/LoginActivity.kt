package com.mobdeve.s15.animall

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var signInBtn: Button
    companion object {
//        const val RC_SIGN_IN = 9001
        const val TAG ="LGNACT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = Firebase.auth
        db = Firebase.firestore

        signInBtn = findViewById(R.id.signInBtn)
        signInBtn.setOnClickListener {
            signIn()
        }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.location_dialog_title)
            val str = "Please sign in with a valid DLSU account."
            builder.setMessage(str)
//            builder.setIcon(android.R.drawable.ic_dialog_alert)

            builder.setPositiveButton("I understand"){ _, _ ->}

            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser

        // if logged in go to landing activity
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        Log.w(TAG, currentUser.toString())
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    if (user != null) {
                        redirectUser(user)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private val getLocation = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val value = intent?.getStringExtra("PREF_LOC")

            val userRef = auth.currentUser?.let { db.collection("users").document(it.uid) }
            userRef?.get()?.addOnCompleteListener { documentTask ->
                if (documentTask.isSuccessful) {
                    if (documentTask.result.data != null) {
                        userRef.update("preferredLocation", value)
                    }
                }
            }
        }
    }

    /*
        Checks if there is an existing user.
        If the user does not exist in firestore,
        add them to the db and get their preferred location,
        else redirect them to the landing page.
     */
    private fun redirectUser(user:FirebaseUser) {
        val userRef = db.collection("users").document(user.uid)
        userRef.get().addOnCompleteListener { documentTask ->
            if (documentTask.isSuccessful) {
                if (documentTask.result.data != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${documentTask.result.data}")
                    if (documentTask.result.get("preferredLocation") == "") {
                        Log.d(TAG, "Missing pref location")
                        getLocation.launch(Intent(this, LocationActivity::class.java))
                    }
                    // return to landing
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Log.d(TAG, "No such document")
                    val userHash = hashMapOf(
                        "email" to user.email,
                        "name" to user.displayName,
                        "preferredLocation" to ""
                    )
                    userRef.set(userHash)
                    getLocation.launch(Intent(this, LocationActivity::class.java))
                }
            }
        }
    }
}