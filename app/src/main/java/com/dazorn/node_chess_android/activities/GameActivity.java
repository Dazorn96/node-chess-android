package com.dazorn.node_chess_android.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dazorn.node_chess_android.R;
import com.dazorn.node_chess_android.dialogs.PawnPromoteDialog;
import com.dazorn.node_chess_android.helpers.SocketHelper;
import com.dazorn.node_chess_android.models.Game;
import com.dazorn.node_chess_android.models.GameEnd;
import com.dazorn.node_chess_android.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import io.socket.emitter.Emitter;

public class GameActivity extends AppCompatActivity {
    private static GameActivity _ref;

    private Context _context;
    private Game _game;
    private boolean _canMove = false;

    private GridLayout _board;
    private LinearLayout _foeGraveyard;
    private LinearLayout _userGraveyard;

    private TextView _gameTimer;
    private TextView _foeUsername;
    private TextView _userUsername;

    private RelativeLayout _selectedUnit;
    private RelativeLayout _destinationUnit;

    private boolean _fieldReady = false;

    private long _gameTimerEnd = 0;
    private Handler _gameTimerHandler = new Handler();
    private Runnable _gameTimerRunnable = new Runnable() {
        @Override
        public void run() {
            long ms = _gameTimerEnd - System.currentTimeMillis();
            int secs = (int) ms / 1000;

            if(secs > 60) {
                secs = 60;
            }

            if(secs > 30) {
                _gameTimer.setTextColor(Color.parseColor("#388E3C"));
            }
            else if(secs > 10) {
                _gameTimer.setTextColor(Color.parseColor("#FBC02D"));
            }
            else {
                _gameTimer.setTextColor(Color.parseColor("#D32F2F"));
            }

            _gameTimer.setText(String.format("%02d", secs));
            _gameTimerHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE);

        _context = this;
        _ref = this;

        try {
            _game = SocketHelper.GetInstance(this).getGame();

            _foeUsername = findViewById(R.id.foeUsername);
            _gameTimer = findViewById(R.id.gameTimer);

            SocketHelper.GetInstance(this).OnAuthorizedMovement(OnAuthorizedMovement);
            SocketHelper.GetInstance(this).OnDenyMovement(OnDenyMovement);
            SocketHelper.GetInstance(this).OnPawnPromote(OnPawnPromote);
            SocketHelper.GetInstance(this).OnGameEnd(OnGameEnd);

            if(_game.getMe().getSide() == Game.GameConstants.PlayerClass.REMOTE) {
                _foeUsername = findViewById(R.id.foeUsername);
                _userUsername = findViewById(R.id.userUsername);
                _foeGraveyard = findViewById(R.id.foeGraveyard);
                _userGraveyard = findViewById(R.id.userGraveyard);
                _board = findViewById(R.id.boardRemote);
            }
            else {
                _foeUsername = findViewById(R.id.userUsername);
                _userUsername = findViewById(R.id.foeUsername);
                _foeGraveyard = findViewById(R.id.userGraveyard);
                _userGraveyard = findViewById(R.id.foeGraveyard);

                _board = findViewById(R.id.boardLocal);
            }

            _userUsername.setText(getText(R.string.game_user_username));
            _foeUsername.setText(_game.getFoe().getUsername());
            _board.setVisibility(View.VISIBLE);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        boolean resumed = intent.getBooleanExtra("RESUMED", false);

        if(_game.getMe().getSide() == Game.GameConstants.PlayerClass.LOCAL && resumed == false) {
            renderUnits();
            gameReady();
        }
        else if(resumed == true) {
            boolean canMove = intent.getBooleanExtra("CAN_MOVE", false);
            _canMove = canMove;
            renderUnits();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    @Override
    public void onBackPressed() {
        return;
    }

    private void renderUnits() {
        Handler handler = new Handler(getMainLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < _board.getChildCount(); i++) {
                    View cell = _board.getChildAt(i);

                    if(cell != null) {
                        cell.setOnClickListener(null);
                        Object tag = cell.getTag();

                        if(tag != null) {
                            String[] posParts = tag.toString().split("-");
                            int y = Integer.parseInt(posParts[0]);
                            int x = Integer.parseInt(posParts[1]);

                            boolean hasUnit = false;
                            for(int p = 0; p < _game.getPlayers().size(); p++) {
                                Game.Unit unit = _game.getPlayers().get(p).getUnitByPosition(y, x);

                                ImageView imageView = (ImageView)((RelativeLayout)cell).getChildAt(0);

                                if(unit != null) {
                                    imageView.setImageDrawable(getDrawable(unit.getDrawableId()));
                                    imageView.setPadding(1, 1, 1, 1);

                                    if(_game.getPlayers().get(p).getSide() == Game.GameConstants.PlayerClass.REMOTE) {
                                        imageView.setColorFilter(Color.parseColor("#ffffff"));
                                    }
                                    else {
                                        imageView.setColorFilter(Color.parseColor("#000000"));
                                    }

                                    hasUnit = true;
                                    imageView.setTag(unit);

                                    if(_canMove && _game.getPlayers().get(p).getUsername().equals(User.getInstance().getUsername())) {
                                        imageView.setOnClickListener(onUnitClicked);
                                    }
                                }
                            }

                            if(hasUnit == false) {
                                ImageView imageView = (ImageView)((RelativeLayout)cell).getChildAt(0);
                                imageView.clearColorFilter();
                                imageView.setImageResource(android.R.color.transparent);
                                imageView.setTag(null);
                                imageView.setOnClickListener(null);
                            }
                        }
                    }
                }
            }
        };

        handler.post(runnable);
    }

    private void renderGraveyards() {
        Handler handler = new Handler(getMainLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                List<Game.Unit> units = new ArrayList<>();

                _ref._foeGraveyard.removeAllViews();
                _ref._userGraveyard.removeAllViews();

                for(int i = 0; i < _ref._game.getPlayers().size(); i++) {
                    Game.Player p = _ref._game.getPlayers().get(i);

                    for (int j = 0; j < p.getGraveyard().size(); j++) {
                        Game.Unit u = p.getGraveyard().get(j);
                        Game.Unit ref = null;

                        for (int k = 0; k < units.size(); k++) {
                            if (units.get(k).getType().equals(u.getType()) && units.get(k).getSide().equals(u.getSide())) {
                                ref = units.get(k);
                            }
                        }

                        if (ref != null) {
                            ref.Count++;
                        } else {
                            u.Player = p;
                            units.add(u);
                        }
                    }
                }

                for(int i = 0; i < units.size(); i++) {
                    Game.Unit u = units.get(i);

                    LinearLayout layout = (LinearLayout) _ref.getLayoutInflater().inflate(R.layout.list_item_graveyard, null);
                    ImageView imageView = (ImageView) layout.findViewById(R.id.graveyardImage);
                    TextView textView = (TextView) layout.findViewById(R.id.graveyardCount);

                    imageView.setImageDrawable(getDrawable(u.getDrawableId()));

                    if(u.getSide() == Game.GameConstants.PlayerClass.REMOTE) {
                        imageView.setColorFilter(Color.parseColor("#ffffff"));
                    }
                    else {
                        imageView.setColorFilter(Color.parseColor("#000000"));
                    }

                    if(u.Count == 1) {
                        textView.setText("");
                    }
                    else {
                        textView.setText("x" + u.Count);
                    }

                    if(u.Player.getTag().equals(User.getInstance().getTag())) {
                        _ref._userGraveyard.addView(layout);
                    }
                    else {
                        _ref._foeGraveyard.addView(layout);
                    }
                }
            }
        };

        handler.post(runnable);
    }

    private void gameReady() {
        Handler handler = new Handler(getMainLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    SocketHelper.GetInstance(_ref).GameReady(_game.getRoomName(), _game.getBoardArray(_game, _board));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        };
        handler.post(runnable);
    }

    public static void OnNextTurn(boolean canMove, Game game) {
        _ref._canMove = canMove;
        Log.i("CAN_MOVE", _ref._canMove == true ? "SI": "NO");
        _ref._game = game;

        _ref._gameTimerEnd = _ref._game.getRoundStartMilliseconds() + (58 * 1000);
        _ref._gameTimerHandler.removeCallbacks(_ref._gameTimerRunnable);
        _ref._gameTimerHandler.postDelayed(_ref._gameTimerRunnable, 0);

        _ref.renderUnits();
        _ref.renderGraveyards();

        if(_ref._canMove == true) {
            Handler handler = new Handler(_ref.getMainLooper());

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(_ref._context, R.string.game_next_turn, Toast.LENGTH_LONG).show();
                }
            };
            handler.post(runnable);
        }
    }

    private View.OnClickListener onUnitClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ImageView imageView = (ImageView)view;
            Game.Unit unit = (Game.Unit) imageView.getTag();

            if(_selectedUnit != null)
            {
                clearBoardFromColors();
            }

            _selectedUnit = (RelativeLayout) imageView.getParent();

            List<Game.Move> moves = _game.getMovesByUnit(unit);
            for(int i = 0; i < moves.size(); i++) {
                RelativeLayout cell = (RelativeLayout)_game.getCellByPosition(_board, moves.get(i).getX(), moves.get(i).getY());

                ShapeDrawable shape = new ShapeDrawable();
                Paint paint = shape.getPaint();
                paint.setColor(Color.parseColor("#81E3BD"));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2);

                Drawable drawable = null;

                ImageView moveUnit = (ImageView) cell.getChildAt(0);
                if(moveUnit.getTag() != null) {
                    drawable = getDrawable(R.drawable.cell_red);
                    moveUnit.setOnClickListener(onTryToMove);
                }
                else {
                    drawable = getDrawable(R.drawable.cell_green);
                    cell.setOnClickListener(onTryToMove);
                }

                ImageView img = new ImageView(_context);
                img.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                img.setImageDrawable(drawable);
                img.setAlpha(0.8f);
                cell.addView(img);

                cell.getChildAt(0).bringToFront();
            }

            Drawable drawable = getDrawable(R.drawable.cell_blue);
            ImageView img = new ImageView(_context);
            img.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            img.setImageDrawable(drawable);
            img.setAlpha(0.8f);
            _selectedUnit.addView(img);

            imageView.bringToFront();
        }
    };

    private View.OnClickListener onTryToMove = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                ImageView unit = (ImageView) view;
                _destinationUnit = (RelativeLayout) unit.getParent();
            }
            catch (Exception ex) {
                RelativeLayout unit = (RelativeLayout) view;
                _destinationUnit = unit;
            }

            String[] posParts = _destinationUnit.getTag().toString().split("-");
            int endY = Integer.parseInt(posParts[0]);
            int endX = Integer.parseInt(posParts[1]);

            posParts = _selectedUnit.getTag().toString().split("-");
            int startY = Integer.parseInt(posParts[0]);
            int startX = Integer.parseInt(posParts[1]);

            try {
                SocketHelper.GetInstance(_ref).RequestMovement(_game.getRoomName(), User.getInstance().getTag(), _game.getBoardArray(_game, _board), startX, startY, endX, endY);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener OnAuthorizedMovement = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("ABC", "");
            if(!_game.isMyTurn()) {
                return;
            }

            final boolean promote = ((JSONObject)args[0]).optBoolean("promote");
            final JSONObject side = ((JSONObject)args[0]).optJSONObject("side");
            final boolean sideEffect = side != null;

            Handler handler = new Handler(_context.getMainLooper());

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    ImageView startUnit = (ImageView)_selectedUnit.getChildAt(1);

                    Object tag = startUnit.getTag();
                    Drawable drawable = startUnit.getDrawable();

                    ImageView endUnit = (ImageView)_destinationUnit.getChildAt(1);
                    endUnit.setTag(tag);
                    endUnit.setImageDrawable(drawable);
                    endUnit.clearColorFilter();

                    startUnit.setTag(null);
                    startUnit.setImageResource(android.R.color.transparent);

                    clearBoardFromColors();

                    try {
                        if(_game.isMyTurn() && promote == true) {
                            String[] posParts = _destinationUnit.getTag().toString().split("-");
                            int endY = Integer.parseInt(posParts[0]);
                            int endX = Integer.parseInt(posParts[1]);

                            PawnPromoteDialog promoteDialog = new PawnPromoteDialog(_ref, endX, endY);
                            promoteDialog.show();
                        }
                        else if(sideEffect == false) {
                            SocketHelper.GetInstance(_ref).EndTurn(_game.getRoomName(), _game.getBoardArray(_game, _board), _game.getRoundCount());
                        }
                        else {
                            OnSideEffect(side);
                        }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };

            handler.post(runnable);
        }
    };

    private void OnSideEffect(JSONObject sideMove) throws JSONException, URISyntaxException {
        int fromX = sideMove.getInt("originX");
        int fromY = sideMove.getInt("originY");
        int toX = sideMove.getInt("x");
        int toY = sideMove.getInt("y");

        RelativeLayout fromLayout = null;
        RelativeLayout toLayout = null;

        for(int i = 0; i < _board.getChildCount(); i++){
            View cell = _board.getChildAt(i);

            if (cell != null) {
                cell.setOnClickListener(null);
                Object tag = cell.getTag();

                if(tag != null) {
                    String[] posParts = tag.toString().split("-");
                    int y = Integer.parseInt(posParts[0]);
                    int x = Integer.parseInt(posParts[1]);

                    if(fromX == x && fromY == y) {
                        fromLayout = (RelativeLayout) cell;
                    }
                    else if(toX == x && toY == y) {
                        toLayout = (RelativeLayout) cell;
                    }
                }
            }
        }

        if(fromLayout != null && toLayout != null) {
            ImageView fromImage = (ImageView) fromLayout.getChildAt(0);
            ImageView toImage = (ImageView) toLayout.getChildAt(0);

            toImage.setImageDrawable(fromImage.getDrawable());
            toImage.setTag(fromImage.getTag());

            fromImage.setTag(null);
            fromImage.setImageDrawable(null);

            SocketHelper.GetInstance(_ref).EndTurn(_game.getRoomName(), _game.getBoardArray(_game, _board), _game.getRoundCount());
        }
    }

    private Emitter.Listener OnDenyMovement = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("ABC", "");
        }
    };

    private Emitter.Listener OnGameEnd = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            GameEnd end = null;

            try {
                end = new GameEnd((JSONObject)args[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            GameEnd.GameEndConstants.Reason reason = end.getReason();
            if(reason == GameEnd.GameEndConstants.Reason.CHECK_MATE ||
            reason == GameEnd.GameEndConstants.Reason.OUT_OF_TIME ||
            reason == GameEnd.GameEndConstants.Reason.CHEATING) {
                if(end.getBy().equals(User.getInstance().getTag())){
                    EndGame(true, false, false);
                }
                else {
                    EndGame(false, true, false);
                }
            }
            else {
                EndGame(false, false, true);
            }
        }
    };

    private void EndGame(final boolean win, final boolean lost, final boolean draw){
        Handler handler = new Handler(getMainLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                    String message = null;

                if(win == true) {
                    message = getString(R.string.game_won);
                }
                else if(lost == true) {
                    message = getString(R.string.game_lost);
                }
                else if(draw == true) {
                    message = getString(R.string.game_draw);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(_ref);
                builder.setMessage(message)
                        .setTitle(R.string.game_end)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                close();
                            }
                        }).show();
            }
        };

        handler.post(runnable);
    }

    private void close() {
        finish();
    }

    public void RequestPawnPromote(int type, int x, int y) {
        try {
            SocketHelper.GetInstance(_ref).RequestPawnPromote(_ref._game.getRoomName(), User.getInstance().getTag(), x, y, type);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener OnPawnPromote = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                SocketHelper.GetInstance(_ref).EndTurn(_game.getRoomName(), _game.getBoardArray(_game, _board), _game.getRoundCount());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    };

    private void showKingUnderChess() {
        // TODO: coloro la pedina del re se sotto scacco
    }

    private void clearBoardFromColors() {
        for(int i = 0; i < _board.getChildCount(); i++) {
            View cell = _board.getChildAt(i);

            if(cell != null) {
                Object tag = cell.getTag();

                if(tag != null) {
                    RelativeLayout layout = (RelativeLayout)cell;

                    if(layout.getChildCount() > 1) {
                        ((RelativeLayout)cell).removeViewAt(0);
                    }
                }
            }
        }
    }
}
