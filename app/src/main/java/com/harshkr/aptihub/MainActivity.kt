package com.harshkr.aptihub

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            val loginFragment = LoginFragment()
            loginFragment.show(supportFragmentManager, "LoginFragment")
        } else {
            startActivity(Intent(this, TopicSelectionActivity::class.java))
            finish()
        }
    }
}