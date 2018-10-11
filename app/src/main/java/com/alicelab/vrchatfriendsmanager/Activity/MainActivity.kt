package com.alicelab.vrchatfriendsmanager.Activity

import android.content.Intent
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

    var strItems = mutableListOf<String>()


    override fun communicateAndChangeFragment() {
        Communication(this).start()

        val fragment = FriendListFragment()
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment.createInstance(strItems))
        transaction.commit()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragment = LoadingFragment()
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.container, fragment)
        transaction.commit()
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
