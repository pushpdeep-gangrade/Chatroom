package com.example.chatroom.ui.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.example.chatroom.R
import com.example.chatroom.ui.ui.chatroom.Language
import com.google.gson.Gson
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONArray
import org.json.JSONObject


class SettingsFragment : Fragment() {
    private var autoTranslateChecked: Boolean? = false
    private var locationPermissionChecked: Boolean? = false
    private var micPermissionChecked: Boolean? = false
    private var cameraPermissionChecked: Boolean? = false
    private var locationPermissionGranted: Boolean = false
    private var cameraPermissionGranted: Boolean = false
    private var micPermissionGranted: Boolean = false
    val arrStringName: ArrayList<String> = ArrayList()
    val arrLanguageObjects: ArrayList<Language> = ArrayList()
    private lateinit var languageDropDown: Spinner
    private var languageSelected: String = ""

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var autoTranslate: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var locationPermission: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var micPermission: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var cameraPermission: Switch
    lateinit var saveSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_settings, container, false)

        autoTranslate = view.findViewById(R.id.settings_autoTranslateSwitch)
        locationPermission = view.findViewById(R.id.settings_locationPermissionsSwitch)
        micPermission = view.findViewById(R.id.settings_micPermissionSwitch)
        cameraPermission = view.findViewById(R.id.settings_cameraPermissionSwitch)
        saveSettings = view.findViewById(R.id.settings_saveSettingsButton)
        languageDropDown = view.findViewById(R.id.settings_autoTranslateDropdown)

        populateLanguageDropdown()



        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
            == PackageManager.PERMISSION_GRANTED) {
            locationPermission.isChecked = true
        }

        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.RECORD_AUDIO
                )
            }
            == PackageManager.PERMISSION_GRANTED) {
            micPermission.isChecked = true
        }

        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.CAMERA
                )
            }
            == PackageManager.PERMISSION_GRANTED) {
            cameraPermission.isChecked = true
        }


        autoTranslate.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Auto-Translate:ON" else "Auto-Translate:OFF"

            autoTranslateChecked = isChecked

            Toast.makeText(context, message,
                Toast.LENGTH_SHORT).show()
        }

        locationPermission.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Location:ON" else "Location:OFF"

            if(isChecked){
                getLocationPermission()
            }

            locationPermissionChecked = isChecked

            Toast.makeText(context, message,
                Toast.LENGTH_SHORT).show()
        }

        micPermission.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Mic:ON" else "Mic:OFF"

            if(isChecked){
                getMicPermission()
            }

            micPermissionChecked = isChecked

            Toast.makeText(context, message,
                Toast.LENGTH_SHORT).show()
        }

        cameraPermission.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Camera:ON" else "Camera:OFF"

            if(isChecked){
                getCameraPermission()
            }

            cameraPermissionChecked = isChecked

            Toast.makeText(context, message,
                Toast.LENGTH_SHORT).show()
        }

        saveSettings.setOnClickListener {
            val prefs: SharedPreferences =
                requireContext().getSharedPreferences("info", Context.MODE_PRIVATE)

            if(autoTranslateChecked == true){
                //Automatically check to see what language is being used or let user select a language
                Log.d("Auto-Translate Checked", "Auto-Translate Checked")

                val gsonObject = Gson()

                val language = (arrLanguageObjects.filter { it.name == languageSelected })[0]

                prefs.edit().putString("language", gsonObject.toJson(language)).apply()


                val currentLanguage = gsonObject.fromJson(prefs.getString("language", null), Language::class.java)


                Log.d("Language", currentLanguage.toString())

                Toast.makeText(context,"Auto-Translate to " .plus(currentLanguage),
                    Toast.LENGTH_SHORT).show()

            }
            else{
                prefs.edit().putString("language", null).apply()
                Toast.makeText(context,"Auto-Translate turned off",
                    Toast.LENGTH_SHORT).show()
            }

        }

        return view
    }

    private fun populateLanguageDropdown(){

        val languagesStringArr = resources.getStringArray(R.array.text_to_speech_languages)
        val languageCodesStringArr = resources.getStringArray(R.array.text_to_speech_codes)
        val languagesRegionStringArr = resources.getStringArray(R.array.text_to_speech_codes_with_region)
        val languageVoiceStringArr = resources.getStringArray(R.array.text_to_speech_voices)

        for (i in languagesStringArr.indices) {
            arrStringName.add(languagesStringArr[i])

            val language: Language = Language(
                languageCodesStringArr[i], languagesStringArr[i],
                languagesRegionStringArr[i], languageVoiceStringArr[i]
            )

            arrLanguageObjects.add(language)

        }

        val adapter = context?.let {
            ArrayAdapter(
                it,
                android.R.layout.simple_spinner_item, arrStringName
            )
        }

        languageDropDown.adapter = adapter

        languageSelected = arrStringName.get(0)


        val prefs: SharedPreferences =
            requireContext().getSharedPreferences("info", Context.MODE_PRIVATE)

        if(prefs.getString("language", null) != null){
            autoTranslate.isChecked = true
            autoTranslateChecked = true

            val gsonObject = Gson()

            val language: Language = gsonObject.fromJson(prefs.getString("language", null), Language::class.java)

            if (adapter != null) {
                languageDropDown.setSelection(adapter.getPosition(language.name))
            }

            languageSelected = language.name

            Toast.makeText(
                context, "Current auto-translate language: ".plus(language.name),
                Toast.LENGTH_SHORT
            ).show()
        }

        languageDropDown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                languageSelected = arrStringName.get(position)
            }

        }

        /*val url =
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


                    val prefs: SharedPreferences =
                        requireContext().getSharedPreferences("info", Context.MODE_PRIVATE)

                    if(prefs.getString("language", null) != null){
                        autoTranslate.isChecked = true

                        val gsonObject = Gson()

                        val language: Language = gsonObject.fromJson(prefs.getString("language", null), Language::class.java)

                        if (adapter != null) {
                            languageDropDown.setSelection(adapter.getPosition(language.name))
                        }

                        languageSelected = language.name

                        Toast.makeText(
                            context, "Current auto-translate language: ".plus(language.name),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    languageDropDown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            languageSelected = arrStringName.get(position)
                        }

                    }

                }else{

                    Toast.makeText(context,"Failed to get languages",
                        Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, e: Throwable, response: JSONArray?)
            {
                Toast.makeText(context,"Failed to get languages",
                    Toast.LENGTH_SHORT).show()

            }
        })*/
    }

    private fun getLocationPermission() {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
            == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context,"Location permission already granted",
                Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                context as Activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                SettingsFragment.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    private fun getCameraPermission() {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.CAMERA
                )
            }
            == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context,"Camera permission already granted",
                Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                context as Activity, arrayOf(Manifest.permission.CAMERA),
                SettingsFragment.PERMISSIONS_REQUEST_CAMERA
            )
        }
    }

    private fun getMicPermission() {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.RECORD_AUDIO
                )
            }
            == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context,"Mic permission already granted",
                Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                context as Activity, arrayOf(Manifest.permission.RECORD_AUDIO),
                SettingsFragment.PERMISSIONS_REQUEST_AUDIO
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        when (requestCode) {
            SettingsFragment.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                }
            }
        }

        when (requestCode) {
            SettingsFragment.PERMISSIONS_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    cameraPermissionGranted = true
                }
            }
        }

        when (requestCode) {
            SettingsFragment.PERMISSIONS_REQUEST_AUDIO -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    micPermissionGranted = true
                }
            }
        }

    }

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val PERMISSIONS_REQUEST_CAMERA = 2
        private const val PERMISSIONS_REQUEST_AUDIO = 3
    }
}