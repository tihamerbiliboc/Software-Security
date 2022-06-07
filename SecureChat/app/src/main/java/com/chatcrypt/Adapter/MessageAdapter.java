package com.chatcrypt.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chatcrypt.MessagingActivity;
import com.chatcrypt.Model.Messages;
import com.chatcrypt.Model.User;
import com.chatcrypt.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MESSAGE_TYPE_LEFT = 0;
    public static final int MESSAGE_TYPE_RIGHT = 1;
    private Context mContex;
    private List<Messages> mMessages;
    FirebaseUser firebaseUser;

    public MessageAdapter(Context context, List<Messages> messages){
        this.mContex = context;
        this.mMessages = messages;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MESSAGE_TYPE_RIGHT){
            View view = LayoutInflater.from(mContex).inflate(R.layout.messageitem_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }else{
            View view = LayoutInflater.from(mContex).inflate(R.layout.messageitem_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Messages messages = mMessages.get(position);
        holder.show_message.setText(messages.getMessage());



    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView show_message;
        public ImageView profileImage;

        public ViewHolder(View itemView){
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            profileImage = itemView.findViewById(R.id.user_image);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mMessages.get(position).getSender().equals(firebaseUser.getUid())){
            return MESSAGE_TYPE_RIGHT;

        }else {
            return MESSAGE_TYPE_LEFT;
        }
    }
}
