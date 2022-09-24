package com.udacity.project4.authentication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.Constants.SIGN_IN_CODE
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    lateinit var sharedPreference: SharedPreferences
    lateinit var editor : SharedPreferences.Editor
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        sharedPreference = getSharedPreferences(getString(R.string.shared), Context.MODE_PRIVATE)
        editor = sharedPreference.edit()

//          TODO: If the user was authenticated, send him to RemindersActivity

        var haslogged = sharedPreference.getBoolean("hasLogged",false)
        if (haslogged){
        startActivity(Intent(this, RemindersActivity::class.java))
        finish()}
//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google



    }

    fun LOGIN_CLICKED(view: View) {

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        //starting sign in and login flow
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
                .build(), SIGN_IN_CODE
        )
    }

    //login flow result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                //saving login state
                editor.putBoolean("hasLogged",true)
                editor.commit()
                //navigate to the app
                startActivity(Intent(this, RemindersActivity::class.java))
                finish()
            }
        }
    }
}
