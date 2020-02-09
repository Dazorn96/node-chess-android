package com.dazorn.node_chess_android.adapters;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dazorn.node_chess_android.R;
import com.dazorn.node_chess_android.fragments.RankingsFragment;
import com.dazorn.node_chess_android.models.Rank;

import java.util.ArrayList;
import java.util.List;

public class ListViewRanksAdapter extends BaseAdapter {
    private RankingsFragment _fragment;
    private List<Rank> _items;

    public ListViewRanksAdapter(RankingsFragment fragment, List<Rank> items) {
        _fragment = fragment;
        _items = items;
    }

    @Override
    public int getCount() {
        return _items.size();
    }

    @Override
    public Object getItem(int i) {
        return _items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null) {
            view = _fragment.getLayoutInflater().inflate(R.layout.list_item_rankings_user, viewGroup, false);
        }

        Rank rank = (Rank) getItem(i);

        ((TextView) view.findViewById(R.id.rankPosition)).setText(Integer.toString(i + 1));
        ((TextView) view.findViewById(R.id.rankUsername)).setText(rank.getUsername());
        ((TextView) view.findViewById(R.id.rankPoints)).setText(Integer.toString(rank.getPoints()));

        return view;
    }

    public void setItems(List<Rank> items) {
        _items = items;
    }
}
