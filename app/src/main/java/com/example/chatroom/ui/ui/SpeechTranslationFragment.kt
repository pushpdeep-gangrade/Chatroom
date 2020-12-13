package com.example.chatroom.ui.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.chatroom.R
import com.example.chatroom.ui.ui.chatroom.Chatroom
import com.example.chatroom.ui.ui.chatroom.Language
import com.google.gson.Gson
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.microsoft.cognitiveservices.speech.ResultReason
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechSynthesisCancellationDetails
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import com.microsoft.cognitiveservices.speech.translation.SpeechTranslationConfig
import com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.fragment_speech_translation.*
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text
import java.util.*
import kotlin.collections.ArrayList


class SpeechTranslationFragment : Fragment() {

    var arrStringName: ArrayList<String> = ArrayList()
    var arrLanguageObjects: ArrayList<Language> = ArrayList()
    private var languageSelected: String = ""
    private var audioPermissionGranted = false
    private var fromSpinnerLanguageSelected: String = ""
    private var toSpinnerLanguageSelected: String = ""
    private var fromLanguageCode: String = ""
    private var toLanguageCode: String = ""
    private var originalTextBox: TextView? = null
    private var translatedTextBox: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_speech_translation, container, false)

//        imageView = view.findViewById(R.id.imageTranslation_imageView)
        val translateSpeech: ImageView = view.findViewById(R.id.speechTranslationImageView)
        originalTextBox = view.findViewById(R.id.speechTranslation_originalTextBox)
        translatedTextBox = view.findViewById(R.id.speechTranslation_translatedTextBox)
        val playAudio: Button = view.findViewById(R.id.speechTranslation_playAudioButton)

//

        translateSpeech.setOnClickListener {
            getAudioPermission()
            setTranslateMessageDialog(requireContext())
        }

        playAudio.setOnClickListener {
            textToSpeechOnly(translatedTextBox)
        }


        return view
    }

    //----------
    private fun setTranslateMessageDialog(context: Context) {
        val arrStringName: ArrayList<String> = ArrayList()
        val arrLanguageObjects: ArrayList<Language> = ArrayList()

        val builder = AlertDialog.Builder(context);
        val view: View = View.inflate(context, R.layout.translate_speech_dialog, null)

        val cancel: TextView = view.findViewById<TextView>(R.id.translateSpeechDialog_cancelButton)
        val submit: TextView = view.findViewById<TextView>(R.id.translateSpeechDialog_submitButton)
        val fromLanguageSpinner: Spinner = view.findViewById<Spinner>(R.id.translateSpeechDialog_fromLanguageSpinner)
        val toLanguageSpinner: Spinner = view.findViewById<Spinner>(R.id.translateSpeechDialog_toLanguageSpinner)
        val progressBar: ProgressBar = view.findViewById<ProgressBar>(R.id.translateSpeechDialog_progressBar)
        val speakNowPrompt: TextView = view.findViewById<TextView>(R.id.speak_now_prompt)
        val fromPrompt: TextView = view.findViewById<TextView>(R.id.from_prompt)
        val toPrompt: TextView = view.findViewById<TextView>(R.id.to_prompt)

        progressBar.visibility = View.VISIBLE
        speakNowPrompt.visibility = View.INVISIBLE
        submit.isEnabled = false

        builder.setView(view);
        val dialog: AlertDialog = builder.create()

        cancel.setOnClickListener {
            Log.d("Cancel", "Hit cancel")

            dialog.cancel()

        }

        submit.setOnClickListener {
            //Logic for translating text goes here
            Log.d("SUBMIT TEST","From Spinner Language: " + fromSpinnerLanguageSelected
                        + "\nTo Spinner Language: " + toSpinnerLanguageSelected)

            //This is how you will get the proper value for the selected languages
            Log.d("From Language", fromLanguageCode)
            Log.d("To Language", toLanguageCode)

            //textToTextTranslation(from, to, mTvMsg, context, dialog)
            //Recognizer and translator
            if(toSpinnerLanguageSelected == "Unknown"){
                Toast.makeText(
                    context, "Cannot choose Unknown for To language",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else{
                if (audioPermissionGranted) {
                    Log.d("Speech to Text", "Speak into your microphone.")
                    // Get the Intent action
                    val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                    // Language model defines the purpose, there are special models for other use cases, like search.
                    sttIntent.putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    // Adding an extra language, you can use any language from the Locale class.
                    sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    // Text that shows up on the Speech input prompt.
                    sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")
                    try {
                        // Start the intent for a result, and pass in our request code.
                        startActivityForResult(sttIntent, SPEECH_TO_TEXT_REQUEST)
                        dialog.cancel()
                    } catch (e: ActivityNotFoundException) {
                        // Handling error when the service is not available.
                        e.printStackTrace()
                        Toast.makeText(context, "Your device does not support STT.", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }

        }

        dialog.show()


        val url =
            "https://api.cognitive.microsofttranslator.com/languages?api-version=3.0"

        val client: AsyncHttpClient = AsyncHttpClient()

        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<Header>?,
                response: JSONObject?
            ) {
                if (response != null) {

                    Log.d("Response", response.toString())
                    val translationObj: JSONObject =
                        JSONObject(response.toString()).getJSONObject("translation")
                    val keys: Iterator<String> = translationObj.keys()

                    val unknownLanguage: Language = Language("", "Unknown", "", "")
                    arrStringName.add(unknownLanguage.name)
                    arrLanguageObjects.add(unknownLanguage)

                    while (keys.hasNext()) {
                        val key = keys.next()

                        val value: JSONObject = translationObj.getJSONObject(key)

                        val name: String = value.getString("name")
                        val nativeName: String = value.getString("nativeName")
                        val dir: String = value.getString("dir")

                        val language: Language = Language(key, name, nativeName, dir)

                        arrStringName.add(name)
                        arrLanguageObjects.add(language)

                        // Do something...

                        Log.d("Objects", key + ": " + value)
                    }

                    val adapter = ArrayAdapter(
                        context,
                        android.R.layout.simple_spinner_item, arrStringName
                    )
                    fromLanguageSpinner.adapter = adapter
                    toLanguageSpinner.adapter = adapter

                    val prefs: SharedPreferences =
                        context.getSharedPreferences("info", Context.MODE_PRIVATE)

                    if(toSpinnerLanguageSelected == "" && prefs.getString("language", null) == null){
                        fromSpinnerLanguageSelected = arrStringName.get(0)
                        fromLanguageCode = arrLanguageObjects.get(0).key
                        toSpinnerLanguageSelected = arrStringName.get(0)
                        toLanguageCode = arrLanguageObjects.get(0).key

                        if (adapter != null) {
                            fromLanguageSpinner.setSelection(adapter.getPosition(fromSpinnerLanguageSelected))
                        }
                    }
                    else if(toSpinnerLanguageSelected == "" && prefs.getString("language", null) != null){
                        val gsonObject = Gson()

                        val language: Language = gsonObject.fromJson(prefs.getString("language", null), Language::class.java)

                        fromSpinnerLanguageSelected = language.name

                        if (adapter != null) {
                            fromLanguageSpinner.setSelection(adapter.getPosition(fromSpinnerLanguageSelected))
                        }

                    }
                    else{
                        fromSpinnerLanguageSelected = toSpinnerLanguageSelected.plus("")
                        fromLanguageCode = toLanguageCode.plus("")

                        if (adapter != null) {
                            fromLanguageSpinner.setSelection(adapter.getPosition(toSpinnerLanguageSelected))
                        }
                    }

                    toSpinnerLanguageSelected = arrStringName.get(0)
                    toLanguageCode = arrLanguageObjects.get(0).key

                    fromLanguageSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(parent: AdapterView<*>?) {

                            }

                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                fromSpinnerLanguageSelected = arrStringName.get(position)
                                fromLanguageCode = arrLanguageObjects.get(position).key
                            }

                        }

                    toLanguageSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(parent: AdapterView<*>?) {
                            }

                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                toSpinnerLanguageSelected = arrStringName.get(position)
                                toLanguageCode = arrLanguageObjects.get(position).key
                            }

                        }

                    progressBar.visibility = View.INVISIBLE
                    submit.isEnabled = true


                } else {

                    Toast.makeText(
                        context, "Failed to get languages",
                        Toast.LENGTH_SHORT
                    ).show()

                    progressBar.visibility = View.INVISIBLE

                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<Header>?,
                e: Throwable,
                response: JSONArray?
            ) {
                Toast.makeText(
                    context, "Failed to get languages",
                    Toast.LENGTH_SHORT
                ).show()

                progressBar.visibility = View.INVISIBLE

            }
        })
    }
    //----------

    private fun getAudioPermission() {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.RECORD_AUDIO)
            }
            == PackageManager.PERMISSION_GRANTED) {
            audioPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                context as Activity, arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_REQUEST_RECORD_AUDIO
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                           permissions: Array<String>,
                                           grantResults: IntArray) {


        when (requestCode) {
            PERMISSIONS_REQUEST_RECORD_AUDIO -> {
                audioPermissionGranted = false
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    audioPermissionGranted = true

                }
            }
        }
    }

    private fun textToSpeechTranslation(
        from: String,
        to: String,
        mOgMsg: TextView?,
        mTvMsg: TextView?,
        context: Context
    ) {
        val msg: String = mOgMsg?.text.toString()

        val url =
            "http://104.248.113.55:8080/translate/textToText"

        val client: AsyncHttpClient = AsyncHttpClient()
        val params = RequestParams()

        params.put("from", from)
        params.put("to", to)
        params.put("message", msg)

        client.post(url, params, object : JsonHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<Header>?,
                response: JSONObject?
            ) {
                if (response != null) {

                    Log.d("Response", response.toString())

                    val translationsArray: JSONArray = response.getJSONArray("translations")
                    val translatedMsg = (translationsArray.get(0) as JSONObject).getString("text")

                    if (mTvMsg != null) {
                        mTvMsg.text = translatedMsg
                    }

                    Toast.makeText(
                        context, "Text to text translation successful",
                        Toast.LENGTH_SHORT
                    ).show()

                    val speechConfig = SpeechConfig.fromSubscription(
                        "ENTER TEXT TO SPEECH KEY HERE",
                        "eastus"
                    )
                    val audioConfig = AudioConfig.fromDefaultSpeakerOutput()
                    val synthesizer = SpeechSynthesizer(speechConfig, audioConfig)
                    Log.d(
                        "Info",
                        "Translated Message: ${translatedMsg}"
                    )

                    val result = synthesizer.SpeakText(translatedMsg)
                    Log.d("Test", result.reason.toString())
                    if (result.reason === ResultReason.Canceled) {
                        val cancellationDetails =
                            SpeechSynthesisCancellationDetails.fromResult(result).toString()
                        Log.d("Test Details",
                            "Error synthesizing. Error detail: \n${cancellationDetails}\nDid you update the subscription info?"
                        )
                    }
                    result.close()

                } else {

                    Toast.makeText(
                        context, "Text to text translation failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<Header>?,
                e: Throwable,
                response: JSONArray?
            ) {
                Toast.makeText(
                    context, "Text to text translation failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun textToSpeechOnly(mMsg: TextView?){
        val speechConfig = SpeechConfig.fromSubscription(
            "ENTER TEXT TO SPEECH KEY HERE",
            "eastus"
        )
        val audioConfig = AudioConfig.fromDefaultSpeakerOutput()
        val synthesizer = SpeechSynthesizer(speechConfig, audioConfig)
        Log.d(
            "Info",
            "Translated Message: ${mMsg?.text.toString()}"
        )

        val result = synthesizer.SpeakText(mMsg?.text.toString())
        Log.d("Test", result.reason.toString())
        if (result.reason === ResultReason.Canceled) {
            val cancellationDetails =
                SpeechSynthesisCancellationDetails.fromResult(result).toString()
            Log.d("Test Details",
                "Error synthesizing. Error detail: \n${cancellationDetails}\nDid you update the subscription info?"
            )
        }
        result.close()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // Handle the result for our request code.
            SPEECH_TO_TEXT_REQUEST -> {
                // Safety checks to ensure data is available.
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // Retrieve the result array.
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    // Ensure result array is not null or empty to avoid errors.
                    if (!result.isNullOrEmpty()) {
                        // Recognized text is in the first position.
                        val recognizedText = result[0]
                        // Do what you want with the recognized text.

                        //CALL THE TRANSLATION API HERE!!!!!!
                        originalTextBox?.setText(recognizedText)
                        Log.d("Call the translation API here, with desired { to:, from:, message: }",recognizedText)

                        textToSpeechTranslation(fromLanguageCode, toLanguageCode, originalTextBox, translatedTextBox, requireContext())
                    }
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 100
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 2
        private const val SPEECH_TO_TEXT_REQUEST = 3
    }
}
