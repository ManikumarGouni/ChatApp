package technologies.mango.com.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    TextInputLayout email,password,profilename;
    Button createaccount;
    private FirebaseAuth mAuth;
    String profile,reg_mail,reg_pass;
    private Toolbar mtoolbar;
    private ProgressDialog mprogressdialog;
    DatabaseReference database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        profilename=(TextInputLayout)findViewById(R.id.reg_username);
        email=(TextInputLayout)findViewById(R.id.reg_email);
        password=(TextInputLayout)findViewById(R.id.reg_password);
        createaccount=(Button)findViewById(R.id.create);
        mAuth = FirebaseAuth.getInstance();


        mtoolbar=(Toolbar)findViewById(R.id.reg_toolbar);
        mprogressdialog=new ProgressDialog(this);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        createaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile=profilename.getEditText().getText().toString();
                reg_mail=email.getEditText().getText().toString();
                reg_pass=password.getEditText().getText().toString();
                if(!TextUtils.isEmpty(profile)||!TextUtils.isEmpty(reg_mail)||!TextUtils.isEmpty(reg_pass)){
                    mprogressdialog.setTitle("Registering User");
                    mprogressdialog.setMessage("Please wait while we create your account");
                    mprogressdialog.setCanceledOnTouchOutside(false);
                    mprogressdialog.show();
                    register_user(profile,reg_mail,reg_pass);
                }


            }
        });
    }

    private void register_user(final String profile, final String reg_mail, final String reg_pass) {
        mAuth.createUserWithEmailAndPassword(reg_mail, reg_pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = currentUser.getUid();
                            database = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                            String device_token= FirebaseInstanceId.getInstance().getToken();
                            HashMap<String,String> usermap=new HashMap<String, String>();
                            usermap.put("name",profile);
                            usermap.put("status","hii!! welcome");
                            usermap.put("image","default");
                            usermap.put("thumb_image","default");
                            usermap.put("device_token",device_token);

                            database.setValue(usermap);
                            Toast.makeText(RegisterActivity.this, "Succesfully Created",
                                    Toast.LENGTH_SHORT).show();

                              mprogressdialog.dismiss();

                            Intent i=new Intent(RegisterActivity.this,MainActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();

                            }
                        else{
                            mprogressdialog.hide();
                            Toast.makeText(RegisterActivity.this, "Can't Sign in ,Please check your details",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
}
