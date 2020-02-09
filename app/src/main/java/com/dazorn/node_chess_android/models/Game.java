package com.dazorn.node_chess_android.models;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.dazorn.node_chess_android.R;
import com.dazorn.node_chess_android.utilities.ApplicationUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Game {
    private List<Player> _players = new ArrayList<>();
    private List<Round> _rounds = new ArrayList<>();
    private CurrentRound _round;
    private String _roomName;
    private Date _createdAt;
    private Date _askedAt;
    private Date _startedAt;

    public Game() { }

    public Game(JSONObject game) throws JSONException, ParseException {
        for (int i = 0; i < game.getJSONArray("players").length(); i++) {
            _players.add(new Player(game.getJSONArray("players").getJSONObject(i)));
        }

        for (int i = 0; i < game.getJSONArray("rounds").length(); i++) {
            _rounds.add(new Round(game.getJSONArray("rounds").getJSONObject(i)));
        }

        if(game.optJSONObject("round") != null) {
            _round = new CurrentRound(game.getJSONObject("round"));
        }

        _roomName = game.getString("roomName");

        if(game.getString("createdAt") != null) {
            _createdAt = ApplicationUtils.parse(game.getString("createdAt"));
        }

        if(game.getString("askedAt") != null) {
            _askedAt = ApplicationUtils.parse(game.getString("askedAt"));
        }

        if(game.optString("startedAt") != null && !game.optString("startedAt").isEmpty()) {
            _startedAt = ApplicationUtils.parse(game.getString("startedAt"));
        }
    }

    public List<Player> getPlayers() {
        return _players;
    }

    public Unit getUnitByPosition(int y, int x) {
        Unit unit = null;

        for(int i = 0; i < _players.size(); i++) {
            if(unit != null) {
                continue;
            }

            Player player = _players.get(i);
            unit = player.getUnitByPosition(y, x);
        }

        return unit;
    }

    public Player getMe() {
        for(int i = 0; i < _players.size(); i++) {
            if(_players.get(i).getUsername().equals(User.getInstance().getUsername())) {
                return _players.get(i);
            }
        }

        return null;
    }

    public Player getFoe() {
        for(int i = 0; i < _players.size(); i++) {
            if(!_players.get(i).getUsername().equals(User.getInstance().getUsername())) {
                return _players.get(i);
            }
        }

        return null;
    }

    public boolean isMyTurn() {
        if(_round != null) {
            return _round._by.equals(User.getInstance().getTag());
        }

        return false;
    }

    public String getRoomName() {
        return _roomName;
    }

    public long getRoundStartMilliseconds() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(_round._startedAt);
        calendar.add(Calendar.HOUR_OF_DAY, 1);

        return calendar.getTimeInMillis();
    }

    public JSONArray getBoardArray(Game game, GridLayout grid) {
        JSONArray json = new JSONArray();

        for(int i = 0; i < grid.getChildCount(); i++) {
            View cell = grid.getChildAt(i);

            if(cell != null) {
                Object tag = cell.getTag();

                if(tag != null) {
                    String[] posParts = tag.toString().split("-");
                    int y = Integer.parseInt(posParts[0]);
                    int x = Integer.parseInt(posParts[1]);

                    boolean hasUnit = false;

                    RelativeLayout layout = (RelativeLayout)cell;
                    ImageView imageView = (ImageView)layout.getChildAt(0);

                    if(layout.getChildAt(1) != null && imageView.getTag() == null) {
                            imageView = (ImageView)layout.getChildAt(1);
                    }

                    if(imageView.getTag() != null) {
                        hasUnit = true;
                    }

                    Log.i("CELL", "X:" + x + "-Y:" + y + "-H:" + (hasUnit == true ? 1 : 0));

                    //for(int p = 0; p < game.getPlayer s().size(); p++) {
                        //Game.Unit unit = game.getPlayers().get(p).getUnitByPosition(y, x);

                        //if(unit != null) {
                            //hasUnit = true;
                        //}
                    //}

                    try {
                        json.put(new BoardCell(x, y, !hasUnit).toJSON());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return json;
    }

    public View getCellByPosition(GridLayout grid, int x, int y) {
        View cell = null;

        for(int i = 0; i < grid.getChildCount(); i++) {
            View c = grid.getChildAt(i);

            if(c != null) {
                Object tag = c.getTag();

                if(tag != null) {
                    String[] posParts = tag.toString().split("-");
                    int cY = Integer.parseInt(posParts[0]);
                    int cX = Integer.parseInt(posParts[1]);

                    if(x == cX && y == cY) {
                        cell = c;
                    }
                }
            }
        }

        return cell;
    }

    public List<Move> getMovesByUnit(Unit unit) {
        List<Move> moves = new ArrayList<>();

        for(int i = 0; i < _players.size(); i++) {
            if(_players.get(i)._username.equals(User.getInstance().getUsername())) {
                for(int j = 0; j < _players.get(i)._moves.size(); j++) {
                    UnitMove move = _players.get(i)._moves.get(j);

                    if(unit._x == move._x && unit._y == move._y && unit._type == move._unitType) {
                        moves.addAll(move.moves);
                    }
                }
            }
        }

        return moves;
    }

    public int getRoundCount() {
        return _round._count;
    }

    public class Player {
        private String _userTag;
        private String _socketId;
        private String _username;
        private List<Unit> _units = new ArrayList<>();
        private List<Unit> _graveyard = new ArrayList<>();
        private List<UnitMove> _moves = new ArrayList<>();
        private GameConstants.PlayerClass _class;
        private boolean _underChess;

        public Player(JSONObject player) throws JSONException {
            _userTag = player.getString("userTag");
            _socketId = player.getString("socketId");
            _username = player.getString("username");

            for(int i = 0; i < player.getJSONArray("units").length(); i++) {
                _units.add(new Unit(player.getJSONArray("units").getJSONObject(i)));
            }

            for(int i = 0; i < player.getJSONArray("graveyard").length(); i++ ){
                _graveyard.add(new Unit(player.getJSONArray("graveyard").getJSONObject(i)));
            }

            if(player.optJSONArray("authorizedMoves") != null) {
                for(int i = 0; i < player.getJSONArray("authorizedMoves").length(); i++ ){
                    _moves.add(new UnitMove(player.getJSONArray("authorizedMoves").getJSONObject(i)));
                }
            }

            _class = player.getString("class").equals("local") ? GameConstants.PlayerClass.LOCAL : GameConstants.PlayerClass.REMOTE;
            _underChess = player.optBoolean("underChess", false);
        }

        public String getUsername() {
            return _username;
        }

        public String getTag() {
            return _userTag;
        }

        public GameConstants.PlayerClass getSide() {
            return _class;
        }

        public List<Unit> getGraveyard() {
            return _graveyard;
        }

        public Unit getUnitByPosition(int y, int x) {
            for(int i = 0; i < _units.size(); i++) {
                if(_units.get(i)._y == y && _units.get(i)._x == x) {
                    return _units.get(i);
                }
            }

            return null;
        }
    }

    public class Round {
        private int _number;
        private String _by;
        private Date _startedAt;
        //private Move _move;

        public Round(JSONObject round) throws JSONException, ParseException {
            _number = round.getInt("number");
            _by = round.getString("by");
            //_startedAt = ApplicationUtils.parse(round.getString("startedAt"));
        }
    }

    public class CurrentRound {
        private int _count;
        private String _by;
        private Date _startedAt;

        public CurrentRound(JSONObject round) throws JSONException, ParseException {
            _count = round.getInt("count");
            _by = round.getString("by");
            _startedAt = ApplicationUtils.parse(round.getString("startedAt"));
        }
    }

    public class Unit {
        private GameConstants.PlayerClass _side;
        private GameConstants.UnitType _type;
        private int _x;
        private int _y;
        private boolean _firstTimeMoving;

        public int Count = 1;
        public Player Player = null;

        public Unit(JSONObject unit) throws JSONException {
            _side = unit.getString("s").equals("l") ? GameConstants.PlayerClass.LOCAL : GameConstants.PlayerClass.REMOTE;
            _type = GameConstants.UnitType.values()[unit.getInt("u")];
            _x = unit.getInt("x");
            _y = unit.getInt("y");

            _firstTimeMoving = unit.optBoolean("f", false);
        }

        public int getDrawableId() {
            switch (_type){
                case PAWN: return R.drawable.ic_pawn;
                case TURRET: return R.drawable.ic_turret;
                case BISHOP: return R.drawable.ic_bishop;
                case KNIGHT: return R.drawable.ic_knight;
                case QUEEN: return R.drawable.ic_queen;
                case KING: return R.drawable.ic_king;
            }

            return -1;
        }

        public GameConstants.PlayerClass getSide(){
            return _side;
        }

        public GameConstants.UnitType getType() {
            return _type;
        }
    }

    public class UnitMove {
        private int _x;
        private int _y;
        private GameConstants.UnitType _unitType;
        private List<Move> moves = new ArrayList<>();

        public UnitMove(JSONObject unitMove) throws JSONException {
            _x = unitMove.getInt("x");
            _y = unitMove.getInt("y");
            _unitType = GameConstants.UnitType.values()[unitMove.getInt("u")];

            for(int i = 0; i < unitMove.getJSONArray("moves").length(); i++) {
                moves.add(new Move(unitMove.getJSONArray("moves").getJSONObject(i)));
            }
        }
    }

    public class Move {
        private int _x;
        private int _y;
        private boolean _empty;
        private boolean _first;
        private boolean _promote;

        public Move(JSONObject move) throws JSONException {
            System.out.println("M");
            _x = move.getInt("x");
            _y = move.getInt("y");

            _empty = move.optBoolean("empty", false);
            _first = move.optBoolean("first", false);
            _promote = move.optBoolean("promote", false);
        }

        public int getX(){
            return _x;
        }

        public int getY(){
            return _y;
        }
    }

    public class BoardCell {
        public int x;
        public int y;
        public boolean empty;

        public BoardCell(int x, int y, boolean empty) {
            this.x = x;
            this.y = y;
            this.empty = empty;
        }

        public JSONObject toJSON() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("x", x);
            obj.put("y", y);
            obj.put("empty", empty);

            return obj;
        }
    }

    public static class GameConstants {
        public enum PlayerClass {
            LOCAL,
            REMOTE
        }

        public enum UnitType {
            NONE,
            PAWN,
            TURRET,
            BISHOP,
            KNIGHT,
            QUEEN,
            KING
        }
    }
}
