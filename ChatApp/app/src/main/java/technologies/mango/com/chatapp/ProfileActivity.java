package technologies.mango.com.chatapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    String request_type;
    private ImageView profile_image;
    private  TextView  profile,status,total_frnds;
    private Button send_req,dec_req;
    private DatabaseReference databaseReference;
    ProgressDialog progressDialog;
    private  String current_state;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mFriendRequestDb;
    private DatabaseReference mFriendDataBase;
    private DatabaseReference mnotificationDataBase;

    private DatabaseReference mRootreference;

    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        current_state="not_friends";
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Loading User Data");
        progressDialog.setMessage("Please wait while we load data...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();


        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        profile=(TextView)findViewById(R.id.profile_text);
        status=(TextView)findViewById(R.id.user_status);
        total_frnds=(TextView)findViewById(R.id.total_friends);
        profile_image=(ImageView)findViewById(R.id.profile_image);
        send_req=(Button)findViewById(R.id.send_request);
        dec_req=(Button)findViewById(R.id.decline_req);
        dec_req.setVisibility(View.INVISIBLE);
        dec_req.setEnabled(false);

        uid= getIntent().getStringExtra("user_id");

        databaseReference= FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        mFriendRequestDb= FirebaseDatabase.getInstance().getReference().child("Friend_request");
        mFriendDataBase= FirebaseDatabase.getInstance().getReference().child("Friends");
        mnotificationDataBase=FirebaseDatabase.getInstance().getReference().child("notifications");

        mRootreference= FirebaseDatabase.getInstance().getReference();


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("name").getValue().toString();
                String db_status=dataSnapshot.child("status").getValue().toString();
                String image =dataSnapshot.child("thumb_image").getValue().toString();

                profile.setText(name);
                status.setText(db_status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.cm).into(profile_image);


                //**************FRIENDS LIST/REQUEST FEATURE*******//

                mFriendRequestDb.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(uid)){
                          request_type=dataSnapshot.child(uid).child("request_type").getValue().toString();
                            if(request_type.equals("received")){
                                current_state="req_received";
                                send_req.setText("Accept Friend Request");
                                dec_req.setVisibility(View.VISIBLE);
                                dec_req.setEnabled(true);


                            }
                            else if (request_type.equals("sent")){
                                current_state="req_sent";
                                send_req.setText("Cancel Friend Request");

                                dec_req.setVisibility(View.INVISIBLE);
                                dec_req.setEnabled(false);

                            }
                            progressDialog.dismiss();
                        }
                        else{
                            mFriendDataBase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(uid)){
                                        current_state="friends";
                                        send_req.setText("Unfriend this person");
                                        dec_req.setVisibility(View.INVISIBLE);
                                        dec_req.setEnabled(false);

                                    }
                                    progressDialog.dismiss();


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    progressDialog.dismiss();


                                }
                            });

                          }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.hide();

            }
        });

        dec_req.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mFriendRequestDb.child(mCurrentUser.getUid()).child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {

                    @Override
                    public void onSuccess(Void aVoid) {
                        mFriendRequestDb.child(uid).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {

                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ProfileActivity.this,"Declined  Friend Request",Toast.LENGTH_SHORT).show();
                                send_req.setText("Send Friend Request");
                                current_state="not_friends";
                                dec_req.setVisibility(View.INVISIBLE);
                                dec_req.setEnabled(false);
                            }
                        });
                    }
                });
            }
        });





        send_req.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_req.setEnabled(false);
                //********************NOT FRIENDSS****************//
                if(current_state.equals("not_friends")){

                    DatabaseReference notification_ref=mRootreference.child("notifications").child(uid).push();
                    String newnotificationId=notification_ref.getKey();
                    Map notificationData=new HashMap();
                    notificationData.put("from",mCurrentUser.getUid());
                    notificationData.put("type","request");



                Map requestMap=new HashMap();
                requestMap.put("Friend_request/"+mCurrentUser.getUid()+"/"+uid+"/"+"request_type","sent");
                requestMap.put("Friend_request/"+uid+"/"+mCurrentUser.getUid()+"/"+"request_type","received");
                requestMap.put("notifications/"+uid+"/"+newnotificationId,notificationData);


                    mRootreference.updateChildren(requestMap, new DatabaseReference.CompletionListener() {

                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError!=null)
                        {
                            Toast.makeText(ProfileActivity.this,"There was an error in sendin request",Toast.LENGTH_SHORT)
                                    .show();
                        }
                        send_req.setEnabled(true);
                        current_state="req_sent";
                        send_req.setText("Cancel Friend Request");
                    }

                });


                }
                //************CANCEL REQUEST******************//
                if(current_state.equals("req_sent")){
                    mFriendRequestDb.child(mCurrentUser.getUid()).child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {

                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendRequestDb.child(uid).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mnotificationDataBase.child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {

                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            send_req.setEnabled(true);
                                            current_state="not_friends";
                                            send_req.setText("Send Friend Request");

                                        }
                                    });


                                }
                            });
                        }
                    });
                }
                if(current_state.equals("friends")){
                    mFriendDataBase.child(mCurrentUser.getUid()).child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {

                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDataBase.child(uid).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    send_req.setEnabled(true);
                                    current_state="not_friends";
                                    send_req.setText("Send Friend Request");
                                    dec_req.setVisibility(View.INVISIBLE);
                                    dec_req.setEnabled(false);


                                }
                            });

                        }
                    });
                }
                if(current_state.equals("req_received")){
                    final String currentDate= DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDataBase.child(mCurrentUser.getUid()).child(uid).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {

                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDataBase.child(uid).child(mCurrentUser.getUid()).child("date").setValue(currentDate)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {

                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mFriendRequestDb.child(mCurrentUser.getUid()).child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {

                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mFriendRequestDb.child(uid).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            send_req.setEnabled(true);
                                                            current_state="friends";
                                                            send_req.setText("Unfriend this person");

                                                            dec_req.setVisibility(View.INVISIBLE);
                                                            dec_req.setEnabled(false);


                                                        }
                                                    });
                                                }
                                            });

                                        }
                                    });


                        }
                    });

                }


            }
        });





    }
}
