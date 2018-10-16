package com.alicelab.vrchatfriendsmanager.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.alicelab.vrchatfriendsmanager.Network.Communication
import com.alicelab.vrchatfriendsmanager.Fragment.FriendListFragment
import com.alicelab.vrchatfriendsmanager.Fragment.LoadingFragment
import com.alicelab.vrchatfriendsmanager.R
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity


class MainActivity : AppCompatActivity(), LoadingFragment.FragmentListener {

    val REQUEST_CODE = 1;
    val PREF_NAME = "LAUNCH_STATE"
    val PREF_VALUE = "LAUNCHED"

    var strItems = mutableListOf<String>()


    fun changeFragment(){
        Log.d("debug", "change to friend list fragment")

        val fragment = FriendListFragment()
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment.createInstance(strItems))
        transaction.commit()
    }


    override fun communicateAndChangeFragment() {
        Communication(this).start()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初回起動時にログイン画面からスタートする
        val preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (!preferences.getBoolean(PREF_VALUE, false)){
            val intent = Intent(this, LoginActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)
            return
        }

        val fragment = LoadingFragment()
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.container, fragment)
        transaction.commit()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK){
                    val fragment = LoadingFragment()
                    val transaction = fragmentManager.beginTransaction()
                    transaction.add(R.id.container, fragment)
                    transaction.commit()
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.menuLicence) {
            val intent = Intent(this, OssLicensesMenuActivity::class.java)
            OssLicensesMenuActivity.setActivityTitle("Licences")
            startActivity(intent)
        } else {
            Log.d("debug", "selected else")
        }
        return super.onOptionsItemSelected(item)
    }
}
