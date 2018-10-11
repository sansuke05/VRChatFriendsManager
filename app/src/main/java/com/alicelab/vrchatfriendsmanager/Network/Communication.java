package com.alicelab.vrchatfriendsmanager.Network;


import android.content.Context;
import android.util.Log;

import com.alicelab.vrchatfriendsmanager.Activity.MainActivity;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by user on 2018/10/04.
 */

public class Communication {

    private Context mContext;


    public Communication(Context context){
        if (context instanceof MainActivity) {
            mContext = context;
        }
    }


    public void start() {
        Single.<List<String>>create(emitter -> emitter.onSuccess(getOnlineFriendList()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> {
                    MainActivity activity = (MainActivity)mContext;
                    activity.setStrItems(items);
                    activity.changeFragment();
                });
    }


    private List<String> getOnlineFriendList() {
        Log.d("debug", "io thread");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        Log.d("debug", "process finished");

        return Arrays.asList("taro", "jiro", "saburo", "KANI", "草草の草", "( ˘ω˘ )");
    }
}
