package com.alicelab.vrchatfriendsmanager.Network;


import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Base64;

import com.alicelab.vrchatfriendsmanager.Activity.MainActivity;
import com.alicelab.vrchatfriendsmanager.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieManager;
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
        mAuthToken = mContext.getString(R.string.auth_token);
        mUserName = mContext.getString(R.string.user_name);
        mPassword = mContext.getString(R.string.password);
    }


    public void start() {
        getAuthInfoFromResource();

        Single.<List<String>>create(emitter -> emitter.onSuccess(/*getOnlineFriendList()*/getAuthToken()))
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
            int resp_code = con.getResponseCode();

            switch (resp_code){
                case HttpURLConnection.HTTP_OK:
                    InputStream in = con.getInputStream();
                    String response = readInputStream(in);

                    JSONObject jsonData = new JSONObject(response);
                    apiKey = jsonData.getString("clientApiKey");
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
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


    private List<String> getAuthToken(){
        String urlStr = API_BASE + "/auth/user";
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

        return Arrays.asList(authToken);
    }


    private List<String> getOnlineFriendList() {
        

        return Arrays.asList("taro", "jiro", "saburo", "KANI", "草草の草", "( ˘ω˘ )");
    }
}
