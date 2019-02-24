package com.example.redditapi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.framed.Header;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String AUTH_URL =
            "https://www.reddit.com/api/v1/authorize.compact?client_id=%s" +
                    "&response_type=code&state=%s&redirect_uri=%s&" +
                    "duration=permanent&scope=identity";

    private static final String CLIENT_ID = "5q0gWpz511D3Fg";
    private static final String CLIENT_SECRET = "tGmP7TRN1RYn_lbXox-YbSYsOp0";

    private static final String REDIRECT_URI =
            "http://www.example.com/my_redirect";

    private static final String STATE = "MY_RANDOM_STRING_1";

    private static final String ACCESS_TOKEN_URL =
            "https://www.reddit.com/api/v1/access_token";

    private static final String FEED_URL =
            "https://oauth.reddit.com/api/subreddits/search?q=puppies&limit=5&sort=relevance";

    String authString;
    String encodedAuthString;
    String code;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startSignIn(View view) {
        String url = String.format(AUTH_URL, CLIENT_ID, STATE, REDIRECT_URI);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent() != null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            Uri uri = getIntent().getData();
            if (uri.getQueryParameter("error") != null) {
                String error = uri.getQueryParameter("error");
                Log.e(TAG, "An error has occurred : " + error);
            } else {
                String state = uri.getQueryParameter("state");
                if (state.equals(STATE)) {
                    code = uri.getQueryParameter("code");
                    Log.d(TAG, "onResume: "+code);
                    getAccessToken(code);
                }
            }
        }
    }

    private void getAccessToken(String code) {
        OkHttpClient client = new OkHttpClient();
        authString = CLIENT_ID + ":"+CLIENT_SECRET;
        encodedAuthString = Base64.encodeToString(authString.getBytes(),
                Base64.NO_WRAP);

        Request request = new Request.Builder()
                .addHeader("User-Agent", "Sample App")
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(ACCESS_TOKEN_URL)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                        "grant_type=authorization_code&code=" + code +
                                "&redirect_uri=" + REDIRECT_URI))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "ERROR: " + e);
            }


            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                JSONObject data = null;
                try {
                    data = new JSONObject(json);
                    String accessToken = data.optString("access_token");
                    String refreshToken = data.optString("refresh_token");

                    Log.d(TAG, "Access Token = " + data.optString("access_token"));
                    Log.d(TAG, "Refresh Token = " + refreshToken);
                    getProfile(accessToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void getProfile(String accessToken) {
        String token= accessToken;

       OkHttpClient clients = new OkHttpClient();

        //String authString = CLIENT_ID + ":"+CLIENT_SECRET;
        String encodedAuthString = Base64.encodeToString(token.getBytes(),
                Base64.NO_WRAP);

        Request request = new Request.Builder()
                .addHeader("User-Agent", "Sample App")
                .addHeader("Authorization", "bearer " + token)
                .url(FEED_URL)
                .get()
                //.post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                 //       "id " + "t3_anegd0" ))
                .build();

        clients.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "ERROR: " + e);
            }


            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                Log.d(TAG, "get profile onResponse: "+json);
                /*JSONObject data = null;
                try {
                    data = new JSONObject(json);
                    String accessToken = data.optString("access_token");
                    String refreshToken = data.optString("refresh_token");

                    Log.d(TAG, "get profile Access Token = " + data.optString("access_token"));
                    Log.d(TAG, "get profile Refresh Token = " + refreshToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
            }
        });

    }

}