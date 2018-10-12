package com.alicelab.vrchatfriendsmanager.Network;


import android.content.Context;
import android.util.Log;

import com.alicelab.vrchatfriendsmanager.Activity.MainActivity;
import com.alicelab.vrchatfriendsmanager.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by user on 2018/10/04.
 */

public class Communication {

    //定数
    private static final String API_BASE = "https://api.vrchat.cloud/api/1";

    private HttpURLConnection con = null;
    private URL url = null;
    private String mApiKey = "";
    private String mUserName = "";
    private String mPassword = "";

    private Context mContext;


    public Communication(Context context){
        if (context instanceof MainActivity) {
            mContext = context;
        }
    }


    // Utility
    private String readInputStream(InputStream in) throws IOException {
        StringBuffer buffer = new StringBuffer();
        String str = "";

        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        while ((str = reader.readLine()) != null) {
            buffer.append(str);
        }

        try {
            in.close();
        } catch (Exception e){
            e.printStackTrace();
        }

        return buffer.toString();
    }


    private void getAuthInfoFromResource(){
        mApiKey = mContext.getString(R.string.api_key);
        mUserName = mContext.getString(R.string.user_name);
        mPassword = mContext.getString(R.string.password);
    }


    public void start() {
        getAuthInfoFromResource();

        Single.<List<String>>create(emitter ->
                emitter.onSuccess(/*getOnlineFriendList()*/getAPIKey()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> {
                    MainActivity activity = (MainActivity)mContext;
                    activity.setStrItems(items);
                    activity.changeFragment();
                });
    }


    private List<String> getAPIKey() {
        String urlStr = API_BASE + "/config";
        String apiKey = "";

        try {
            url = new URL(urlStr);
            con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.connect();

            //responseの取得
            InputStream in = con.getInputStream();
            String response = readInputStream(in);

            JSONObject jsonData = new JSONObject(response);
            apiKey = jsonData.getString("clientApiKey");
        } catch (JSONException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        Log.d("debug", "VRChat APIKey: " + apiKey);

        return Arrays.asList(apiKey);
    }


    /*
    private List<String> getAuthToken(){

    }
    */

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
