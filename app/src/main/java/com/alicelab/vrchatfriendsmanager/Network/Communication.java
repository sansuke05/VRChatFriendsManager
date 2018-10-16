package com.alicelab.vrchatfriendsmanager.Network;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.util.Base64;
import android.widget.Toast;

import com.alicelab.vrchatfriendsmanager.Activity.MainActivity;
import com.alicelab.vrchatfriendsmanager.utils.Error;
import com.alicelab.vrchatfriendsmanager.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by user on 2018/10/04.
 */

public class Communication {

    //定数
    private static final String API_BASE = "https://api.vrchat.cloud/api/1";
    private static final String COOKIES_HEADER = "Set-Cookie";

    private Error errorState = Error.NO_ERROR;
    private HttpURLConnection con = null;
    private URL url = null;
    private String mApiKey = "";
    private String mAuthToken = "";
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

        Single.<List<String>>create(emitter -> {
            mAuthToken = getAuthToken();
            emitter.onSuccess(getOnlineFriendList());
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> {
                    if (errorState == Error.UNAUTHORIZED){
                        Toast.makeText(mContext, "承認に失敗しました", Toast.LENGTH_SHORT).show();
                        return;
                    } else if (errorState == Error.COMMUNICATION){
                        Toast.makeText(mContext, "通信に失敗しました", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    MainActivity activity = (MainActivity)mContext;

                    // 初回起動時の場合、次回からMainActivityからの起動に切り替える処理
                    SharedPreferences preferences = activity.getSharedPreferences(activity.getPREF_NAME(), Context.MODE_PRIVATE);
                    preferences.edit()
                            .putBoolean(activity.getPREF_VALUE(), true)
                            .apply();

                    activity.setStrItems(items);
                    activity.changeFragment();
                });
    }


    private List<String> getAPIKey() {
        final String urlStr = API_BASE + "/config";
        String apiKey = "";

        try {
            url = new URL(urlStr);
            con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.connect();

            //responseの取得
            int resp_code = con.getResponseCode();

            switch (resp_code){
                case HttpURLConnection.HTTP_OK:
                    InputStream in = con.getInputStream();
                    String response = readInputStream(in);

                    JSONObject jsonData = new JSONObject(response);
                    apiKey = jsonData.getString("clientApiKey");
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    errorState = Error.UNAUTHORIZED;
                    break;
                default:
                    break;
            }

        } catch (JSONException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        Log.d("debug", "VRChat APIKey: " + apiKey);

        return Arrays.asList(apiKey);
    }


    private String getAuthToken(){
        final String urlStr = API_BASE + "/auth/user";
        final String userPassword = mUserName + ":" + mPassword;
        final String encodeAuthorization = Base64.encodeToString(userPassword.getBytes(), Base64.NO_WRAP);
        String authToken = "";

        try {
            Uri.Builder builder = new Uri.Builder();
            builder.appendQueryParameter("apiKey", mApiKey);
            url = new URL(urlStr + builder.toString());
            con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Basic " + encodeAuthorization);
            con.setDoInput(true);
            con.connect();

            //responseの取得
            int resp_code = con.getResponseCode();

            switch (resp_code) {
                case HttpURLConnection.HTTP_OK:
                    // Response HeaderからCookieを取得
                    Map<String, List<String>> headerFields = con.getHeaderFields();
                    List<String> cookiesHader = headerFields.get(COOKIES_HEADER);

                    if (cookiesHader != null) {
                        for (String cookieHeader : cookiesHader){
                            List<HttpCookie> cookies;
                            cookies = HttpCookie.parse(cookieHeader);

                            if (cookies != null){
                                // CookieからAuthTokenを取得
                                for (HttpCookie cookie : cookies){
                                    Log.d("debug", "cookie: " + cookie.toString());
                                    String[] keyValue = cookie.toString().split("=");
                                    if (keyValue[0].equals("auth")){
                                        authToken = keyValue[1];
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    errorState = Error.UNAUTHORIZED;
                    break;
                default:
                    break;
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }

        return authToken;
    }


    private List<String> getOnlineFriendList() {
        final String urlStr = API_BASE + "/auth/user/friends";
        List<String> onlineFriendList = new ArrayList<>();

        try {
            Uri.Builder builder = new Uri.Builder();
            builder.appendQueryParameter("apiKey", mApiKey);
            builder.appendQueryParameter("authToken", mAuthToken);
            url = new URL(urlStr + builder.toString());
            con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.connect();

            //responseの取得
            int resp_code = con.getResponseCode();

            switch (resp_code){
                case HttpURLConnection.HTTP_OK:
                    InputStream in = con.getInputStream();
                    String response = readInputStream(in);

                    //JSONからフレンド名を取得
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject;
                    String friendName;
                    for (int i = 0; i < jsonArray.length(); i++){
                        jsonObject = jsonArray.getJSONObject(i);
                        friendName = jsonObject.getString("displayName");
                        onlineFriendList.add(friendName);
                    }
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    errorState = Error.UNAUTHORIZED;
                    break;
                default:
                    errorState = Error.COMMUNICATION;
                    break;
            }
        } catch (JSONException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }

        return onlineFriendList;
    }
}
