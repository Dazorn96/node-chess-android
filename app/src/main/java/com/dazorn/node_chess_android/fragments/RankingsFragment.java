package com.dazorn.node_chess_android.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.dazorn.node_chess_android.R;
import com.dazorn.node_chess_android.adapters.ListViewRanksAdapter;
import com.dazorn.node_chess_android.helpers.APIHelper;
import com.dazorn.node_chess_android.helpers.SocketHelper;
import com.dazorn.node_chess_android.models.Rank;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class RankingsFragment extends Fragment {
    private SwipeRefreshLayout _swipeRefresh;
    private ListView _listViewRanks;

    private ListViewRanksAdapter _adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rankings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        _swipeRefresh = getView().findViewById(R.id.swipeRefresh);
        _listViewRanks = getView().findViewById(R.id.listViewRanks);

        _swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateRanks();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        _swipeRefresh.setRefreshing(true);
        updateRanks();
    }

    private void updateRanks(){
        APIHelper.getGlobalRankings(getContext(), new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray("content");
                    List<Rank> ranks = new ArrayList<>();

                    for(int i = 0; i < data.length(); i++) {
                        ranks.add(new Rank(data.getJSONObject(i)));
                    }

                    setAdapter(ranks);
                }
                catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void setAdapter(List<Rank> ranks) {
        if(_adapter == null) {
            _adapter = new ListViewRanksAdapter(this, ranks);
            _listViewRanks.setAdapter(_adapter);
        }
        else {
            _adapter.setItems(ranks);
            _adapter.notifyDataSetChanged();
        }

        _swipeRefresh.setRefreshing(false);
    }
}