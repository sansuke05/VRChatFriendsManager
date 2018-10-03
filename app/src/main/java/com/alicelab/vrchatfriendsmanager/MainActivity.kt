package com.alicelab.vrchatfriendsmanager

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragment = LoadingFragment()
        val transaction = getFragmentManager().beginTransaction()
        transaction.add(R.id.container, fragment)
        transaction.commit()
    }
}
