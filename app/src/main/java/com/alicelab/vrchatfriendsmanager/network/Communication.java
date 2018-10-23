package com.alicelab.vrchatfriendsmanager.network;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.util.Base64;
import android.widget.Toast;

import com.alicelab.vrchatfriendsmanager.activities.MainActivity;
import com.alicelab.vrchatfriendsmanager.entities.Account;
import com.alicelab.vrchatfriendsmanager.entities.AuthInfo;
import com.alicelab.vrchatfriendsmanager.utilities.Error;
import com.alicelab.vrchatfriendsmanager.utilities.Mode;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmResults;


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
    private String mId = "";
    private String mApiKey = "";
    private String mAuthToken = "";
    private String mUserName = "";
    private String mPassword = "";

    private Context mContext;


    // Constructor
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


    private boolean hasError(){
        if (errorState == Error.UNAUTHORIZED){
            Toast.makeText(mContext, "承認に失敗しました", Toast.LENGTH_SHORT).show();
            return true;
        } else if (errorState == Error.COMMUNICATION){
            Toast.makeText(mContext, "通信に失敗しました", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }


    private Error getAuthInfoFromDB(MainActivity activity){
        RealmResults<AuthInfo> data = activity.operation.readAuthInfo();

        // データ取得エラーチェック
        if (data.isEmpty()) {
            return Error.NO_DATA;
        }

        AuthInfo authInfo = data.first();
        mId = authInfo.getId();
        mApiKey = authInfo.getApiKey();

        return Error.NO_ERROR;
    }


    private Error getAccountFromDB(MainActivity activity) {
        RealmResults<Account> data = activity.operation.readAccount();

        // データ取得エラーチェック
        if (data.isEmpty()) {
            return Error.NO_DATA;
        }

        Account account = data.first();
        mUserName = account.getUserName();
        mPassword = account.getPassword();

        return Error.NO_ERROR;
    }


    // Main
    public void start() {
        MainActivity activity = (MainActivity)mContext;

        // アカウント情報の取得
        if (getAccountFromDB(activity) == Error.NO_DATA){
            Toast.makeText(activity, "ローカルデータの取得に失敗しました", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("debug", "user name: " + mUserName);
        Log.d("debug", "password: " + mPassword);

        if (activity.getMode() == Mode.FIRST_LAUNCH){
            Single.<List<HashMap<String, String>>>create(emitter -> {
                mApiKey = getAPIKey();
                mAuthToken = getAuthToken();
                emitter.onSuccess(getOnlineFriendList());
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(items -> {
                        if (this.hasError()) return;

                        // DBへ承認データを保存
                        activity.operation.createAuthInfo(mApiKey, mAuthToken);

                        // 初回起動時の場合、次回からMainActivityからの起動に切り替える処理
                        SharedPreferences preferences = activity.getSharedPreferences(activity.getPREF_NAME(), Context.MODE_PRIVATE);
                        preferences.edit()
                                .putBoolean(activity.getPREF_VALUE(), true)
                                .apply();

                        activity.setItems(items);
                        // フレンド名順にソート
                        activity.sortFriendList();

                        activity.changeFragment();
                    });

        } else {
            if (getAuthInfoFromDB(activity) == Error.NO_DATA){
                Toast.makeText(activity, "ローカルデータの取得に失敗しました", Toast.LENGTH_SHORT).show();
                return;
            }

            Single.<List<HashMap<String, String>>>create(emitter -> {
                mAuthToken = getAuthToken();
                emitter.onSuccess(getOnlineFriendList());
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(items -> {
                        if (this.hasError()) return;

                        // DBのデータを更新
                        activity.operation.updateAuthInfo(mId, mAuthToken);

                        activity.setItems(items);
                        // フレンド名順にソート
                        activity.sortFriendList();

                        activity.changeFragment();
                    });
        }
    }


    private String getAPIKey() {
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

        return apiKey;
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


    private List<HashMap<String, String>> getOnlineFriendList() {
        final String urlStr = API_BASE + "/auth/user/friends";
        List<HashMap<String, String>> onlineFriendList = new ArrayList<>();

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

                    //JSONからフレンド名とサムネイルを取得
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject;
                    HashMap<String, String> map;
                    String friendName;
                    String thumbnailUrl;
                    for (int i = 0; i < jsonArray.length(); i++){
                        map = new HashMap<>();

                        jsonObject = jsonArray.getJSONObject(i);
                        friendName = jsonObject.getString("displayName");
                        thumbnailUrl = jsonObject.getString("currentAvatarThumbnailImageUrl");

                        map.put("displayName", friendName);
                        map.put("currentAvatarThumbnailImageUrl", thumbnailUrl);
                        onlineFriendList.add(map);
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
