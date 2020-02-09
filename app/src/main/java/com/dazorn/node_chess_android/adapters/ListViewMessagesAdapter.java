package com.dazorn.node_chess_android.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.dazorn.node_chess_android.R;
import com.dazorn.node_chess_android.fragments.ChatFragment;
import com.dazorn.node_chess_android.models.ChatMessage;
import com.dazorn.node_chess_android.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ListViewMessagesAdapter extends BaseAdapter {
    private ChatFragment _fragment;
    public List<ChatMessage> Messages = new ArrayList<>();

    public ListViewMessagesAdapter(ChatFragment fragment) {
        _fragment = fragment;
    }

    @Override
    public int getCount() {
        return Messages.size();
    }

    @Override
    public Object getItem(int i) {
        return Messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null) {
            view = _fragment.getLayoutInflater().inflate(R.layout.list_item_chat_message, viewGroup, false);
        }

        ChatMessage message = (ChatMessage) getItem(i);

        LinearLayout container = view.findViewById(R.id.messageContainer);
        TextView messageTime = view.findViewById(R.id.messageTime);

        container.setTag(message.getOwning());

        if(message.getOwning()) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.RIGHT;
            container.setLayoutParams(params);
            messageTime.setGravity(Gravity.RIGHT);

            container.setBackground(ContextCompat.getDrawable(_fragment.getContext(), R.drawable.box_message_owning));
            ((TextView) view.findViewById(R.id.messageUsername)).setText(R.string.chat_message_owning_title);
        }
        else {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.LEFT;
            container.setLayoutParams(params);
            messageTime.setGravity(Gravity.LEFT);

            container.setBackground(ContextCompat.getDrawable(_fragment.getContext(), R.drawable.box_message));

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean owning = (boolean) view.getTag();

                    if(!owning) {
                        String userTag = ((TextView) view.findViewById(R.id.messageUsername)).getText().toString().split("#")[1];

                        ClipboardManager clipboard = (ClipboardManager) _fragment.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData data = ClipData.newPlainText("Copied Tag", userTag);
                        clipboard.setPrimaryClip(data);

                        Toast toast = Toast.makeText(_fragment.getContext(), _fragment.getString(R.string.chat_tag_copied), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });

            ((TextView) view.findViewById(R.id.messageUsername)).setText(message.getUsername() + " " + message.getTag());
        }

        ((TextView) view.findViewById(R.id.messageContent)).setText(message.getMessage());

        Date now = new Date();
        String nowTime = new SimpleDateFormat("hh:mm").format(now);
        messageTime.setText(nowTime);

        return view;
    }
}
