package com.dazorn.node_chess_android.models;

import com.dazorn.node_chess_android.utilities.ApplicationUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

public class GameEnd {
    private Date _at;
    private String _by;
    private GameEndConstants.Reason _reason;

    public GameEnd(JSONObject end) throws JSONException, ParseException {
        _at = ApplicationUtils.parse(end.getString("at"));
        _by  = end.getString("by");
        _reason = GameEndConstants.Reason.values()[end.getInt("status")];
    }

    public String getBy(){
        return _by;
    }

    public GameEndConstants.Reason getReason() {
        return _reason;
    }

    public static class GameEndConstants {
        public enum Reason {
            CHECK_MATE,
            DRAW,
            OUT_OF_TIME,
            CHEATING
        }
    }
}
