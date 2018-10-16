package technologies.mango.com.chatapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    private Toolbar mtoolbar;
    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    ProgressDialog progressDialog;
    private FirebaseUser current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mtoolbar=(Toolbar)findViewById(R.id.users_appbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        current_user= FirebaseAuth.getInstance().getCurrentUser();


        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Loading user data");
        progressDialog.setMessage("Please wait while we loading data...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        recyclerView=(RecyclerView)findViewById(R.id.users_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseReference= FirebaseDatabase.getInstance().getReference().child("users");
        progressDialog.dismiss();
        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.userssingle,
                UsersViewHolder.class,
                databaseReference

        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {
                final String user_id=getRef(position).getKey();
                viewHolder.setName(model.getName());
                viewHolder.setUsersStatus(model.getStatus());
                viewHolder.setUsersImage(model.getThumb_image(),getApplicationContext());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent i=new Intent(UsersActivity.this,ProfileActivity.class);
                        i.putExtra("user_id",user_id);
                        startActivity(i);


                    }
                });


            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);

    }



    private static class UsersViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView=itemView;


        }

        public void setName(String name) {
            TextView mUserName= (TextView) mView.findViewById(R.id.user_display_name);
            mUserName.setText(name);

        }

        public void setUsersStatus(String status) {
            TextView mUserstatus= (TextView) mView.findViewById(R.id.user_status);
            mUserstatus.setText(status);
        }


        public void setUsersImage(String thumb_image, Context applicationContext) {
            CircleImageView mUserImage=(CircleImageView)mView.findViewById(R.id.recycler_image);
            Picasso.with(applicationContext).load(thumb_image).placeholder(R.drawable.cm).into(mUserImage);

        }
    }

}
