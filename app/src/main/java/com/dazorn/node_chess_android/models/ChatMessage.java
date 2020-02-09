package com.dazorn.node_chess_android.models;

public class ChatMessage {
    private String _username;
    private String _tag;
    private String _message;
    private Boolean _owning = false;

    public String getUsername() {
        return _username;
    }

    public String getTag() {
        return _tag;
    }

    public String getMessage() {
        return _message;
    }

    public Boolean getOwning() {
        return _owning;
    }

    public ChatMessage(String username, String tag, String message) {
        _username = username;
        _tag = tag;
        _message = message;
        _owning = true;
    }

    public ChatMessage(Object[] args) {
        _username = args[0].toString();
        _tag = args[1].toString();
        _message = args[2].toString();
    }
}
