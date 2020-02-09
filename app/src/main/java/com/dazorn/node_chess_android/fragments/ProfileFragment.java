package com.dazorn.node_chess_android.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dazorn.node_chess_android.R;
import com.dazorn.node_chess_android.helpers.APIHelper;
import com.dazorn.node_chess_android.models.Rank;
import com.dazorn.node_chess_android.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ProfileFragment extends Fragment {
    private RelativeLayout _loadingContainer;
    private TextView _textPoints;
    private TextView _textUsername;
    private TextView _textTag;
    private TextView _textLastRecord;
    private TextView _textGamesPlayed;
    private TextView _textGamesWon;
    private TextView _textGamesLost;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        _loadingContainer = getView().findViewById(R.id.loadingContainer);
        _textPoints = getView().findViewById(R.id.profilePoints);
        _textUsername = getView().findViewById(R.id.profileUsername);
        _textTag = getView().findViewById(R.id.profileTag);
        _textLastRecord = getView().findViewById(R.id.profileLastRecord);
        _textGamesPlayed = getView().findViewById(R.id.profileGamesPlayed);
        _textGamesWon = getView().findViewById(R.id.profileGamesWon);
        _textGamesLost = getView().findViewById(R.id.profileGamesLost);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();
    }

    private void updateData() {
        _loadingContainer.setVisibility(View.VISIBLE);

        APIHelper.getProfileStats(getContext(), User.getInstance().getTag(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject data = response.getJSONObject("content");

                    _textPoints.setText(data.getJSONObject("points").getString("now"));
                    _textUsername.setText(User.getInstance().getUsername());
                    _textTag.setText(User.getInstance().getTag());
                    _textLastRecord.setText(data.getJSONObject("points").getString("record"));
                    _textGamesPlayed.setText(data.getString("total"));
                    _textGamesWon.setText(data.getString("won"));
                    _textGamesLost.setText(data.getString("lost"));

                    _loadingContainer.setVisibility(View.GONE);
                }
                catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}