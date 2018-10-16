package technologies.mango.com.chatapp;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String chatUser,user_name,thumb_image;
    private Toolbar toolbar;
    private DatabaseReference mRootRef;
    private TextView mTitleView,mLastSeenView;
    private CircleImageView mProfileimage;
    FirebaseAuth mAuth;
    String current_user;

    private ImageButton mChatAddbutton;
    private ImageButton mSendbtn;
    private EditText mEditText;
    private RecyclerView mMessagesList;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private SwipeRefreshLayout refresh;
    private static  final int TOTAL_ITEMS_LOAD=10;
    private  int mCurrentPage=1;
    private  int itemPos=0;
    private  String mLast_key="";
    private  String mPrevKey="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        refresh=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        mChatAddbutton=(ImageButton)findViewById(R.id.chat_add_btn);
        mSendbtn=(ImageButton)findViewById(R.id.chat_send_btn);
        mEditText=(EditText)findViewById(R.id.chat_message_view);

        ////recyclerview..
        mAdapter = new MessageAdapter(messagesList);

        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);



        toolbar=(Toolbar)findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        mAuth=FirebaseAuth.getInstance();
        current_user=mAuth.getCurrentUser().getUid();
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        ///intent extras
        chatUser=getIntent().getStringExtra("user_id");
        user_name=getIntent().getStringExtra("user_name");
        thumb_image=getIntent().getStringExtra("thumb_image");

        LayoutInflater inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbar_view=inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(actionbar_view);
        //custom action bar items
        mTitleView=(TextView)findViewById(R.id.custom_displayname);
        mLastSeenView=(TextView)findViewById(R.id.lastseen);
        mProfileimage=(CircleImageView)findViewById(R.id.custom_image);

        mTitleView.setText(user_name);
        mRootRef=FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);
       //
         loadMessages();
        mRootRef.child("users").child(chatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online=dataSnapshot.child("online").getValue().toString();
                String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();
                Picasso.with(getApplicationContext()).load(thumb_image).placeholder(R.drawable.cm).into(mProfileimage);

                if (online.equals("true")){
                  mLastSeenView.setText("Online");
              }
              else{
                  long lastTime=Long.parseLong(online);
                  String lastseenTime= GetTimeGo.getTimeAgo(lastTime,getApplicationContext());
                  mLastSeenView.setText(lastseenTime);
              }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(current_user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(chatUser)){
                    Map chatAddMap=new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap= new HashMap();
                    chatUserMap.put("Chat/"+current_user+"/"+chatUser,chatAddMap);
                    chatUserMap.put("Chat/"+chatUser+"/"+current_user,chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                       if(databaseError!=null){
                           Log.d("CHAT_LOG",databaseError.getMessage().toString());
                       }
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /////........

        mSendbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendMessage();


            }
        });
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos=0;
                loadMoreMessages();

            }
        });



    }

    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootRef.child("messages").child(current_user).child(chatUser);
        Query message_query = messageRef.orderByKey().endAt(mLast_key).limitToLast(10);

        message_query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                String messsage_key=dataSnapshot.getKey();
                if(!mPrevKey.equals(messsage_key)){
                    messagesList.add(itemPos++,message);


                }
                else{
                    mPrevKey=mLast_key;
                }
                if(itemPos==1){
                    mLast_key=messsage_key;
                }

                Log.d("TOTALKEYS","last key:"+mLast_key+"|prev key:"+mPrevKey+"|message key:"+messsage_key);
                mAdapter.notifyDataSetChanged();
                refresh.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10,0);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });
    }


    private void loadMessages() {
        DatabaseReference messageRef=  mRootRef.child("messages").child(current_user).child(chatUser);
        Query message_query= messageRef.limitToLast(mCurrentPage=TOTAL_ITEMS_LOAD);

        message_query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                itemPos++;
                if(itemPos==1){
                        String messsage_key=dataSnapshot.getKey();
                         mLast_key=messsage_key;
                            mPrevKey=messsage_key;
                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(messagesList.size()-1);
                refresh.setRefreshing(false);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {
        String message=mEditText.getText().toString();
        if (!TextUtils.isEmpty(message)){
            String current_user_ref = "messages/" + current_user + "/" + chatUser;
            String chat_user_ref = "messages/" + chatUser + "/" + current_user;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(current_user).child(chatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", current_user);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mEditText.setText("");
            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError != null){

                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                    }

                }
            });
           }
    }
}
