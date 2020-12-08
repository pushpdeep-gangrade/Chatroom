package com.example.chatroom.ui.ui.chatroom

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.chatroom.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.squareup.picasso.Picasso
import cz.msebera.android.httpclient.Header
import org.json.JSONArray
import org.json.JSONObject


class ChatViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.chat_item, parent, false)) {
    private var mUserImage : ImageView? = null
    private var mLikeImage : ImageView? = null
    private var mDelImage : ImageView? = null
    private var mTvUser: TextView? = null
    private var mTvLikes: TextView? = null
    private var mTvMsg: TextView? = null
    private var mTvtime: TextView? = null
    private var mTranslateButton: TextView? = null

    init {
        mUserImage = itemView.findViewById(R.id.user_image_chat)
       mLikeImage = itemView.findViewById(R.id.iv_like_msg)
       mDelImage  = itemView.findViewById(R.id.iv_delete_chat)
       mTvUser= itemView.findViewById(R.id.username_chat)
       mTvLikes  = itemView.findViewById(R.id.no_likes)
       mTvMsg = itemView.findViewById(R.id.message_chat)
        mTvtime = itemView.findViewById(R.id.time_chat)
        mTranslateButton = itemView.findViewById(R.id.iv_translate_button)

    }
    fun bind(chat: Chat, context: Context) {
        mTvUser?.text = chat.userfname.plus(" ").plus(chat.userlname)
        mTvLikes?.text = chat.likesMap.size.toString()
        mTvMsg?.text = chat.message
        mTvtime?.text = chat.timedate
        Picasso.get().load(chat.userphotourl).resize(250, 250).into(mUserImage)

        if(!FirebaseAuth.getInstance().currentUser?.uid.equals(chat.userId))
            mDelImage!!.visibility = View.INVISIBLE

        if(chat.likesMap.containsKey(FirebaseAuth.getInstance().currentUser?.uid))
            mLikeImage?.setImageResource(R.drawable.heart_icon)

        mDelImage?.setOnClickListener(){
         var ref = FirebaseDatabase.getInstance().reference.child("chatrooms").child(chatRoomId.toString()).child("chatList").child(chat.messageId)
        ref.removeValue()
        }

        mLikeImage?.setOnClickListener() {
             var ref = FirebaseDatabase.getInstance().reference.child("chatrooms").child(chatRoomId.toString()).child("chatList").child(chat.messageId)
            onLiked(ref)

        }

        mTranslateButton?.setOnClickListener() {
            setTranslateMessageDialog(context)
            Log.d("New Feature","ChatViewHolder.kt file, where Text to Talk and Text to Text translations features are to be added")
        }
    }

    private fun setTranslateMessageDialog(context: Context) {
        val arrStringName: ArrayList<String> = ArrayList()
        val arrLanguageObjects: ArrayList<Language> = ArrayList()

        val builder = AlertDialog.Builder(context);
        val view: View = View.inflate(context, R.layout.translate_message_dialog, null)

        val cancel: TextView = view.findViewById<TextView>(R.id.translateMessageDialog_cancelButton)
        val submit: TextView = view.findViewById<TextView>(R.id.translateMessageDialog_submitButton)
        val fromLanguageSpinner: Spinner = view.findViewById<Spinner>(R.id.translateMessageDialog_fromLanguageSpinner)
        val toLanguageSpinner: Spinner = view.findViewById<Spinner>(R.id.translateMessageDialog_toLanguageSpinner)
        val radioGroup: RadioGroup = view.findViewById<RadioGroup>(R.id.translateMessageDialog_radioGroup)
        val progressBar: ProgressBar = view.findViewById<ProgressBar>(R.id.translateMessageDialog_progressBar)

        var selectedRadioButton: String = ""
        var fromSpinnerLanguageSelected: String = ""
        var toSpinnerLanguageSelected: String = ""

        progressBar.visibility = View.VISIBLE
        submit.isEnabled = false

        radioGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            val radio: RadioButton = view.findViewById(checkedId)
            selectedRadioButton = radio.text.toString()
            Toast.makeText(context," On checked change : ${radio.text}",
                Toast.LENGTH_SHORT).show()
        })

        builder.setView(view);
        val dialog: AlertDialog = builder.create()

        cancel.setOnClickListener {
            Log.d("Cancel", "Hit cancel")

            dialog.cancel()

        }

        submit.setOnClickListener {
            Log.d("Submit", "Hit submit")

            //Logic for translating text goes here
            Log.d("Selected", "Method: " + selectedRadioButton
                    + "\nFrom Spinner Language: " + fromSpinnerLanguageSelected
                    + "\nTo Spinner Language: " + toSpinnerLanguageSelected)

            if(selectedRadioButton.equals("")){
                Toast.makeText(context,"Please select a translation method",
                    Toast.LENGTH_SHORT).show()
            }
            else if(selectedRadioButton.equals("Text to Text")){
                //Grab message text and selected translation method and make API call

                //This is how you will get the proper value for the selected languages
                Log.d("From Language",(arrLanguageObjects.filter { it.name == fromSpinnerLanguageSelected })[0].key)
                Log.d("To Language",(arrLanguageObjects.filter { it.name == toSpinnerLanguageSelected })[0].key)
                Log.d("Message to Translate", mTvMsg?.text.toString())

                val from: String = (arrLanguageObjects.filter { it.name == fromSpinnerLanguageSelected })[0].key
                val to: String = (arrLanguageObjects.filter { it.name == toSpinnerLanguageSelected })[0].key

                textToTextTranslation(from, to, mTvMsg, context, dialog)

                //This is the body that needs to be sent to the api
                //URL if you run nodemon ./server.js: http://localhost:8080/translate/textToText
                /*
                {
                    "from":"en",
                    "to":["it"],
                    "message":"This is the message to translate"
                }
                */
                //The response will look like this
                /*
                {
                    "translations": [
                        {
                            "text": "Questo è il messaggio da tradurre",
                            "to": "it"
                        }
                    ]
                }
                */
            }
            else if(selectedRadioButton.equals("Text to Talk")){
                //Grab message text and selected translation method and make API call
            }
        }

        dialog.show()


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

                    val adapter = ArrayAdapter(context,
                        android.R.layout.simple_spinner_item, arrStringName)
                    fromLanguageSpinner.adapter = adapter
                    toLanguageSpinner.adapter = adapter

                    toSpinnerLanguageSelected = arrStringName.get(0)
                    fromSpinnerLanguageSelected = arrStringName.get(0)

                    fromLanguageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            fromSpinnerLanguageSelected = arrStringName.get(position)
                        }

                    }

                    toLanguageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {
                        }

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            toSpinnerLanguageSelected = arrStringName.get(position)
                        }

                    }

                    progressBar.visibility = View.INVISIBLE
                    submit.isEnabled = true


                }else{

                    Toast.makeText(context,"Failed to get languages",
                        Toast.LENGTH_SHORT).show()

                    progressBar.visibility = View.INVISIBLE

                }
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, e: Throwable, response: JSONArray?)
            {
                Toast.makeText(context,"Failed to get languages",
                    Toast.LENGTH_SHORT).show()

                progressBar.visibility = View.INVISIBLE

            }
        })
    }

    private fun textToTextTranslation(from: String, to: String, mTvMsg: TextView?, context: Context, dialog: AlertDialog){
        val msg: String = mTvMsg?.text.toString()

        val url =
            "http://10.0.2.2:8080/translate/textToText"

        val client: AsyncHttpClient = AsyncHttpClient()
        val params = RequestParams()

        params.put("from", from)
        params.put("to", to)
        params.put("message", msg)

        client.post(url, params, object : JsonHttpResponseHandler()
        {
            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?)
            {
                if(response != null){

                    Log.d("Response", response.toString())

                    val translationsArray: JSONArray = response.getJSONArray("translations")
                    val translatedMsg = (translationsArray.get(0) as JSONObject).getString("text")

                    if (mTvMsg != null) {
                        mTvMsg.text = translatedMsg
                    }

                    dialog.cancel()

                    Toast.makeText(context,"Text to text translation successful",
                        Toast.LENGTH_SHORT).show()

                }else{

                    Toast.makeText(context,"Text to text translation failed",
                        Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, e: Throwable, response: JSONArray?)
            {
                Toast.makeText(context,"Text to text translation failed",
                    Toast.LENGTH_SHORT).show()
            }
        })

    }


    private fun onLiked(postRef: DatabaseReference) {
        postRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val p = currentData.getValue(Chat::class.java)
                    ?: return Transaction.success(currentData)
                var id = FirebaseAuth.getInstance().currentUser?.uid.toString()
                if (p.likesMap.containsKey(FirebaseAuth.getInstance().currentUser?.uid)) {
                   p.likesMap.remove(id)
                    p.likes = p.likesMap.size
                    mLikeImage!!.setImageResource(R.drawable.heart_icon_empty)
                } else {
                p.likesMap[id] = true
                    p.likes = p.likesMap.size
                    mLikeImage!!.setImageResource(R.drawable.heart_icon)
                }
                currentData.value = p
                return Transaction.success(currentData)
            }


            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
            }


        })
    }


}