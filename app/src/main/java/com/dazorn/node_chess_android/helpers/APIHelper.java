package com.dazorn.node_chess_android.helpers;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class APIHelper {
    private static final String _endpoint = "https://node-chess-online.herokuapp.com";//"https://ffcdf165.ngrok.io"

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
