package com.alicelab.vrchatfriendsmanager.fragments

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.alicelab.vrchatfriendsmanager.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import kotlinx.android.synthetic.main.fragment_loading.*

/**
 * Created by user on 2018/10/03.
 */

class LoadingFragment : Fragment() {

    interface FragmentListener {
        fun communicateAndChangeFragment()
    }

    private lateinit var mListener: FragmentListener
    private lateinit var loadingView: ImageView


    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is FragmentListener) {
            mListener = context
        }
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater!!.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingView = loadingGifView
        val target = GlideDrawableImageViewTarget(loadingView)
        Glide.with(this).load(R.raw.icon_loader).into(target)

        Log.d("debug", "Now Loading")
        mListener.communicateAndChangeFragment()
    }
}