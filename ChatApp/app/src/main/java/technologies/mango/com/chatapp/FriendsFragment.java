package technologies.mango.com.chatapp;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private RecyclerView recycler_friends;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;
    private View mMainView;
    String current_user;
    String username,thumb_image;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView=inflater.inflate(R.layout.fragment_friends, container, false);
        recycler_friends=(RecyclerView)mMainView.findViewById(R.id.friends_recycler);
        mAuth=FirebaseAuth.getInstance();
        current_user= mAuth.getCurrentUser().getUid().toString();
        mFriendDatabase= FirebaseDatabase.getInstance().getReference().child("Friends").child(current_user);
        mFriendDatabase.keepSynced(true);
        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("users");
        mUsersDatabase.keepSynced(true);
        recycler_friends.setHasFixedSize(true);
        recycler_friends.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.userssingle,
                FriendsViewHolder.class,
                mFriendDatabase

        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                viewHolder.setDate(model.getDate());
                final String user_id=getRef(position).getKey();
                mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("online")){
                           String userOnline= dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);

                        }
                         username=dataSnapshot.child("name").getValue().toString();
                        thumb_image=dataSnapshot.child("thumb_image").getValue().toString();
                        viewHolder.setDisplayName(username);
                        viewHolder.setUsersImage(thumb_image,getContext());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                CharSequence options[]=new CharSequence[]{"Open Profile","Send Message"};
                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Option");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {

                                        if(i==0){
                                            Intent profileIntent=new Intent(getContext(),ProfileActivity.class);
                                            profileIntent.putExtra("user_id",user_id);
                                            startActivity(profileIntent);
                                        }
                                        if(i==1){
                                            Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("user_id",user_id);
                                            chatIntent.putExtra("user_name",username);
                                            chatIntent.putExtra("thumb_image",thumb_image);
                                            startActivity(chatIntent);


                                        }
                                    }
                                });
                                builder.show();

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        };
        recycler_friends.setAdapter(firebaseRecyclerAdapter);
    }


    private static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView=itemView;

        }

        public void setDate(String date) {
            TextView userNameView=(TextView)mView .findViewById(R.id.user_status);
             userNameView.setText(date);

            }

        public void setDisplayName(String displayName) {
            TextView displaynameView=(TextView)mView .findViewById(R.id.user_display_name);
            displaynameView.setText(displayName);

        }

        public void setUsersImage(String thumb_image, Context applicationContext) {
            CircleImageView mUserImage=(CircleImageView)mView.findViewById(R.id.recycler_image);
            Picasso.with(applicationContext).load(thumb_image).placeholder(R.drawable.cm).into(mUserImage);

        }

        public void setUserOnline(String userOnline) {
            ImageView onlineimage=(ImageView)mView.findViewById(R.id.online);

                if(userOnline.equals("true")){
                    onlineimage.setVisibility(View.VISIBLE);
                }
                else{
                    onlineimage.setVisibility(View.INVISIBLE);

                }
        }
    }
}
