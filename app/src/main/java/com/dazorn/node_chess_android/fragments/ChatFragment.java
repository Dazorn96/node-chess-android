package com.dazorn.node_chess_android.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dazorn.node_chess_android.R;
import com.dazorn.node_chess_android.activities.MainActivity;
import com.dazorn.node_chess_android.adapters.ListViewMessagesAdapter;
import com.dazorn.node_chess_android.helpers.SocketHelper;
import com.dazorn.node_chess_android.models.ChatMessage;
import com.dazorn.node_chess_android.models.User;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.net.Socket;
import java.net.URISyntaxException;

import io.socket.client.Ack;
import io.socket.emitter.Emitter;

public class ChatFragment extends Fragment {
    private ListView _listViewMessages;
    private EditText _editTextMessage;

    private static Context _context;
    private static ListViewMessagesAdapter _listViewMessagesAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        _context = getContext();

        _listViewMessages = getView().findViewById(R.id.listViewMessages);
        _editTextMessage = getView().findViewById(R.id.editTextMessage);

        _listViewMessages.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if(focus) {
                    MainActivity.HideKeyboard(view);
                }
            }
        });

        _listViewMessagesAdapter = new ListViewMessagesAdapter(this);
        _listViewMessages.setAdapter(_listViewMessagesAdapter);

        _editTextMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if(!focus) {
                    MainActivity.HideKeyboard(view);
                }
            }
        });
        _editTextMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if(i == EditorInfo.IME_ACTION_SEND) {
                    User user = User.getInstance();

                    final ChatMessage message = new ChatMessage(user.getUsername(), user.getTag(), textView.getText().toString());

                    try {
                        SocketHelper.GetInstance(getActivity()).SendMessage(message);
                        OnMessageSent(message);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    handled = true;
                }

                return handled;
            }
        });

        try {
            SocketHelper.GetInstance(getActivity()).OnGeneralChatMessage(OnMessage);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener OnMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            ChatMessage message = new ChatMessage(args);
            OnGeneralChatMessage(message);
        }
    };

    private void OnMessageSent(ChatMessage message) {
        OnGeneralChatMessage(message);
        _editTextMessage.getText().clear();
        MainActivity.HideKeyboard(_listViewMessages);
    }

    public static void OnGeneralChatMessage(final ChatMessage message) {
        if(_context == null) {
            return;
        }

        Handler handler = new Handler(_context.getMainLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                _listViewMessagesAdapter.Messages.add(message);
                _listViewMessagesAdapter.notifyDataSetChanged();
            }
        };

        handler.post(runnable);
    }
}
