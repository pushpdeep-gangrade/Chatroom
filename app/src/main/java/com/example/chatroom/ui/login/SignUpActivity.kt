package com.example.chatroom.ui.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.chatroom.R
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val firstName = findViewById<EditText>(R.id.signup_firstName)
        val lastName = findViewById<EditText>(R.id.signup_lastName)
        val email = findViewById<EditText>(R.id.signup_email)
        val city = findViewById<EditText>(R.id.signup_city)
        val password = findViewById<EditText>(R.id.signup_password)
        val reEnterPassword = findViewById<EditText>(R.id.signup_reenterPassword)
        val avatar = findViewById<ImageView>(R.id.signup_logo)
        val genderRadioGroup = findViewById<RadioGroup>(R.id.signup_genderRadioGroup)
        val maleRadioButton = findViewById<RadioButton>(R.id.signup_maleRadioButton)
        val femaleRadioButton = findViewById<RadioButton>(R.id.signup_femaleRadioButton)
        val signUp = findViewById<Button>(R.id.signup_completeSignUpButton)

        auth = FirebaseAuth.getInstance()

        signUp.setOnClickListener {
            val emailText = email.text.toString()
            val passwordText = password.text.toString()

            auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this) { task ->
                    if(task.isSuccessful) {
                        Log.d("Success", "Sign Up Successful")

                        Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_LONG).show()

                    }
                    else {
                        Log.d("Failure", "Sign Up Failure")

                        Toast.makeText(this, "Sign Up Failed", Toast.LENGTH_LONG).show()

                    }
                }
        }

    }
}