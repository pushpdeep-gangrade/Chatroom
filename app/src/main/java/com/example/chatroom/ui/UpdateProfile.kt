package com.example.chatroom.ui


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.chatroom.R
import com.example.chatroom.databinding.FragmentUpdateProfileBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.*
import java.io.ByteArrayOutputStream

data class User(val firstname: String, val lastname: String, val gender : String, val city : String, val profileImageUrl : String)

class UpdateProfile : Fragment() {
    var userGender : String? = null
    var profileimageUrl : String? = null
    val REQUEST_IMAGE_CAPTURE = 1
    private var _binding : FragmentUpdateProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentUpdateProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profileImage.setOnClickListener{
            dispatchTakePictureIntent()
        }

        binding.tvSave.setOnClickListener{
            uploadUserProfile()
        }
        binding.tvCancel.setOnClickListener{

        }

        fun onRadioButtonClicked(view: View) {
            if (view is RadioButton) {
                val checked = view.isChecked
                when (view.getId()) {
                    R.id.rb_male ->
                        if (checked) {
                      userGender = "Male"
                        }
                    R.id.rb_female ->
                        if (checked) {
                        userGender =  "Female"
                        }
                }
            }
        }
    }
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            context?.packageManager?.let {
                takePictureIntent.resolveActivity(it)?.also {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            lateinit var storage: FirebaseStorage
            storage = Firebase.storage
            val storageRef = storage.reference
            val userProfileImage = storageRef.child("UserImages/1")
            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            var uploadTask = userProfileImage.putBytes(data)
            uploadTask.addOnFailureListener {
                Toast.makeText(context, "Upload failed", Toast.LENGTH_LONG).show()
            }.addOnSuccessListener { taskSnapshot ->
                Toast.makeText(context, "Upload failed", Toast.LENGTH_LONG).show()
                getDownloadURL(userProfileImage, uploadTask)
                binding.profileImage.setImageBitmap(imageBitmap)
            }
        }
    }
    fun getDownloadURL(ref : StorageReference, uploadTask : UploadTask){
    val urlTask = uploadTask.continueWithTask { task ->
        if (!task.isSuccessful) {
            task.exception?.let {
                throw it
            }
        }
        ref.downloadUrl
    }.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val downloadUri = task.result
            profileimageUrl = downloadUri.toString()
            Log.d("demo", downloadUri.toString())
           // Toast.makeText(context, downloadUri, Toast.LENGTH_LONG).show()
        } else {

        }
    }
    }


    fun uploadUserProfile(){
        val user = User(binding.tvFirstnameUpdate.text.toString(), binding.tvLastnameUpdate.text.toString(),
            userGender.toString(),
            city = binding.etCity.text.toString(), profileImageUrl = profileimageUrl.toString()
        )
        val db = Firebase.firestore

        db.collection("Users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d("demo", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("demo", "Error adding document", e)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

