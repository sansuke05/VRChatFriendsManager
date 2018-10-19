package com.alicelab.vrchatfriendsmanager.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

/**
 * Created by user on 2018/10/19.
 */
open class Account(
        @PrimaryKey open var id: String = UUID.randomUUID().toString(),
        @Required open var userName: String = "",
        @Required open var password: String = ""
) : RealmObject() {}