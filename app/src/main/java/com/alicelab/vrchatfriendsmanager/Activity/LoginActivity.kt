package com.alicelab.vrchatfriendsmanager.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.alicelab.vrchatfriendsmanager.R
import kotlinx.android.synthetic.main.activity_login.*

/**
 * Created by user on 2018/10/16.
 */
class LoginActivity : Activity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val userName = userName.text
        val password = password.text

        // MainActivityへの遷移
        loginButton.setOnClickListener({
            val intent = Intent()
            intent.putExtra("USER_NAME", userName)
            intent.putExtra("PASSWORD", password)
            setResult(RESULT_OK, intent)
            finish()
        })
    }
}