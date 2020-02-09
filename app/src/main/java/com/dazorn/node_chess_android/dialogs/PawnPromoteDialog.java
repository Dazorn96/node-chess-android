package com.dazorn.node_chess_android.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.dazorn.node_chess_android.R;
import com.dazorn.node_chess_android.activities.GameActivity;

public class PawnPromoteDialog extends Dialog implements View.OnClickListener {
    private GameActivity _activity;
    private int _x;
    private int _y;

    public PawnPromoteDialog(GameActivity activity, int x, int y) {
        super(activity);

        _activity = activity;
        _x = x;
        _y = y;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.game_promote_pawn);

        findViewById(R.id.promote_turret).setOnClickListener(this);
        findViewById(R.id.promote_bishop).setOnClickListener(this);
        findViewById(R.id.promote_knight).setOnClickListener(this);
        findViewById(R.id.promote_queen).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int type = Integer.parseInt(view.getTag().toString());
        _activity.RequestPawnPromote(type, _x, _y);
    }
}
