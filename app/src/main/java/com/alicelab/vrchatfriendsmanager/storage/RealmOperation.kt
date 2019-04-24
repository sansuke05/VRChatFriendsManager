package com.alicelab.vrchatfriendsmanager.storage

import android.content.Context
import com.alicelab.vrchatfriendsmanager.entities.Account
import com.alicelab.vrchatfriendsmanager.entities.AuthInfo
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import java.util.*

/**
 * Created by user on 2018/10/19.
 */
class RealmOperation(val context: Context) {

    lateinit var mRealm: Realm


    fun init() {
        Realm.init(context)

        // スキーマが変更された場合にDBの再構成を行う
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)
    }


    fun close() {
        mRealm.close()
    }


    fun createAccount(userName: String, password: String) {
        mRealm.executeTransaction {
            val account = mRealm.createObject(Account::class.java, UUID.randomUUID().toString())
            account.userName = userName
            account.password = password
            it.copyToRealm(account)
        }
    }


    fun createAuthInfo(apiKey: String, authToken: String) {
        mRealm.executeTransaction {
            val authInfo = mRealm.createObject(AuthInfo::class.java, UUID.randomUUID().toString())
            authInfo.apiKey = apiKey
            authInfo.authToken = authToken
            it.copyToRealm(authInfo)
        }
    }


    fun readAccount() : RealmResults<Account> {
        return mRealm.where(Account::class.java).findAll()
    }


    fun readAuthInfo() : RealmResults<AuthInfo> {
        return mRealm.where(AuthInfo::class.java).findAll()
    }


    fun updateAccount(id: String, password: String) {
        mRealm.executeTransaction {
            val account = mRealm.where(Account::class.java).equalTo("id",id).findFirst()
            account!!.password = password
        }
    }

    fun updateAuthInfo(id: String, authToken: String) {
        mRealm.executeTransaction {
            val account = mRealm.where(AuthInfo::class.java).equalTo("id",id).findFirst()
            account!!.authToken = authToken
        }
    }

    fun deleteAccount(id:String) {
        mRealm.executeTransaction {
            val account = mRealm.where(Account::class.java).equalTo("id",id).findAll()
            account.deleteFromRealm(0)
        }
    }
}