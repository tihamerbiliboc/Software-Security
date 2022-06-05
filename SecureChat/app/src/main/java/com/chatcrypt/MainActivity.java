package com.chatcrypt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chatcrypt.Fragments.ChatFragment;
import com.chatcrypt.Fragments.UsersFragment;
import com.chatcrypt.Model.User;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "TAG";
    Button mLogout;
    TextView userName;
    FirebaseAuth fAuth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    FirebaseDatabase dbase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userName = findViewById(R.id.user_name);
        fAuth = FirebaseAuth.getInstance();
        firebaseUser = fAuth.getCurrentUser();
        dbase = FirebaseDatabase.getInstance("https://chatcrypt-23a35-default-rtdb.europe-west1.firebasedatabase.app/");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");


        if( firebaseUser == null ){
            startActivity(new Intent(getApplicationContext(),Login.class));
            finish();
        }
        databaseReference = dbase.getReference("users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                userName.setText(user.getUserName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        TabLayout tLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.view_pager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new UsersFragment(), "Users");
        viewPagerAdapter.addFragment(new ChatFragment(), "Messages");
        viewPager.setAdapter(viewPagerAdapter);
        tLayout.setupWithViewPager(viewPager);


    }

    class ViewPagerAdapter extends FragmentPagerAdapter{

        private ArrayList<Fragment> fragments;
        private  ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager manager){
            super(manager);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();

        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();//logout
                startActivity(new Intent(getApplicationContext(),Login.class));
                finish();
                return true;
        }

        return false;

    }
}

