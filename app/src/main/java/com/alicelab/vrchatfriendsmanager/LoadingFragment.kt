package com.alicelab.vrchatfriendsmanager

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import kotlinx.android.synthetic.main.fragment_loading.*

/**
 * Created by user on 2018/10/03.
 */

class LoadingFragment : Fragment() {

    private lateinit var loadingView: ImageView

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater!!.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingView = loadingGifView
        val target = GlideDrawableImageViewTarget(loadingView)
        Glide.with(this).load(R.raw.icon_loader).into(target)
    }
}