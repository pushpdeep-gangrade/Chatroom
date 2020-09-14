package com.example.chatroom.ui.signup

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.example.chatroom.R
import com.example.chatroom.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var db: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var storage: FirebaseStorage = FirebaseStorage.getInstance()
    private lateinit var dbRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    private var genderText: String = ""
    private var imageUrl: String = ""
    private lateinit var avatar: ImageView
    private lateinit var loading: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        val firstName = findViewById<EditText>(R.id.signup_firstName)
        val lastName = findViewById<EditText>(R.id.signup_lastName)
        val email = findViewById<EditText>(R.id.signup_email)
        val city = findViewById<EditText>(R.id.signup_city)
        val password = findViewById<EditText>(R.id.signup_password)
        val reEnterPassword = findViewById<EditText>(R.id.signup_reenterPassword)
        avatar = findViewById<ImageView>(R.id.signup_logo)
        val genderRadioGroup = findViewById<RadioGroup>(R.id.signup_genderRadioGroup)
        val signUp = findViewById<Button>(R.id.signup_completeSignUpButton)
        loading = findViewById<ProgressBar>(R.id.signup_loading)
        val cancel = findViewById<Button>(R.id.signup_cancelSignUpButton)


        loading.visibility = View.INVISIBLE

        auth = FirebaseAuth.getInstance()
        dbRef = db.reference
        storageRef = storage.reference

        avatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, Companion.REQUEST_CODE)
        }

        genderRadioGroup.setOnCheckedChangeListener( RadioGroup.OnCheckedChangeListener{ group, checkedId ->
            val radio: RadioButton = findViewById(checkedId)
            genderText = radio.text.toString()
        })

        cancel.setOnClickListener {
            finish()
        }

        signUp.setOnClickListener {
            val emailText = email.text.toString()
            val passwordText = password.text.toString()
            val reEnterPasswordText = reEnterPassword.text.toString()
            val firstNameText = firstName.text.toString()
            val lastNameText = lastName.text.toString()
            val cityText = city.text.toString()

            var allValid = true

            if(firstNameText.equals("")){
                firstName.error = "Please enter your first name"
                allValid = false
            }

            if(lastNameText.equals("")){
                lastName.error = "Please enter your last name"
                allValid = false
            }

            if(emailText.equals("")){
                email.error = "Please enter your email"
                allValid = false
            }

            if(cityText.equals("")){
                city.error = "Please enter your city"
                allValid = false
            }

            if(!passwordText.equals(reEnterPasswordText) || passwordText.equals("")){
                if(!passwordText.equals(reEnterPasswordText)){
                    password.error = "Passwords do not match"
                }
                else{
                    password.error = "Please enter a password"
                }
                allValid = false
            }

            if(imageUrl.equals("")){
                Toast.makeText(this, "Please select a profile picture", Toast.LENGTH_LONG).show()
                allValid = false
            }

            if(genderText.equals("")){
                Toast.makeText(this, "Please select a gender", Toast.LENGTH_LONG).show()
                allValid = false
            }

            if(allValid){
                loading.visibility = View.VISIBLE

                auth.createUserWithEmailAndPassword(emailText, passwordText)
                    .addOnCompleteListener(this) { task ->
                        if(task.isSuccessful) {
                            val user =  User;

                            user.email = emailText
                            user.firstName = firstNameText
                            user.lastName = lastNameText
                            user.city = cityText
                            user.userId = auth.currentUser?.uid.toString()
                            user.gender = genderText

                            storeUserData(user)

                            Log.d("Success", "Sign Up Successful")

                        }
                        else {
                            loading.visibility = View.INVISIBLE
                            Log.d("Failure", "Sign Up Failure")

                            Toast.makeText(this, "Sign Up Failed", Toast.LENGTH_LONG).show()

                        }
                    }
            }

        }

    }

    private fun storeUserData(user: User?){
        if (user != null) {
            avatar.isDrawingCacheEnabled = true
            avatar.buildDrawingCache()
            val bitmap = (avatar.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val profilePic = storageRef.child("images/" + user.userId + "/profilePic.jpg")

            val uploadTask = profilePic.putBytes(data)
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
                Log.d("Image load success", "Image upload failed")
                loading.visibility = View.INVISIBLE
            }.addOnSuccessListener { taskSnapshot ->
                Log.d("Image load success", "Image uploaded successfully")
            }

            val urlTask = uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                profilePic.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    Log.d("Download Uri", downloadUri.toString())
                    user.imageUrl = downloadUri.toString()
                    dbRef.child("users").child(user.userId).setValue(user)

                    loading.visibility = View.INVISIBLE
                    Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    loading.visibility = View.INVISIBLE
                    Log.d("Download Uri", "Unable to get download uri")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Companion.REQUEST_CODE){
            avatar.setImageURI(data?.data) // handle chosen image
            Log.d("Image url", data?.data.toString())

            imageUrl = data?.data.toString()
        }
    }

    companion object {
        private const val REQUEST_CODE = 100
    }
}


