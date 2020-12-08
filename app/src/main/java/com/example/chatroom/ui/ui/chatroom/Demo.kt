//package com.example.texttospeech
//
//import android.Manifest
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.speech.RecognitionListener
//import android.speech.RecognizerIntent
//import android.speech.SpeechRecognizer
//import android.view.MotionEvent
//import android.view.View.OnTouchListener
//import android.widget.EditText
//import android.widget.ImageView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import java.util.*
//
//class MainActivity : AppCompatActivity() {
//
//    private var editText: EditText? = null
//    private var micButton: ImageView? = null
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        micButton.setOnTouchListener(OnTouchListener { view, motionEvent ->
//            if (motionEvent.action == MotionEvent.ACTION_UP) {
//                speechRecognizer.stopListening()
//            }
//            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
//                micButton.setImageResource(R.drawable.ic_mic_black_24dp)
//                speechRecognizer.startListening(speechRecognizerIntent)
//            }
//            false
//        })
//    }
//
//
//}