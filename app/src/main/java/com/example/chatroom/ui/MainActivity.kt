package com.example.chatroom.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Global.putInt
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.chatroom.R
import com.example.chatroom.R.id.topAppBar
import com.example.chatroom.data.model.User
import com.example.chatroom.ui.login.LoginActivity
import com.example.chatroom.ui.ui.chatroom.ChatroomFragment
import com.example.chatroom.ui.ui.profile.ProfileFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.serialization.json.Json.Default.context

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var userImage: ImageView
    private lateinit var userFullname: TextView
    private lateinit var userEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar : MaterialToolbar = findViewById(topAppBar)
          setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

//        drawerLayout.nav_header_user_name.text = globaluser.firstName.plus(" ").plus(globaluser.lastName)
//        drawerLayout.nav_header_nav_user_email.text = globaluser.email
//        Picasso.get().load(globaluser.imageUrl).into(drawerLayout.nav_header_user_image);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_profile, R.id.nav_create_chatroom, R.id.nav_chatrooms, R.id.nav_users
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        userImage = navView.getHeaderView(0).findViewById<ImageView>(R.id.nav_header_user_image)
        userFullname = navView.getHeaderView(0).findViewById<TextView>(R.id.nav_header_user_name)
        userEmail = navView.getHeaderView(0).findViewById<TextView>(R.id.nav_header_nav_user_email)

        setUserInfo()
    }

    private fun setUserInfo(){
        globalid?.let {
            dbRef.child("users").child(globalid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val globaluser = dataSnapshot.getValue<com.example.chatroom.data.model.User>()!!
                    if (globaluser != null) {
                        userFullname.text =  globaluser.firstName + " " + globaluser.lastName
                        userEmail.text =  globaluser.email
                        Picasso.get().load(globaluser.imageUrl).into(userImage);
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w("demo", "Failed to read value.", error.toException())
                }
            })
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.logout -> {
                auth.signOut()
                val intent = Intent(baseContext, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    companion object{
        var db = FirebaseDatabase.getInstance()
        var dbRef = db.reference
        var auth = FirebaseAuth.getInstance()
        val globalid = auth.currentUser?.uid
    }
}