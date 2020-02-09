package com.dazorn.node_chess_android.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dazorn.node_chess_android.R;
import com.dazorn.node_chess_android.activities.GameActivity;
import com.dazorn.node_chess_android.activities.MainActivity;
import com.dazorn.node_chess_android.helpers.SocketHelper;
import com.dazorn.node_chess_android.models.Game;
import com.dazorn.node_chess_android.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.ParseException;

import io.socket.emitter.Emitter;

public class MainFragment extends Fragment {
    private RelativeLayout _loadingContainer;
    private TextView _textUsersOnline;
    private EditText _editTextAskGame;
    private Button _askGame;
    private Button _searchGame;

    private boolean _searching = false;

    private static Context _context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        _context = getContext();

        _loadingContainer = getView().findViewById(R.id.loadingContainer);
        _textUsersOnline = getView().findViewById(R.id.textUsersOnline);
        _editTextAskGame = getView().findViewById(R.id.editTextAskGame);
        _searchGame = getView().findViewById(R.id.searchGame);
        _askGame = getView().findViewById(R.id.askGame);

        _searchGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(!_searching) {
                        _loadingContainer.setVisibility(View.VISIBLE);

                        SocketHelper.GetInstance(getActivity()).SearchGame(User.getInstance().getTag());
                        _searchGame.setText(R.string.main_searchgameabort_text);

                        _askGame.setEnabled(false);
                        _editTextAskGame.setEnabled(false);
                    }
                    else {
                        _loadingContainer.setVisibility(View.GONE);

                        SocketHelper.GetInstance(getActivity()).AbortSearchGame();
                        _searchGame.setText(R.string.main_searchgame_text);

                        _editTextAskGame.setEnabled(true);
                        if(_editTextAskGame.getText().length() == 6) {
                            _askGame.setEnabled(true);
                        }
                    }

                    _searching = !_searching;
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });

        _editTextAskGame.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean correctSize = charSequence.length() == 6 && !_searching;
                _askGame.setEnabled(correctSize);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        _askGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userTag = "#" + _editTextAskGame.getText().toString();

                _askGame.setEnabled(false);
                _editTextAskGame.setEnabled(false);
                _searchGame.setEnabled(false);

                _loadingContainer.setVisibility(View.VISIBLE);
                _searching = true;

                try {
                    SocketHelper.GetInstance(getActivity()).AskGame(userTag);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            SocketHelper.GetInstance(getActivity()).OnUserConnected(OnUserConnectionChanged);
            SocketHelper.GetInstance(getActivity()).OnUserDisconnected(OnUserConnectionChanged);
            SocketHelper.GetInstance(getActivity()).OnAskGameError(OnAskGameError);
            SocketHelper.GetInstance(getActivity()).OnGameFound(OnGameFound);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener OnUserConnectionChanged = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Handler handler = new Handler(_context.getMainLooper());

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    _textUsersOnline.setText(args[0].toString());

                    if(_loadingContainer.getVisibility() == View.VISIBLE) {
                        _loadingContainer.setVisibility(View.GONE);
                    }
                }
            };

            handler.post(runnable);
        }
    };

    private Emitter.Listener OnGameFound = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i("GAME", "GAME FOUND");
            Handler handler = new Handler(_context.getMainLooper());

            Runnable runnnable = new Runnable() {
                @Override
                public void run() {
                    Game game = new Game();

                    try {
                        game = new Game((JSONObject) args[0]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    try {
                        SocketHelper.GetInstance(getActivity()).setGame(game);
                        if(game.getRoomName() != null) {
                            SocketHelper.GetInstance(getActivity()).JoinRoom(game.getRoomName());
                        }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(_context, getString(R.string.main_gamefound_text), Toast.LENGTH_LONG).show();

                    _searchGame.setText(R.string.main_searchgame_text);

                    _editTextAskGame.setEnabled(true);
                    _editTextAskGame.getText().clear();
                    _askGame.setEnabled(false);

                    Intent intent = new Intent(_context, GameActivity.class);
                    Bundle options = ActivityOptionsCompat.makeCustomAnimation(_context,
                            R.anim.fade_in,
                            R.anim.fade_out).toBundle();
                    _context.startActivity(intent, options);

                    _loadingContainer.setVisibility(View.GONE);
                }
            };

            handler.post(runnnable);
        }
    };

    private Emitter.Listener OnAskGameError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Handler handler = new Handler(_context.getMainLooper());

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    _askGame.setEnabled(true);
                    _editTextAskGame.setEnabled(true);
                    _searchGame.setEnabled(true);

                    _loadingContainer.setVisibility(View.GONE);
                    _searching = false;

                    Toast toast = Toast.makeText(_context, getString(R.string.main_askgame_error), Toast.LENGTH_LONG);
                    toast.show();
                }
            };
            handler.post(runnable);
        }
    };
}
