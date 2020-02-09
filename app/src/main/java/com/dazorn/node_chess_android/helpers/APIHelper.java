package com.dazorn.node_chess_android.helpers;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.entity.StringEntity;

public class APIHelper {
    private static final String _endpoint = "https://fcf11980.ngrok.io";//"https://node-chess-online.herokuapp.com";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static final void SigninUser(Context context, String idToken, AsyncHttpResponseHandler handler) {
        client.post(context, _endpoint + "/auth/google/" + idToken, null, "application/json", handler);
    }

    public static final void getGlobalRankings(Context context, AsyncHttpResponseHandler handler){
        client.get(context, _endpoint + "/users/top100", null, "application/json", handler);
    }

    public static final void getProfileStats(Context context, String userTag, AsyncHttpResponseHandler handler) {
        client.get(context, _endpoint + "/user/" + userTag.replace("#", "") + "/games/stats", null, "application/json", handler);
    }
}
