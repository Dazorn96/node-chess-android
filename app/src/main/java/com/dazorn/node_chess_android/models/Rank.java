package com.dazorn.node_chess_android.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Rank {
    private String _username;
    private int _points;

    public String getUsername(){
        return _username;
    }

    public int getPoints(){
        return _points;
    }

    public Rank(JSONObject rank) {
        try {
            _username = rank.getString("username");
            _points = rank.getInt("points");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
