package technologies.mango.com.chatapp;

/**
 * Created by VASU on 21-Nov-17.
 */

import android.content.Context;
import android.graphics.Color;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{


    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    String current_user;
    public MessageAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout ,parent, false);
        mAuth=FirebaseAuth.getInstance();

        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;
        public TextView displayName;

        public MessageViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);
            displayName = (TextView) view.findViewById(R.id.name_text_layout);

        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        Messages c = mMessageList.get(i);

        final String from_user = c.getFrom();
        if(mAuth.getCurrentUser()!=null) {
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(from_user);
             current_user=mAuth.getCurrentUser().getUid();
        }
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                viewHolder.displayName.setText(name);

                Picasso.with(viewHolder.profileImage.getContext()).load(image)
                        .placeholder(R.drawable.cm).into(viewHolder.profileImage);

                    if(from_user.equals(current_user)){
                        viewHolder.messageText.setBackgroundColor(Color.WHITE);
                        viewHolder.messageText.setTextColor(Color.BLACK);
                    }
                    else{
                        viewHolder.messageText.setGravity(Gravity.RIGHT);
                        viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
                        viewHolder.messageText.setTextColor(Color.WHITE);
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        viewHolder.messageText.setText(c.getMessage());

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }



}