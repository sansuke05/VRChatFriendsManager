package com.alicelab.vrchatfriendsmanager.fragments

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.support.annotation.CheckResult
import com.alicelab.vrchatfriendsmanager.activities.MainActivity
import com.alicelab.vrchatfriendsmanager.R
import kotlinx.android.synthetic.main.fragment_friend_list.*

/**
 * Created by user on 2018/10/03.
 */

class FriendListFragment : Fragment() {

    //定数
    private val KEY_NAME = "key_name_list"

    var mNameList = mutableListOf<String>()

    lateinit var mListView: ListView
    @get:JvmName("getContext_") private var mContext: Context? = null


    @CheckResult
    fun createInstance(name_list: MutableList<String>): FriendListFragment {
        val fragment = FriendListFragment()
        val args = Bundle()

        args.putStringArrayList(KEY_NAME, ArrayList<String>(name_list))
        fragment.arguments = args

        return fragment
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is MainActivity){
            this.mContext = context
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null){
            mNameList = arguments.getStringArrayList(KEY_NAME)
        }
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater!!.inflate(R.layout.fragment_friend_list, container, false)
    }


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null){
            mListView = friendList
        }

        if (mContext != null){
            mListView.adapter = ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, mNameList)
        }

        //mListView.setOnItemClickListener {}
    }
}