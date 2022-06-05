package com.chatcrypt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;


import com.chatcrypt.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    public static final String TAG = "TAG";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "cryptoChat";
    MaterialEditText userName, emailAddress, mPassword;
    Button register_btn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    DatabaseReference databaseReference;
    String userID;
    Key publicKey = null;
    Key privateKey = null;
    KeyStore keyStore = null;

    private void generateKeys(){

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE);

            kpg.initialize(new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setKeySize(2048)
                    .build());
            KeyPair keyPair = kpg.generateKeyPair();

        } catch (Exception e) {
            Log.e("Crypto", "RSA key pair error");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userName   = findViewById(R.id.username);
        emailAddress      = findViewById(R.id.email);
        mPassword   = findViewById(R.id.password);
        register_btn= findViewById(R.id.register_button);
        mLoginBtn   = findViewById(R.id.alreadyText);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fAuth = FirebaseAuth.getInstance();

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = emailAddress.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                final String username = userName.getText().toString();

                if(TextUtils.isEmpty(email)){
                    emailAddress.setError("Email is Required.");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is Required.");
                    return;
                }

                if(password.length() < 6){
                    mPassword.setError("Password Must be >= 6 Characters");
                    return;
                }

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser fuser = fAuth.getCurrentUser();
                            String user_id = fuser.getUid();
                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(RegisterActivity.this, "Verification Email Has been Sent.", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: Email not sent " + e.getMessage());
                                }
                            });

                            Toast.makeText(RegisterActivity.this, "User Created.", Toast.LENGTH_SHORT).show();
                            generateKeys();
                            try {
                                keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
                                keyStore.load(null);
                                KeyStore.Entry entry = keyStore.getEntry(KEY_ALIAS, null);
                                publicKey = keyStore.getCertificate(KEY_ALIAS).getPublicKey();

                                PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
                            } catch (KeyStoreException e) {
                                e.printStackTrace();
                            } catch (UnrecoverableEntryException e) {
                                e.printStackTrace();
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            } catch (CertificateException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String pubkey = MyBase64.encode(publicKey.getEncoded());
                            User user = new User(user_id, username, pubkey);
                            DatabaseReference mDatabase = FirebaseDatabase.getInstance("https://chatcrypt-23a35-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
                            mDatabase.child("users").child(user_id).setValue(user);
                            startActivity(new Intent(getApplicationContext(), Login.class));

                        }else {
                            Toast.makeText(RegisterActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
            }
        });

    }
}


