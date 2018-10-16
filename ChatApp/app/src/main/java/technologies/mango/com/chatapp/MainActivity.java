package technologies.mango.com.chatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Toolbar mtoolbar;
    private ViewPager viewPager;
    private SectionPageAdapter msectionPageAdapter;
    private TabLayout tabLayout;
    private DatabaseReference UserRef;
    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabLayout=(TabLayout) findViewById(R.id.main_tabs);
        msectionPageAdapter=new SectionPageAdapter(getSupportFragmentManager());
        viewPager=(ViewPager)findViewById(R.id.tab_pager);
        viewPager.setAdapter(msectionPageAdapter);
        mAuth = FirebaseAuth.getInstance();
        mtoolbar=(Toolbar)findViewById(R.id.mainpage_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Mango Chat");
        tabLayout.setupWithViewPager(viewPager);
        user = mAuth.getCurrentUser();
        if(mAuth.getCurrentUser()!=null) {
            UserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        }





    }
    @Override
    public void onStart() {
        super.onStart();
        if (user != null) {
            UserRef.child("online").setValue("true");
            // User is signed in
        } else {
            send_to_start();
            // User is signed out
        }

        }
    @Override
    public void onStop() {
        super.onStop();
        if(user!=null){
            UserRef.child("online").setValue(ServerValue.TIMESTAMP);
          }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.log_out:
                FirebaseAuth.getInstance().signOut();
                send_to_start();
                break;
            case R.id.account_settings:
                Intent in=new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(in);
                break;
            case R.id.all_users:
                Intent i=new Intent(MainActivity.this,UsersActivity.class);
                startActivity(i);
                break;
        }
        return true;
    }

    private void send_to_start() {
        Intent i=new Intent(MainActivity.this,StartActivity.class);
        startActivity(i);
        finish();
    }

}
