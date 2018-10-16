package technologies.mango.com.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
    Button save_chngs;
    TextInputLayout status_editor;
    private DatabaseReference database;
    private FirebaseUser currentUser;
    ProgressDialog progressDialog;
    Toolbar mtoolbar;
    String old_status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        old_status=getIntent().getStringExtra("status_value");
        progressDialog=new ProgressDialog(this);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();
        database = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        status_editor=(TextInputLayout)findViewById(R.id.status_id);
        status_editor.getEditText().setText(old_status);
        save_chngs=(Button)findViewById(R.id.save_change);
        mtoolbar = (Toolbar) findViewById(R.id.status_appbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        save_chngs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status_equal=status_editor.getEditText().getText().toString();
                progressDialog.setTitle("Saving Changes");
                progressDialog.setMessage("Please wait while we save changes..");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                database.child("status").setValue(status_equal).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                            Intent i=new Intent(StatusActivity.this,SettingsActivity.class);
                            startActivity(i);
                            Toast.makeText(StatusActivity.this,"Saved Successfully",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            progressDialog.hide();
                            Toast.makeText(StatusActivity.this,"Not saved,Try Again Later",Toast.LENGTH_SHORT).show();

                        }

                    }
                });


            }
        });
    }
}
