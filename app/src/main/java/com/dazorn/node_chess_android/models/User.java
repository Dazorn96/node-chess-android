package com.dazorn.node_chess_android.models;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.dazorn.node_chess_android.activities.LauncherActivity;
import com.dazorn.node_chess_android.helpers.APIHelper;
import com.dazorn.node_chess_android.helpers.SocketHelper;
import com.dazorn.node_chess_android.utilities.ApplicationUtils;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Random;

import cz.msebera.android.httpclient.Header;

public class User {
    private String _id;
    private String _username;
    private String _tag;
    private int _points;
    private int _recordPoints;
    private String _socketId;
    private double _gameRoom;

    private static User _instance;

    public static User getInstance() {
        if (_instance == null) {
            _instance = new User();
        }

        return _instance;
    }

    private User() {

    }

    public String getId() {
        return _id;
    }

    public String getUsername(){
        return _username;
    }

    public String getTag(){
        return _tag;
    }

    public void updateUser(final Context context, final String idToken, final LauncherActivity activity) throws UnsupportedEncodingException, JSONException {
        APIHelper.SigninUser(context, idToken, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject data = response.getJSONObject("content");

                    _id = data.getString("_id");
                    _username = data.getString("username");
                    _tag = data.getString("tag");
                    _points = data.getInt("points");
                    _recordPoints = data.getInt("record");

                    SocketHelper.GetInstance(activity);

                    activity.Start();
                }
                catch (JSONException ex) {
                    ex.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                ApplicationUtils.forceRestartApplication(activity);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                ApplicationUtils.forceRestartApplication(activity);
            }
        });
    }
}
