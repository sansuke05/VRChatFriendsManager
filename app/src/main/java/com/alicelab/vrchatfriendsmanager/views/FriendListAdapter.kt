package com.alicelab.vrchatfriendsmanager.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.alicelab.vrchatfriendsmanager.R
import com.bumptech.glide.Glide

/**
 * Created by user on 2018/10/20.
 */
class FriendListAdapter(context: Context) : ArrayAdapter<HashMap<String, String>>(context, 0) {

    var mInflater: LayoutInflater


    init {
        mInflater = LayoutInflater.from(context)
    }


    override fun getView(position: Int, _convertView: View?, parent: ViewGroup?): View {
        var convertView = _convertView

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.adapter_list_friend_card, parent, false)
        }

        val item: HashMap<String, String> = getItem(position)

        val friendNameView = convertView?.findViewById<TextView>(R.id.friendName)
        friendNameView?.text = item["displayName"]

        val thumbnailImageView = convertView?.findViewById<ImageView>(R.id.thumbnail)
        val thumbnailURL = item["currentAvatarThumbnailImageUrl"]
        Glide.with(context).load(thumbnailURL).into(thumbnailImageView)

        return convertView!!
    }
}