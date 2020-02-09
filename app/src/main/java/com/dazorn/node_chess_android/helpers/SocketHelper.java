package com.dazorn.node_chess_android.helpers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityOptionsCompat;

import com.dazorn.node_chess_android.R;
import com.dazorn.node_chess_android.activities.GameActivity;
import com.dazorn.node_chess_android.fragments.ChatFragment;
import com.dazorn.node_chess_android.models.ChatMessage;
import com.dazorn.node_chess_android.models.Game;
import com.dazorn.node_chess_android.models.User;
import com.dazorn.node_chess_android.utilities.ApplicationUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketHelper {
    private static SocketHelper _instance;

    private final String _endpoint = "https://fcf11980.ngrok.io";//"https://node-chess-online.herokuapp.com";
    private Socket _socket;
    private static Activity _activity;
    private Game _game;
    private boolean _canMove;

    public static SocketHelper GetInstance(Activity activity) throws URISyntaxException {
        if(_instance == null) {
            _instance = new SocketHelper();
        }

        _activity = activity;

        return _instance;
    }

    private SocketHelper() throws URISyntaxException {
        try {
            _socket = IO.socket(_endpoint);

            _socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("SOCKET ONLINE");
                }
            });

            _socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("SOCKET OFFLINE");
                    ApplicationUtils.restartApplication(_activity);
                }
            });

            _socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("SOCKET CONNECTION FAILED");
                    ApplicationUtils.restartApplication(_activity);
                }
            });
        }
        catch (URISyntaxException ex) {
            throw ex;
        }

        try {
            _socket.on("game resumed", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    Handler handler = new Handler(_activity.getMainLooper());

                    Runnable runnnable = new Runnable() {
                        @Override
                        public void run() {
                            Game game = new Game();

                            try {
                                game = new Game((JSONObject) args[1]);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            try {
                                SocketHelper.GetInstance(_activity).setGame(game);
                                if(game.getRoomName() != null) {
                                    SocketHelper.GetInstance(_activity).JoinRoom(game.getRoomName());
                                }
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(_activity, _activity.getString(R.string.main_gamefound_text), Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(_activity, GameActivity.class);
                            intent.putExtra("CAN_MOVE", Boolean.parseBoolean(args[0].toString()));
                            intent.putExtra("RESUMED", true);
                            Bundle options = ActivityOptionsCompat.makeCustomAnimation(_activity,
                                    R.anim.fade_in,
                                    R.anim.fade_out).toBundle();
                            _activity.startActivity(intent, options);
                        }
                    };

                    handler.post(runnnable);
                }
            });

            _socket.on("next turn", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        EmitNextTurn((boolean)args[0], new Game((JSONObject)args[1]));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });

            _socket.connect();
            _socket.emit("android connection", User.getInstance().getId());
        }
        catch (Exception ex) {
            throw ex;
        }
    }

    private void EmitNextTurn(final boolean canMove, final Game game) {
        try {
            GameActivity.OnNextTurn(canMove, game);
        }
        catch (Exception ex) {
            Handler handler = new Handler(_activity.getMainLooper());

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    EmitNextTurn(canMove, game);
                }
            };

            handler.postDelayed(runnable, 1000);
        }
    }

    public void OnUserConnected(Emitter.Listener listener) {
        _socket.on("user connected", listener);
    }

    public void OnUserDisconnected(Emitter.Listener listener){
        _socket.on("user disconnected", listener);
    }

    public void OnGeneralChatMessage(Emitter.Listener listener) {
        _socket.on("general chat message", listener);
    }

    public void OnAskGameError(Emitter.Listener listener){
        _socket.on("ask for game error", listener);
    }

    public void OnGameFound(Emitter.Listener listener){
        _socket.on("game found", listener);
    }

    public void OnAuthorizedMovement(Emitter.Listener listener) {
        _socket.on("do movement", listener);
    }

    public void OnDenyMovement(Emitter.Listener listener){
        _socket.on("deny movement", listener);
    }

    public void OnPawnPromote(Emitter.Listener listener) {
        _socket.on("do pawn promote", listener);
    }

    public void OnGameEnd(Emitter.Listener listener) {
        _socket.on("game end", listener);
    }

    public void SendMessage(ChatMessage message) {
        _socket.emit("general chat message", message.getUsername(), message.getTag(), message.getMessage());
    }

    public void SearchGame(String userTag) {
        _socket.emit("search game", userTag);
    }

    public void AbortSearchGame() {
        _socket.emit("abort search game");
    }

    public void AskGame(String userTag){
        _socket.emit("ask for game", User.getInstance().getTag(), userTag);
    }

    public void JoinRoom(String roomName){
        _socket.emit("join room", new BigInteger(roomName));
    }

    public void GameReady(String roomName, JSONArray board) {
        _socket.emit("game ready", new BigInteger(roomName), board);
    }

    public void RequestMovement(String roomName, String userTag, JSONArray board, int startX, int startY, int endX, int endY) {
        _socket.emit("request authorize movement", new BigInteger(roomName), userTag, board, startX, startY, endX, endY);
    }

    public void RequestPawnPromote(String roomName, String userTag, int x, int y, int type) {
        _socket.emit("request pawn promote", new BigInteger(roomName), userTag, x, y, type);
    }

    public void EndTurn(String roomName, JSONArray board, int roundCount) {
        _socket.emit("end turn", new BigInteger(roomName), board, roundCount);
    }

    public Game getGame(){
        return _game;
    }

    public void setGame(Game game) {
        _game = game;
    }
}
