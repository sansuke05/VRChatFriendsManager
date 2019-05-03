package com.alicelab.vrchatfriendsmanager.main

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.support.annotation.CheckResult
import com.alicelab.vrchatfriendsmanager.R
import com.alicelab.vrchatfriendsmanager.views.FriendListAdapter
import kotlinx.android.synthetic.main.fragment_friend_list.*

/**
 * Created by user on 2018/10/03.
 */

class FriendListFragment : Fragment() {

    //定数
    private val KEY_ITEM = "key_item_list"

    var mItemList = arrayOf<HashMap<String, String>>()

    lateinit var mListView: ListView
    @get:JvmName("getContext_") private var mContext: Context? = null


    @CheckResult
    fun createInstance(items: List<HashMap<String, String>>): FriendListFragment {
        val fragment = FriendListFragment()
        val args = Bundle()

        val itemsTmp = items.toTypedArray()
        args.putSerializable(KEY_ITEM, itemsTmp)
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
            @Suppress("UNCHECKED_CAST")
            mItemList = arguments.getSerializable(KEY_ITEM) as Array<HashMap<String, String>>
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
            val adapter = FriendListAdapter(mContext?.applicationContext!!)

            if (mItemList != null) {
                for (item in mItemList) {
                    adapter.add(item)
                }
            }

            val padding = (resources.displayMetrics.density * 8).toInt()
            mListView.setPadding(padding, 0, padding, 0)
            mListView.scrollBarStyle = ListView.SCROLLBARS_OUTSIDE_OVERLAY
            mListView.divider = null

            val inflater = LayoutInflater.from(mContext?.applicationContext!!)
            val header = inflater.inflate(R.layout.list_header_footer, mListView, false)
            val footer = inflater.inflate(R.layout.list_header_footer, mListView, false)
            mListView.addHeaderView(header, null, false)
            mListView.addFooterView(footer, null, false)
            mListView.adapter = adapter

            //mListView.adapter = ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, mNameList)
        }

        //mListView.setOnItemClickListener {}
    }
}