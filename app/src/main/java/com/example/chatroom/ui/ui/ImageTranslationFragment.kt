package com.example.chatroom.ui.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.chatroom.R
import com.example.chatroom.ui.MainActivity
import com.example.chatroom.ui.signup.SignUpActivity
import com.example.chatroom.ui.ui.chatroom.Language
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.lang.Exception


class ImageTranslationFragment : Fragment() {
    lateinit var imageView: ImageView
    var imageUrl: String = ""
    var arrStringName: ArrayList<String> = ArrayList()
    var arrLanguageObjects: ArrayList<Language> = ArrayList()
    private var languageSelected: String = ""
    val REQUEST_IMAGE_CAPTURE = 1


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
        val view: View = inflater.inflate(R.layout.fragment_image_translation, container, false)

        imageView = view.findViewById(R.id.imageTranslation_imageView)
        val translateImage: Button = view.findViewById(R.id.imageTranslation_translateImageButton)
        val translatedTextBox: EditText = view.findViewById(R.id.imageTranslation_translatedTextBox)
        val playAudio: Button = view.findViewById(R.id.imageTranslation_playAudioButton)

        imageView.setOnClickListener {

            val builder = AlertDialog.Builder(context);


            builder.setTitle("Choose an Image");

            builder.setMessage("Take a photo or choose an image from your gallery")

            builder.setNeutralButton("Take Photo", DialogInterface.OnClickListener {
                    dialog, id -> dispatchTakePictureIntent()
            })

            builder.setPositiveButton("Choose From Gallery", DialogInterface.OnClickListener {
                    dialog, id ->
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, ImageTranslationFragment.REQUEST_CODE)
            })

            val dialog: AlertDialog = builder.create()

            dialog.show()
        }

        translateImage.setOnClickListener{
            //Call API for translating image

            val builder = AlertDialog.Builder(context);
            val dialogView: View = View.inflate(context, R.layout.translate_image_dialog, null)

            val cancel: TextView = dialogView.findViewById<TextView>(R.id.translateImageDialog_cancelButton)
            val submit: TextView = dialogView.findViewById<TextView>(R.id.translateImageDialog_submitButton)
            val toLanguageSpinner: Spinner = dialogView.findViewById<Spinner>(R.id.translateImageDialog_toLanguageSpinner)
            val radioGroup: RadioGroup = dialogView.findViewById<RadioGroup>(R.id.translateImageDialog_radioGroup)
            val progressBar: ProgressBar = dialogView.findViewById<ProgressBar>(R.id.translateImageDialog_progressBar)


            var selectedRadioButton: String = ""
            var toSpinnerLanguageSelected: String = ""

            progressBar.visibility = View.VISIBLE
            submit.isEnabled = false


            radioGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
                val radio: RadioButton = dialogView.findViewById(checkedId)
                selectedRadioButton = radio.text.toString()
                Toast.makeText(context," On checked change : ${radio.text}",
                    Toast.LENGTH_SHORT).show()
            })

            builder.setView(dialogView);
            val dialog: AlertDialog = builder.create()


            cancel.setOnClickListener {
                Log.d("Cancel", "Hit cancel")

                dialog.cancel()

            }

            populateLanguageDropdown(toLanguageSpinner, progressBar, submit)

            submit.setOnClickListener {
                Log.d("Submit", "Hit submit")

                //Logic for translating text goes here
                Log.d("Selected", "Method: " + selectedRadioButton
                        + "\nTo Spinner Language: " + languageSelected)

                if(selectedRadioButton.equals("")){
                    Toast.makeText(context,"Please select a translation method",
                        Toast.LENGTH_SHORT).show()
                }
                else if(selectedRadioButton.equals("Image to Text")){
                    Log.d("To Language", (arrLanguageObjects.filter { it.name == languageSelected })[0].key)

                    val to: String = (arrLanguageObjects.filter { it.name == languageSelected })[0].key

                    //Call Image to Text API here
                }
                else if(selectedRadioButton.equals("Image to Talk")){
                    Log.d("To Language", (arrLanguageObjects.filter { it.name == languageSelected })[0].key)

                    val to: String = (arrLanguageObjects.filter { it.name == languageSelected })[0].key

                    //Call Image to Talk API here
                }
            }


            dialog.show()


        }

        playAudio.setOnClickListener {

        }


        return view
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


    private fun dispatchTakePictureIntent() {
        try{
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                context?.packageManager?.let {
                    takePictureIntent.resolveActivity(it)?.also {
                        startActivityForResult(takePictureIntent, 1)
                    }
                }
            }
        }
        catch (e: Exception){
            Log.d("Exception", e.toString())
            Toast.makeText(context,"Camera Permission Denied",
                Toast.LENGTH_SHORT).show()
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == ImageTranslationFragment.REQUEST_CODE) {
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
    }

    companion object {
        private const val REQUEST_CODE = 100
    }

}