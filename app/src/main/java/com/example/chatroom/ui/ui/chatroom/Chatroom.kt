package com.example.chatroom.ui.ui.chatroom

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.chatroom.R
import com.example.chatroom.data.model.PickedPlace
import com.example.chatroom.data.model.RideRequest
import com.example.chatroom.data.model.User
import com.example.chatroom.databinding.FragmentChatroomBinding
import com.example.chatroom.ui.MainActivity
import com.example.chatroom.ui.ui.driver.PotentialRiderFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.maps.android.PolyUtil
import org.json.JSONObject
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

var messageUser: User? = null
var messageUserId: String = ""
private var listchats = mutableListOf<Chat>()
private var listActiveUsers = mutableListOf<String>()
private var listActiveUsersNames = mutableListOf<String>()
private var listActiveUserImageURLs = mutableListOf<String>()
private var listRideRequests = mutableListOf<String>()
private var listRideRequestsObjects = mutableListOf<RideRequest>()
private var listRideRequestNames = mutableListOf<String>()
private var listSharedLocationUserNames = mutableListOf<String>()
private var listSharedLocations = mutableListOf<LatLng>()
private var locationIsShared = false
private var locationPermissionGranted = false
private var lastKnownLocation: Location? = null
private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
private var activeUsers = mutableListOf<User>()
var chatRoomId: String? = null
private val RECORD_REQUEST_CODE = 101
private var audioPermissionGranted = false


class Chatroom : Fragment() {
    private var speechRecognizer: SpeechRecognizer? = null
    private var _binding: FragmentChatroomBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        chatRoomId = arguments?.getString("chatroomId")
        _binding = FragmentChatroomBinding.inflate(inflater, container, false)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatRoomId = arguments?.getString("chatroomId")

        Log.d("Active Status", "User is now active")

//        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
//        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//        speechRecognizerIntent.putExtra(
//            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//        )
//        speechRecognizerIntent.putExtra(
//            RecognizerIntent.EXTRA_LANGUAGE,
//            Locale.getDefault()
//        )
//
//
//        speechRecognizer?.run {
//            speechRecognizerIntent.putExtra(
//                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//            )
//            speechRecognizerIntent.putExtra(
//                RecognizerIntent.EXTRA_LANGUAGE,
//                Locale.getDefault()
//            )
//
//
//            setRecognitionListener(object : RecognitionListener {
//                override fun onReadyForSpeech(bundle: Bundle) {  }
//                override fun onRmsChanged(p0: Float) {
//
//                }
//
//                override fun onBufferReceived(p0: ByteArray?) {
//
//                }
//
//                override fun onPartialResults(p0: Bundle?) {
//                }
//
//                override fun onEvent(p0: Int, p1: Bundle?) {
//
//                }
//
//                override fun onBeginningOfSpeech() {
//                    binding.inputMessage.setText("")
//                    binding.inputMessage.setHint("Listening...")
//                    binding.talkToText.setImageResource(R.drawable.ic_mic_on)
//                }
//
//                override fun onEndOfSpeech() {
//                    speechRecognizer!!.stopListening()
//                    binding.inputMessage.setHint("Type message here...")
//                }
//
//                override fun onError(p0: Int) {
//                    Log.d("error",p0.toString())
//
//                }
//
//                override fun onResults(p0: Bundle?) {
//                    val data = p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//                    binding.inputMessage.append(data!![0])
//                    binding.talkToText.setImageResource(R.drawable.ic_mic_off)
//
//                }
//            })
//        }

        fusedLocationProviderClient =
            context?.let { LocationServices.getFusedLocationProviderClient(it) }!!
        //Weird if I try to check permission on button click for share location button
        getLocationPermission()
        getDeviceLocation()
        //----------------------------------------------------------------------------

        initializeList()
        setRideRequestListener(view)
        setShareLocationListener(view)

                MainActivity.dbRef.child("users").child(MainActivity.auth.currentUser?.uid.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.d("demo", "Firebase event cancelled on getting user data")
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    messageUser = dataSnapshot.getValue<User>()
                    Log.d(
                        "Message User",
                        messageUser?.userId.toString() + " whaaaaaaaaaaaaaaaaaaaaaaaaaaat"
                    )
                    messageUserId = messageUser?.userId.toString()
                    MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                        .child("listActiveUsers").child(messageUser?.userId.toString()).setValue(
                            messageUser
                        )
                    getActiveUsers()
                }
            })

//        binding.talkToText.setOnClickListener {
//            if (ContextCompat.checkSelfPermission(
//                    requireContext(),
//                    Manifest.permission.RECORD_AUDIO
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                checkPermission()
//            }
//        }

//        binding.talkToText.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
//            if (motionEvent.action == MotionEvent.ACTION_UP) {
//                speechRecognizer?.run { stopListening() }
//            }
//            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
//                speechRecognizer?.run {startListening(speechRecognizerIntent)}
//            }
//            false
//        })

        binding.chatroomActiveUsers.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Active Users")

            builder.setNegativeButton("Close", { dialog, id ->
                dialog.cancel()
            })

            builder.setCancelable(false)

            //var userNames = Array<String>(listActiveUsers.size){ i -> listActiveUsersNames[i] }
            var userNames = listActiveUsersNames.toTypedArray()

            builder.setItems(userNames, { dialogInterface: DialogInterface, i: Int ->
                val bundle = bundleOf("userData" to listActiveUsers[i])
                view.findNavController().navigate(R.id.action_chatroom_to_profile, bundle)
            })

            var dialog: AlertDialog = builder.create()
            dialog.show()
        }

        binding.sendMessage.setOnClickListener {
            val timestamp =
                LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
            val message = binding.inputMessage.text.toString()
            //val templist = mutableListOf<String>()
            val templist = mutableMapOf<String, Boolean>()

            if (!message.isEmpty()) {
                val msgKey = MainActivity.dbRef.child("chatrooms").push().key
                val msg = Chat(
                    messageUser?.userId.toString(),
                    messageUser?.firstName.toString(),
                    messageUser?.lastName.toString(),
                    messageUser?.imageUrl.toString(),
                    message,
                    0,
                    timestamp,
                    msgKey.toString(),
                    templist
                )
                MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("chatList")
                    .child(msgKey.toString()).setValue(msg)
                binding.inputMessage.setText("").toString()
                updateAdapter()
            }
        }

        binding.chatroomShareLocationButton.setOnClickListener {

            if (locationPermissionGranted && lastKnownLocation != null) {
                if (!locationIsShared) {
                    MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                        .child("shareLocations")
                        .child(MainActivity.auth.currentUser?.uid.toString())
                        .setValue(
                            PickedPlace(
                                "",
                                lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude,
                                "${messageUser?.firstName.toString()} ${messageUser?.lastName.toString()}"
                            )
                        )
                    locationIsShared = true
                } else {
                    MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                        .child("shareLocations")
                        .child(MainActivity.auth.currentUser?.uid.toString()).removeValue()
                    locationIsShared = false
                }
            }
        }

        binding.requestRideButton.setOnClickListener {
            val bundle2 = Bundle()
            bundle2.putString("chatroomId", chatRoomId.toString())
            //This is for rider view (testing)
            view.findNavController().navigate(R.id.action_chatroom_to_nav_request_ride, bundle2)
        }

//        binding.talkToText.setOnClickListener {
//            //THIS IS WHERE WE WOULD SETUP CODE TO RECORD USER SPEECH AND CONVERT TO TEXT.
//            Log.d(
//                "New Feature",
//                "Chatroom.kt file, where Speech to Text translation/creation feature is to be added"
//            )
//        }

        binding.talkToText.setOnClickListener {
            getAudioPermission()
            if (audioPermissionGranted) {
                //THIS IS WHERE WE WOULD SETUP CODE TO RECORD USER SPEECH AND CONVERT TO TEXT.
                Log.d(
                    "New Feature",
                    "Chatroom.kt file, where Speech to Text translation/creation feature is to be added"
                )

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
                } catch (e: ActivityNotFoundException) {
                    // Handling error when the service is not available.
                    e.printStackTrace()
                    Toast.makeText(context, "Your device does not support STT.", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }


        // this would be deleted. Just for testing layouts at the moment -------------------------------------------------------------------
        binding.requestRideButton.setOnLongClickListener {
            val bundle3 = Bundle()
            bundle3.putString("chatroomId", chatRoomId.toString())
            //This is for driver view (testing)
            view.findNavController().navigate(R.id.action_chatroom_to_nav_potential_rider, bundle3)
            true
        }
        //----------------------------------------------------------------------------------------------------------------------------------
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        speechRecognizer!!.destroy()
        Log.d("Active Status", "Destroy: User is no longer active")
        Log.d("IDs", "${chatRoomId} ${messageUser?.userId} $messageUserId")
        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("listActiveUsers")
            .child(MainActivity.auth.currentUser?.uid.toString()).removeValue()
    }

    fun initializeList() {

        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("chatList")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    listchats.clear()
                    for (postSnapshot in dataSnapshot.children) {
                        var value = postSnapshot.getValue<Chat>()
                        if (value != null) {
                            listchats.add(value)
                        }
                    }

                    updateAdapter()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("demo", "cancel")
                }
            })
    }

    fun updateAdapter() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = ChatAdapter(listchats, context)
        }
    }

    fun getActiveUsers() {
        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("listActiveUsers")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    listActiveUsers.clear()
                    listActiveUsersNames.clear()
                    listActiveUserImageURLs.clear()

                    for (postSnapshot in dataSnapshot.children) {
                        var u: User? = postSnapshot.getValue<User>()
                        if (u != null) {
//                        Log.d("Data change id", value.userId.toString())
//                        //Log.d("Data change id", value.toString())
//                        var fullName = "${value.firstName} ${value.lastName}"
//                        listActiveUsersNames.add(fullName)
                            listActiveUsers.add(u.userId)
                            listActiveUsersNames.add(u.firstName)
                            listActiveUserImageURLs.add(u.imageUrl)
                        }
                    }
                    updateActiveUsers()
                    //for(ac in activeUsers){
                    //    Log.d("check active user " , "${ac.firstName}")
                    //}

//                var activeUsersText = "${listActiveUsers.size} Active User(s)"
//                for(user in listActiveUsers){
//                    Log.d("Active Users", user)
//                }
//                for(user in listActiveUsersNames){
//                    Log.d("Active Users", user)
//                }
//                binding.chatroomActiveUsers.setText(activeUsersText)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("demoo", "cancel")
                }
            })
    }

    fun updateActiveUsers() {
        binding.activeUserRcylerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.activeUserRcylerView.adapter =
            ActiveUserAdapter(listActiveUsersNames, listActiveUserImageURLs)
    }

    fun setRideRequestListener(view: View) {
        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
            .child("rideRequests").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d("Ride Request", "Ride Request Updated")

                    listRideRequests.clear()
                    listRideRequestNames.clear()
                    listRideRequestsObjects.clear()

                    for (postSnapshot in dataSnapshot.children) {
                        var rr: RideRequest? = postSnapshot.getValue<RideRequest>()
                        if (rr != null) {
                            listRideRequests.add(rr.requestId)
                            var name = rr.riderInfo.firstName.plus(" ").plus(rr.riderInfo.lastName)
                                .plus(": ").plus(rr.pickupLocation.name).plus(" to ")
                                .plus(rr.dropoffLocation.name)
                            listRideRequestNames.add(name)
                            listRideRequestsObjects.add(rr)

                        }
                    }

                    var active = false

                    for (id in listActiveUsers) {
                        if (id == messageUserId) {
                            active = true
                        }
                    }

                    if (active && listRideRequests.size > 0) {
                        showNotificationDialog(view)
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("demoo", "cancel")
                }
            })
    }

    fun showNotificationDialog(view: View) {
        if (context != null) {
            var builder = AlertDialog.Builder(context)
            builder.setTitle("Ride Requests")

            builder.setNegativeButton("Close", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })

            builder.setCancelable(false)

            var requestNames = listRideRequestNames.toTypedArray()

            builder.setItems(
                requestNames,
                DialogInterface.OnClickListener() { dialogInterface: DialogInterface, i: Int ->
                    //val bundle = bundleOf("userData" to listRideRequests[i])
                    val bundle =
                        bundleOf("chatroomId" to chatRoomId, "requestId" to listRideRequests[i])
                    view.findNavController()
                        .navigate(R.id.action_chatroom_to_nav_potential_rider, bundle)
                })

            var dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    fun setShareLocationListener(view: View) {
        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("shareLocations")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d("Ride Request", "Ride Request Updated")

                    listSharedLocations.clear()
                    listSharedLocationUserNames.clear()

                    for (postSnapshot in dataSnapshot.children) {
                        var sl: PickedPlace? = postSnapshot.getValue<PickedPlace>()
                        if (sl != null) {
                            var location = LatLng(sl.latitude, sl.longitude)
                            listSharedLocations.add(location)
                            listSharedLocationUserNames.add(sl.name)
                        }
                    }

                    var active = false

                    for (id in listActiveUsers) {
                        if (id == messageUserId) {
                            active = true
                        }
                    }

                    if (active) {
                        showLocationNotificationDialog(view)
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("demoo", "cancel")
                }
            })
    }

    fun showLocationNotificationDialog(view: View) {
        if (context != null) {
            var builder = AlertDialog.Builder(context)
            builder.setTitle("Shared Locations")

            builder.setNegativeButton("Close", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })

            builder.setCancelable(false)

            var sharedLocationUserNames = listSharedLocationUserNames.toTypedArray()

            builder.setItems(
                sharedLocationUserNames,
                DialogInterface.OnClickListener() { dialogInterface: DialogInterface, i: Int ->
                    //val bundle = bundleOf("userData" to listRideRequests[i])
                    val bundle = bundleOf(
                        "chatroomId" to chatRoomId,
                        "sharedLocationUserName" to listSharedLocationUserNames[i],
                        "sharedLocationLat" to listSharedLocations[i].latitude,
                        "sharedLocationLng" to listSharedLocations[i].longitude
                    )
                    view.findNavController()
                        .navigate(R.id.action_chatroom_to_nav_shared_location, bundle)
                })

            var dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

//    private fun checkPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            ActivityCompat.requestPermissions(
//                context as Activity,
//                arrayOf(Manifest.permission.RECORD_AUDIO),
//               RECORD_AUDIO_REQUEST_CODE
//            )
//        }
//    }

    private fun getLocationPermission() {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                context as Activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

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
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                locationPermissionGranted = false
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                }
            }


//            RECORD_AUDIO_REQUEST_CODE -> {
//                if (grantResults.isNotEmpty() &&
//                    grantResults[0] == PackageManager.PERMISSION_GRANTED
//                ) {
//                    Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()

            PERMISSIONS_REQUEST_RECORD_AUDIO -> {
                audioPermissionGranted = false
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    audioPermissionGranted = true

                }
            }
        }
    }

    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        Log.d(
                            "driver location",
                            lastKnownLocation?.latitude.toString() + " " + lastKnownLocation?.longitude.toString()
                        )
                    } else {
                        Log.d("demo", "Current location is null. Using defaults.")
                        Log.e("demo", "Exception: %s", task.exception)
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
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
                        binding.inputMessage.setText(recognizedText)
                        Log.d("Call the translation API here, with desired { to:, from:, message: }",recognizedText)
                    }
                }
            }
        }
    }

    companion object {
        private const val RECORD_AUDIO_REQUEST_CODE = 10
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 2
        private const val SPEECH_TO_TEXT_REQUEST = 3
    }
}

