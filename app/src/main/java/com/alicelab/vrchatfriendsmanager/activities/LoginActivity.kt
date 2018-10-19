package com.alicelab.vrchatfriendsmanager.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.alicelab.vrchatfriendsmanager.R
import kotlinx.android.synthetic.main.activity_login.*

/**
 * Created by user on 2018/10/16.
 */
class LoginActivity : Activity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // MainActivityへの遷移
        loginButton.setOnClickListener({
            val userName = userName.text.toString()
            val password = password.text.toString()

            Log.d("debug", "user name: $userName")
            Log.d("debug", "password: $password")

            val intent = Intent()
            val bundle = Bundle()

            bundle.putString("USER_NAME", userName)
            bundle.putString("PASSWORD", password)
            intent.putExtras(bundle)
            setResult(RESULT_OK, intent)

            finish()
        })
    }
}