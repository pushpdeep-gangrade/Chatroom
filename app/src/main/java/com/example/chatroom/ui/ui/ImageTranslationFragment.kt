package com.example.chatroom.ui.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.chatroom.R
import com.example.chatroom.ui.ui.chatroom.Language
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.loopj.android.http.SyncHttpClient
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVision
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionClient
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionManager
import com.microsoft.azure.cognitiveservices.vision.computervision.implementation.ComputerVisionImpl
import com.microsoft.azure.cognitiveservices.vision.computervision.models.OcrDetectionLanguage
import com.microsoft.azure.cognitiveservices.vision.computervision.models.OperationStatusCodes
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadInStreamHeaders
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadOperationResult
import com.microsoft.cognitiveservices.speech.ResultReason
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechSynthesisCancellationDetails
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import cz.msebera.android.httpclient.Header
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*


class ImageTranslationFragment : Fragment() {
    var subscriptionKey = "fd6436de1fdd4564a53423c0f2512098"
    var endpoint = "https://amad-vision-api-image-to-text.cognitiveservices.azure.com/"
    data class visionLanguage(val name: String, val code: String)
    lateinit var imageView: ImageView
    var imageUrl: String = ""
    var arrStringName: ArrayList<String> = ArrayList()
    var arrLanguageObjects: ArrayList<Language> = ArrayList()
    private var languageSelected: String = ""
    private var fromlanguageSelected: String = ""
    private lateinit var imageByteArray : ByteArray
    private var fromlanguageCode: String = ""
    val REQUEST_IMAGE_CAPTURE = 1

    private var speechIndex: Int = 0
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        val compVisClient: ComputerVisionClient =
            ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_image_translation, container, false)
        imageView = view.findViewById(R.id.imageTranslation_imageView)
        val translateImage: Button = view.findViewById(R.id.imageTranslation_translateImageButton)
        val translatedTextBox: TextView = view.findViewById(R.id.imageTranslation_translatedTextBox)
        val playAudio: Button = view.findViewById(R.id.imageTranslation_playAudioButton)

        imageView.setOnClickListener {

            val builder = AlertDialog.Builder(context);

            builder.setTitle("Choose an Image");

            builder.setMessage("Take a photo or choose an image from your gallery")

            builder.setNeutralButton("Take Photo", DialogInterface.OnClickListener { dialog, id ->
                dispatchTakePictureIntent()
            })

            builder.setPositiveButton(
                "Choose From Gallery",
                DialogInterface.OnClickListener { dialog, id ->
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, ImageTranslationFragment.REQUEST_CODE)
                })

            val dialog: AlertDialog = builder.create()

            dialog.show()
        }

        translateImage.setOnClickListener {
            //Call API for translating image

            val builder = AlertDialog.Builder(context);
            val dialogView: View = View.inflate(context, R.layout.translate_image_dialog, null)

            val cancel: TextView =
                dialogView.findViewById<TextView>(R.id.translateImageDialog_cancelButton)
            val submit: TextView =
                dialogView.findViewById<TextView>(R.id.translateImageDialog_submitButton)
            val fromLanguageSpinner: Spinner =
                dialogView.findViewById<Spinner>(R.id.translateImageDialog_fromLanguageSpinner)
            val toLanguageSpinner: Spinner =
                dialogView.findViewById<Spinner>(R.id.translateImageDialog_toLanguageSpinner)
            val radioGroup: RadioGroup =
                dialogView.findViewById<RadioGroup>(R.id.translateImageDialog_radioGroup)
            progressBar =
                dialogView.findViewById<ProgressBar>(R.id.translateImageDialog_progressBar)

            var selectedRadioButton: String = ""
            var toSpinnerLanguageSelected: String = ""

            progressBar.visibility = View.VISIBLE
            submit.isEnabled = true

            radioGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
                val radio: RadioButton = dialogView.findViewById(checkedId)
                selectedRadioButton = radio.text.toString()
                Toast.makeText(
                    context, " On checked change : ${radio.text}",
                    Toast.LENGTH_SHORT
                ).show()
            })

            builder.setView(dialogView);
            val dialog: AlertDialog = builder.create()

            cancel.setOnClickListener {
                Log.d("Cancel", "Hit cancel")
                dialog.cancel()
            }

            context?.let { it1 ->
                ArrayAdapter.createFromResource(
                    it1,
                    R.array.vision_7_languages,
                    android.R.layout.simple_spinner_item
                ).also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    // Apply the adapter to the spinner
                    fromLanguageSpinner.adapter = adapter
                    toLanguageSpinner.adapter = adapter
                }
            }



            populateLanguageDropdownStrings(fromLanguageSpinner, toLanguageSpinner)
            //populateLanguageDropdown(fromLanguageSpinner, toLanguageSpinner, progressBar, submit)
            progressBar.visibility = View.INVISIBLE

            submit.setOnClickListener {
                Log.d("Submit", "Hit submit")

                //Logic for translating text goes here
                Log.d(
                    "Selected", "Method: " + selectedRadioButton
                            + "\nTo Spinner Language: " + languageSelected
                )

                if (selectedRadioButton.equals("")) {
                    Toast.makeText(
                        context, "Please select a translation method",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (selectedRadioButton.equals("Image to Text")) {
                    Log.d(
                        "To Language",
                        (arrLanguageObjects.filter { it.name == languageSelected })[0].key
                    )

                    val to: String =
                        (arrLanguageObjects.filter { it.name == languageSelected })[0].key

                    //Call Image to Text API here
                    convertImageToText(fromlanguageCode , imageByteArray, to,
                        translatedTextBox,dialog, "Text" )

                } else if (selectedRadioButton.equals("Image to Talk")) {
                    Log.d(
                        "To Language",
                        (arrLanguageObjects.filter { it.name == languageSelected })[0].key
                    )

                    val to: String =
                        (arrLanguageObjects.filter { it.name == languageSelected })[0].key

                    //Call Image to Talk API here
                    convertImageToText(fromlanguageCode , imageByteArray, to,
                        translatedTextBox,dialog, "Talk" )
                }
            }

            dialog.show()

        }

        playAudio.setOnClickListener {

            val speechConfig = SpeechConfig.fromSubscription(
                SPEECH_SUBS_KEY,
                "eastus"
            )

            Log.d("Speech","Language Code: ${arrLanguageObjects[speechIndex].region}\nVoice: ${arrLanguageObjects[speechIndex].voice}")
            speechConfig.speechSynthesisLanguage = arrLanguageObjects[speechIndex].region
            speechConfig.speechSynthesisVoiceName = arrLanguageObjects[speechIndex].voice

            val audioConfig = AudioConfig.fromDefaultSpeakerOutput()
            val synthesizer = SpeechSynthesizer(speechConfig, audioConfig)
            val result = synthesizer.SpeakText(translatedTextBox.text.toString())

            if (result.reason === ResultReason.Canceled) {
                val cancellationDetails =
                    SpeechSynthesisCancellationDetails.fromResult(result).toString()
                Log.d("Test Details",
                    "Error synthesizing. Error detail: \n${cancellationDetails}\nDid you update the subscription info?"
                )
            }
            result.close()
        }

        return view
    }

    private fun populateLanguageDropdownStrings(
        fromLanguageDropDown: Spinner,
        toLanguageDropDown: Spinner
    ){
        val languagesStringArr = resources.getStringArray(R.array.vision_7_languages)
        val languageCodesStringArr =  resources.getStringArray(R.array.vision_7_code)
        val languagesRegionStringArr = resources.getStringArray(R.array.vision_7_codes_with_region)
        val languageVoiceStringArr = resources.getStringArray(R.array.vision_7_voices)


        for (i in languagesStringArr.indices)
        {
            arrStringName.add(languagesStringArr[i])

            val language: Language = Language(languageCodesStringArr[i], languagesStringArr[i],
                languagesRegionStringArr[i], languageVoiceStringArr[i])

            arrLanguageObjects.add(language)

        }

        fromLanguageDropDown.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    fromlanguageCode = arrLanguageObjects.get(position).key
                    fromlanguageSelected = arrStringName.get(position)
                }
        }

        toLanguageDropDown.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    languageSelected = arrStringName.get(position)
                    speechIndex = position
                }

        }
    }

    private fun dispatchTakePictureIntent() {
        try {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                context?.packageManager?.let {
                    takePictureIntent.resolveActivity(it)?.also {
                        startActivityForResult(takePictureIntent, 1)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("Exception", e.toString())
            Toast.makeText(
                context, "Camera Permission Denied",
                Toast.LENGTH_SHORT
            ).show()
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == ImageTranslationFragment.REQUEST_CODE) {
            //imageView.setImageURI(data?.data) // handle chosen image
            Log.d("Image url", data?.data.toString())

            try {

                val uri: Uri = Uri.parse(data?.data.toString())

                val imageBitmap: Bitmap =
                    BitmapFactory.decodeStream(requireActivity().contentResolver.openInputStream(uri))

                val baos = ByteArrayOutputStream()

                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

                imageByteArray =  baos.toByteArray()

                imageView.setImageBitmap(imageBitmap)

            } catch (e: IOException) {
                Log.d("Image Load Error", e.toString())
            }

            imageUrl = data?.data.toString()

        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val baos = ByteArrayOutputStream()

            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

           imageByteArray =  baos.toByteArray()
            imageView.setImageBitmap(imageBitmap)

        }
    }

    private fun convertImageToText(
        fromLang: String,
        imageBitmapByteArray: ByteArray,
        to : String,
        textView: TextView,
        dialog: AlertDialog,
        convertType: String
    ) {
        progressBar.visibility = View.VISIBLE

        val compVisClient: ComputerVisionClient =
            ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint)
        try {
            AsyncTask.execute {
                val vision: ComputerVisionImpl =
                    compVisClient.computerVision() as ComputerVisionImpl
                val responseHeader: ReadInStreamHeaders =
                    vision.readInStreamWithServiceResponseAsync(
                        imageBitmapByteArray,
                        OcrDetectionLanguage.fromString(fromLang)
                    )
                        .toBlocking()
                        .single()
                        .headers()

                val operationLocation = responseHeader.operationLocation()

                getAndPrintReadResult(vision, operationLocation, textView, to, dialog, convertType)
            }


        } catch (e: Exception) {
            e.message?.let { Log.d("error", it) };
            e.printStackTrace();
        }

    }

    private fun getAndPrintReadResult(
        vision: ComputerVision,
        operationLocation: String,
        textView: TextView,
        to : String,
        dialog: AlertDialog,
        convertType: String
    ) {
        // Extract OperationId from Operation Location
        val operationId =
            extractOperationIdFromOpLocation(operationLocation)
        var pollForResult = true
        var readResults: ReadOperationResult? = null
        while (pollForResult) {
            // Poll for result every second
            Thread.sleep(1000)
            readResults = vision.getReadResult(UUID.fromString(operationId))
            // The results will no longer be null when the service has finished processing the request.
            if (readResults != null) {
                // Get request status
                val status = readResults.status()
                if (status == OperationStatusCodes.FAILED || status == OperationStatusCodes.SUCCEEDED) {
                    pollForResult = false
                }
            }
        }
        for (pageResult in readResults!!.analyzeResult().readResults()) {
            println("Printing Read results for page " + pageResult.page())
            val builder = StringBuilder()
            for (line in pageResult.lines()) {
                builder.append(line.text())
                builder.append("\n")
            }
            context?.let {
                textToTextTranslation(fromlanguageCode, to, builder.toString(), textView ,  it, dialog, convertType )
            }
        }
    }

    private fun extractOperationIdFromOpLocation(operationLocation: String?): String {
        if (operationLocation != null && !operationLocation.isEmpty()) {
            val splits =
                operationLocation.split("/".toRegex()).toTypedArray()
            if (splits != null && splits.size > 0) {
                return splits[splits.size - 1]
            }
        }
        throw IllegalStateException("Something went wrong: Couldn't extract the operation id from the operation location")
    }

    private fun textToTextTranslation(
        from: String,
        to: String,
        message : String,
        mTvMsg: TextView?,
        context: Context,
        dialog: AlertDialog,
        convertType: String
    ) {
        val url =
            "http://104.248.113.55:8080/translate/textToText"

        val client: SyncHttpClient = SyncHttpClient()
        val params = RequestParams()

        params.put("from", from)
        params.put("to", to)
        params.put("message", message)

        client.post(url, params, object : JsonHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<Header>?,
                response: JSONObject?
            ) {
                if (response != null) {
                    val translationsArray: JSONArray = response.getJSONArray("translations")
                    val translatedMsg = (translationsArray.get(0) as JSONObject).getString("text")
                    if (mTvMsg != null) {
                       mTvMsg.text = translatedMsg
                    }
                    progressBar.visibility = View.INVISIBLE
                    dialog.cancel()

                    if(convertType == "Talk"){
                        val speechConfig = SpeechConfig.fromSubscription(
                            SPEECH_SUBS_KEY,
                            "eastus"
                        )

                        Log.d("Speech","Language Code: ${arrLanguageObjects[speechIndex].region}\nVoice: ${arrLanguageObjects[speechIndex].voice}")

                        speechConfig.speechSynthesisLanguage = arrLanguageObjects[speechIndex].region
                        speechConfig.speechSynthesisVoiceName = arrLanguageObjects[speechIndex].voice

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
                    }

                  //  progressBar.visibility = View.INVISIBLE


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

                progressBar.visibility = View.INVISIBLE

            }
        })

    }

    /*private fun populateLanguageDropdown(
       fromLanguageDropDown: Spinner,
       languageDropDown: Spinner,
       progressBar: ProgressBar,
       submit: TextView
   ) {
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

                   val adapter = context?.let {
                       ArrayAdapter(
                           it,
                           android.R.layout.simple_spinner_item, arrStringName
                       )
                   }

                   languageDropDown.adapter = adapter

                   languageSelected = arrStringName.get(0)

                   fromLanguageDropDown.onItemSelectedListener =
                       object : AdapterView.OnItemSelectedListener {
                           override fun onNothingSelected(parent: AdapterView<*>?) {

                           }

                           override fun onItemSelected(
                               parent: AdapterView<*>?,
                               view: View?,
                               position: Int,
                               id: Long
                           ) {
                               val arr = resources.getStringArray(R.array.vision_7_languages)
                               val arrCode = resources.getStringArray(R.array.vision_7_code)
                               fromlanguageCode = arrCode.get(position)
                               fromlanguageSelected = arr.get(position)
                           }

                       }
                   languageDropDown.onItemSelectedListener =
                       object : AdapterView.OnItemSelectedListener {
                           override fun onNothingSelected(parent: AdapterView<*>?) {
                           }

                           override fun onItemSelected(
                               parent: AdapterView<*>?,
                               view: View?,
                               position: Int,
                               id: Long
                           ) {
                               languageSelected = arrStringName.get(position)
                           }

                       }

                   progressBar.visibility = View.INVISIBLE
                   submit.isEnabled = true

               } else {

                   progressBar.visibility = View.INVISIBLE
                   submit.isEnabled = true

                   Toast.makeText(
                       context, "Failed to get languages",
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
               progressBar.visibility = View.INVISIBLE
               submit.isEnabled = true

               Toast.makeText(
                   context, "Failed to get languages",
                   Toast.LENGTH_SHORT
               ).show()

           }
       })
   }*/

    companion object {
        private const val REQUEST_CODE = 100
        var SPEECH_SUBS_KEY = "API KEY HERE"
    }

}