package com.chatcrypt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
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
//    PrivateKey privateKey;



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

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        databaseReference = FirebaseDatabase.getInstance("https://chatcrypt-23a35-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users").child(uId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username.setText(user.getUserName());
                readMessage(firebaseUser.getUid(), uId);
                publicKey = user.getPublicKey();
                privateKey = user.getPrivateKey();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String message = send_text.getText().toString();
                String message = "";
                try {
                    message = AESEncryption(send_text.getText().toString(), publicKey);
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    e.printStackTrace();
                }
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
        mMessages =  new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance("https://chatcrypt-23a35-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Messaging");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mMessages.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    if(messages.getReceiver().equals(myId) && messages.getSender().equals(userId) ||
                            messages.getReceiver().equals(userId) && messages.getSender().equals(myId)){
                        try {
                            messages.setMessage(AESDecryption(messages.getMessage(), privateKey));
                        } catch (IOException | KeyStoreException | CertificateException | NoSuchAlgorithmException | UnrecoverableEntryException | InvalidKeySpecException e) {
                            e.printStackTrace();
                        }
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

    private String AESEncryption(String message, String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encodedBytes = null;
        byte[] publicBytes = MyBase64.decode(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        Toast.makeText(MessagingActivity.this, pubKey.toString(),Toast.LENGTH_SHORT);
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.ENCRYPT_MODE, pubKey);
            encodedBytes = c.doFinal(message.getBytes());
        } catch (Exception e) {
            Log.e("Crypto", "RSA encryption error");
            Log.e("Crypto", e.getMessage());
        }

        String returnString = null;
        try {

            returnString = new String(encodedBytes, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return returnString;
    }

    private String AESDecryption(String message, String privateKey) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException, InvalidKeySpecException {

        byte[] privateBytes = MyBase64.decode(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privKey = keyFactory.generatePrivate(keySpec);
//        keyStore = KeyStore.getInstance("AndroidKeyStore");
//        keyStore.load(null);
//        privateKey = getprivateKey("cryptoChat");
        Toast.makeText(MessagingActivity.this, privKey.toString(),Toast.LENGTH_SHORT);
        byte[] encryptedByte = message.getBytes("ISO-8859-1");
        String decryptionString = message;
        byte[] decryption;
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.DECRYPT_MODE, privKey);
            decryption = c.doFinal(message.getBytes());
            decryptionString = new String(decryption);
        } catch (Exception e) {
            Log.e("Crypto", "RSA decryption error");
            Log.e("Crypto", e.getMessage());
        }
        return decryptionString;
    }

    private PrivateKey getprivateKey(String alias) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableEntryException {
        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        if (keyStore.containsAlias(alias)) {
            KeyStore.Entry entry = keyStore.getEntry("cryptoChat", null);
            Toast.makeText(MessagingActivity.this, ((KeyStore.PrivateKeyEntry) entry).getPrivateKey().toString(), Toast.LENGTH_LONG).show();
            return ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
        } else{
            Toast.makeText(MessagingActivity.this, "could not found key", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}