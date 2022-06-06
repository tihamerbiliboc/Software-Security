package com.chatcrypt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.chatcrypt.Adapter.MessageAdapter;
import com.chatcrypt.Model.Messages;
import com.chatcrypt.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagingActivity extends AppCompatActivity {
    CircleImageView user_image;
    TextView username;
    ImageButton send_btn;
    EditText send_text;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    Intent intent;
    MessageAdapter messageAdapter;
    List<Messages> mMessages;
    RecyclerView recyclerView;
    KeyStore keyStore = null;
    String publicKey;
    String privateKey;
    String current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        user_image = findViewById(R.id.user_image);
        username = findViewById(R.id.user_name);
        send_btn = findViewById(R.id.send_btn);
        send_text = findViewById(R.id.send_text);
        intent = getIntent();
        String uId = intent.getStringExtra("userId");
        mMessages =  new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        databaseReference = FirebaseDatabase.getInstance("https://chatcrypt-23a35-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users").child(uId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username.setText(user.getUserName());
                readMessage(firebaseUser.getUid(), uId);
                publicKey = user.getPublicKey();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = "";
                current = send_text.getText().toString();
                message = encryptData(send_text.getText().toString(), publicKey);
                if(!message.equals("")){
                    sendMessage(firebaseUser.getUid(), uId, message);
                }else {
                    Toast.makeText(MessagingActivity.this, "There is no message to send", Toast.LENGTH_SHORT).show();
                }
                send_text.setText("");

            }
        });
    }

    private void sendMessage(String sender, String receiver, String message){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://chatcrypt-23a35-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        databaseReference.child("Messaging").push().setValue(hashMap);
    }

    private void readMessage(String myId, String userId){
        SharedPreferences sh = getSharedPreferences("ChatCryptPref", MODE_PRIVATE);

        databaseReference = FirebaseDatabase.getInstance("https://chatcrypt-23a35-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Messaging");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mMessages.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    if(messages.getReceiver().equals(myId) && messages.getSender().equals(userId)){
                        try {
                            privateKey = sh.getString("myKey", "default");
                            messages.setMessage(decryptData(messages.getMessage(), privateKey));
                        } catch (InvalidKeySpecException e) {
                            e.printStackTrace();
                        }
                        mMessages.add(messages);

                    }else if((messages.getReceiver().equals(userId) && messages.getSender().equals(myId))){
                        messages.setMessage(current);
                        mMessages.add(messages);

                    }


                    messageAdapter = new MessageAdapter(MessagingActivity.this, mMessages);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static String encryptData(String text, String pub_key) {
        try {
            byte[] data = text.getBytes("utf-8");
            PublicKey publicKey = getPublicKey(Base64.decode(pub_key.getBytes("utf-8"), Base64.DEFAULT));
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.encodeToString(cipher.doFinal(data),Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String decryptData(String text, String pri_key) throws InvalidKeySpecException {
        try {
            byte[] data =Base64.decode(text,Base64.DEFAULT);
            PrivateKey privateKey = getPrivateKey(Base64.decode(pri_key.getBytes("utf-8"),Base64.DEFAULT));
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(cipher.doFinal(data),"utf-8");
        } catch (Exception e) {
            return null;
        }
    }


    public static PublicKey getPublicKey(byte[] keyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }


    public static PrivateKey getPrivateKey(byte[] keyBytes) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }




}