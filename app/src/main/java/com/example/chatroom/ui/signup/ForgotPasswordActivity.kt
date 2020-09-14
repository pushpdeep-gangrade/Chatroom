package com.example.chatroom.ui.signup

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatroom.R
import com.google.firebase.auth.FirebaseAuth


class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val email = findViewById<EditText>(R.id.forgotPassword_email)
        val sendEmail = findViewById<Button>(R.id.forgotPassword_sendEmailButton)
        val cancel = findViewById<Button>(R.id.forgotPassword_cancelButton)

        auth = FirebaseAuth.getInstance()

        sendEmail.setOnClickListener {
            val emailText = email.text.toString()

            auth.sendPasswordResetEmail(emailText)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Email sent", "Email sent successfully")
                        Toast.makeText(this, "Email was sent to $emailText", Toast.LENGTH_LONG).show()
                    }
                    else{
                        Log.d("Email sent", "Email could not be sent")
                        Toast.makeText(this, "Email could not be sent", Toast.LENGTH_LONG).show()
                    }
                }
        }

        cancel.setOnClickListener {
            finish()
        }
    }
}