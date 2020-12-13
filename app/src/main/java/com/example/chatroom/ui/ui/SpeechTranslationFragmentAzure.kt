package com.example.chatroom.ui.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.chatroom.R
import com.example.chatroom.ui.ui.chatroom.Language
import com.google.gson.Gson
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.microsoft.cognitiveservices.speech.ResultReason
import com.microsoft.cognitiveservices.speech.translation.SpeechTranslationConfig
import com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.fragment_speech_translation.*
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text


class SpeechTranslationFragmentAzure : Fragment() {

    var arrStringName: ArrayList<String> = ArrayList()
    var arrLanguageObjects: ArrayList<Language> = ArrayList()
    private var languageSelected: String = ""
    private var audioPermissionGranted = false
    private var fromSpinnerLanguageSelected: String = ""
    private var fromIndex: Int = 0
    private var toSpinnerLanguageSelected: String = ""


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
        val originalTextBox: EditText = view.findViewById(R.id.speechTranslation_originalTextBox)
        val translatedTextBox: EditText = view.findViewById(R.id.speechTranslation_translatedTextBox)
        val playAudio: Button = view.findViewById(R.id.speechTranslation_playAudioButton)

//

        translateSpeech.setOnClickListener {
            getAudioPermission()
            setTranslateMessageDialog(requireContext())

            //--------------------------------------------------

            //--------------------------------------------------
        }

        playAudio.setOnClickListener {

        }


        return view
    }

    //----------
    private fun setTranslateMessageDialog(context: Context) {
        val arrStringName: ArrayList<String> = ArrayList()
        val arrLanguageObjects: ArrayList<Language> = ArrayList()
        val arrBCP47Codes: Array<String> = resources.getStringArray(R.array.bcp_47_codes)
        val arrBCP47Languages: Array<String> = resources.getStringArray(R.array.bcp_47_languages)

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
            //speakNowPrompt.visibility = View.VISIBLE
            //fromPrompt.visibility = View.INVISIBLE
            //toPrompt.visibility = View.INVISIBLE
            //fromLanguageSpinner.visibility = View.INVISIBLE
            //toLanguageSpinner.visibility = View.INVISIBLE

            //Logic for translating text goes here
            Log.d("SUBMIT TEST","From Spinner Language: " + arrBCP47Languages[fromIndex]
                        + "\nTo Spinner Language: " + toSpinnerLanguageSelected)

            //This is how you will get the proper value for the selected languages
            Log.d(
                "From Language",
                (arrLanguageObjects.filter { it.name == fromSpinnerLanguageSelected })[0].key
            )
            Log.d(
                "To Language",
                (arrLanguageObjects.filter { it.name == toSpinnerLanguageSelected })[0].key
            )

            //val from: String = (arrLanguageObjects.filter { it.name == fromSpinnerLanguageSelected })[0].key
            val from: String = arrBCP47Codes[fromIndex]
            val to: String = (arrLanguageObjects.filter { it.name == toSpinnerLanguageSelected })[0].key

            Log.d("Froms", "${arrBCP47Codes[fromIndex]}, ${arrBCP47Languages[fromIndex]}, $fromIndex")

            //textToTextTranslation(from, to, mTvMsg, context, dialog)
            //Recognizer and translator
            if (audioPermissionGranted) {
                val translationConfig = SpeechTranslationConfig.fromSubscription(
                    "ENTER TEXT TO SPEECH KEY HERE", "eastus"
                )

                //val fromLanguage = "en-US"
                //val toLanguages = arrayOf("it", "fr", "de")
                translationConfig.speechRecognitionLanguage = from////"en-US"
                //for (language in toLanguages) {
                //    translationConfig.addTargetLanguage(language)
                //}
                translationConfig.addTargetLanguage(to)

                TranslationRecognizer(translationConfig).use { recognizer ->
                    Log.d("TEST -1","Say something in ${from} and we'll translate...")
                    val result = recognizer.recognizeOnceAsync().get()
                    if (result.reason == ResultReason.TranslatedSpeech) {
                        Log.d("TEST 0","Recognized: \"${result.text}\"\n")
                        speechTranslation_originalTextBox.setText(result.text)
                        speechTranslation_translatedTextBox.setText(result.translations[to])
                        //for ((key, value) in result.translations) {
                        //    Log.d("TEST 1","Translated into ${key}: ${value}\n")
                        //}
                    } else {
                        Log.d("TEST 1", result.reason.toString())
                    }

                    //speakNowPrompt.visibility = View.INVISIBLE
                    //fromPrompt.visibility = View.VISIBLE
                    //toPrompt.visibility = View.VISIBLE
                    //fromLanguageSpinner.visibility = View.VISIBLE
                    //toLanguageSpinner.visibility = View.VISIBLE
                    dialog.cancel()
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
                    val adapter2 = ArrayAdapter(
                        context,
                        android.R.layout.simple_spinner_item, arrBCP47Languages
                    )
                    fromLanguageSpinner.adapter = adapter2
                    toLanguageSpinner.adapter = adapter

                    val prefs: SharedPreferences =
                        context.getSharedPreferences("info", Context.MODE_PRIVATE)

                    if(toSpinnerLanguageSelected == "" && prefs.getString("language", null) == null){
                        fromSpinnerLanguageSelected = arrStringName.get(0)
                        toSpinnerLanguageSelected = arrStringName.get(0)

                        if (adapter != null) {
                            fromLanguageSpinner.setSelection(adapter2.getPosition(fromSpinnerLanguageSelected))
                        }
                    }
                    else if(toSpinnerLanguageSelected == "" && prefs.getString("language", null) != null){
                        val gsonObject = Gson()

                        val language: Language = gsonObject.fromJson(prefs.getString("language", null), Language::class.java)

                        fromSpinnerLanguageSelected = language.name

                        if (adapter != null) {
                            fromLanguageSpinner.setSelection(adapter2.getPosition(fromSpinnerLanguageSelected))
                        }

                    }
                    else{
                        fromSpinnerLanguageSelected = toSpinnerLanguageSelected.plus("")

                        if (adapter != null) {
                            fromLanguageSpinner.setSelection(adapter2.getPosition(toSpinnerLanguageSelected))
                        }
                    }

                    toSpinnerLanguageSelected = arrStringName.get(0)

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
                                fromIndex = position
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

    private fun populateLanguageDropdown(languageDropDown: Spinner, progressBar: ProgressBar, submit: TextView){
        val url =
            "https://api.cognitive.microsofttranslator.com/languages?api-version=3.0"

        val client: AsyncHttpClient = AsyncHttpClient()

        client.get(url, object : JsonHttpResponseHandler()
        {
            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?)
            {
                if(response != null){

                    Log.d("Response", response.toString())
                    val translationObj: JSONObject = JSONObject(response.toString()).getJSONObject("translation")
                    val keys: Iterator<String> = translationObj.keys()


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

                    val adapter = context?.let {
                        ArrayAdapter(
                            it,
                            android.R.layout.simple_spinner_item, arrStringName)
                    }

                    languageDropDown.adapter = adapter

                    languageSelected = arrStringName.get(0)

                    languageDropDown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            languageSelected = arrStringName.get(position)
                        }

                    }

                    progressBar.visibility = View.INVISIBLE
                    submit.isEnabled = true

                }else{

                    progressBar.visibility = View.INVISIBLE
                    submit.isEnabled = true

                    Toast.makeText(context,"Failed to get languages",
                        Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, e: Throwable, response: JSONArray?)
            {
                progressBar.visibility = View.INVISIBLE
                submit.isEnabled = true

                Toast.makeText(context,"Failed to get languages",
                    Toast.LENGTH_SHORT).show()

            }
        })
    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
/*        if (resultCode == Activity.RESULT_OK && requestCode == SpeechTranslationFragment.REQUEST_CODE) {
            imageView.setImageURI(data?.data) // handle chosen image
            Log.d("Image url", data?.data.toString())

            imageUrl = data?.data.toString()
        }
        else if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val baos = ByteArrayOutputStream()

            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

            imageView.setImageBitmap(imageBitmap)

        }

 */
    }

    companion object {
        private const val REQUEST_CODE = 100
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 2
    }
}
