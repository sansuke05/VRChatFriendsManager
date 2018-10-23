package com.alicelab.vrchatfriendsmanager.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.alicelab.vrchatfriendsmanager.network.Communication
import com.alicelab.vrchatfriendsmanager.fragments.FriendListFragment
import com.alicelab.vrchatfriendsmanager.fragments.LoadingFragment
import com.alicelab.vrchatfriendsmanager.R
import com.alicelab.vrchatfriendsmanager.storage.RealmOperation
import com.alicelab.vrchatfriendsmanager.utilities.Mode
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity


class MainActivity : AppCompatActivity(), LoadingFragment.FragmentListener {

    val REQUEST_CODE = 1;
    val PREF_NAME = "LAUNCH_STATE"
    val PREF_VALUE = "LAUNCHED"

    var items = mutableListOf<HashMap<String, String>>()
    var sortedItems = listOf<HashMap<String, String>>()
    var mode = Mode.LAUNCHED

    lateinit var operation: RealmOperation


    fun sortFriendList() {
        sortedItems = items.sortedWith(compareBy{ it["displayName"] })
    }


    fun changeFragment(){
        Log.d("debug", "change to friend list fragment")

        val fragment = FriendListFragment()
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment.createInstance(sortedItems))
        transaction.commit()
    }


    override fun communicateAndChangeFragment() {
        Communication(this).start()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Realmのセットアップ
        operation = RealmOperation(this)
        operation.init()

        // 初回起動時にログイン画面からスタートする
        val preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (!preferences.getBoolean(PREF_VALUE, false)){
            mode = Mode.FIRST_LAUNCH
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
        val bundle = data!!.extras

        when (requestCode) {
            REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK){
                    val userName = bundle.getString("USER_NAME")
                    val password = bundle.getString("PASSWORD")

                    // DBへアカウント情報の保存
                    operation.createAccount(userName, password)

                    val fragment = LoadingFragment()
                    val transaction = fragmentManager.beginTransaction()
                    transaction.add(R.id.container, fragment)
                    transaction.commit()
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        operation.close()
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
