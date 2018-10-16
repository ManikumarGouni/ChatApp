package technologies.mango.com.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    CircleImageView circleImageView;
    TextView display_name,mstatus;
    Button chg_imag,chg_sts;
    private DatabaseReference database;
    private FirebaseUser current_user;
    private static int GALLERY_PICK=1;
    private StorageReference mStorageRef;
    String uid;
    ProgressDialog progressDialog;
    private Toolbar mtoolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mtoolbar=(Toolbar)findViewById(R.id.settings_appbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressDialog=new ProgressDialog(this);
        current_user= FirebaseAuth.getInstance().getCurrentUser();
         uid =current_user.getUid();
        database = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        database.keepSynced(true);


        circleImageView=(CircleImageView)findViewById(R.id.circleImageView);
        display_name=(TextView)findViewById(R.id.display_name);
        mstatus=(TextView)findViewById(R.id.status);
        chg_imag=(Button)findViewById(R.id.change_image);
        chg_sts=(Button)findViewById(R.id.change_sts);

        mStorageRef = FirebaseStorage.getInstance().getReference();


        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("name").getValue().toString();
                final String image=dataSnapshot.child("image").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                final String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();

                display_name.setText(name);
                mstatus.setText(status);

               // Picasso.with(SettingsActivity.this).load(thumb_image).placeholder(R.drawable.cm).into(circleImageView);
                Picasso.with(SettingsActivity.this).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.cm).into(circleImageView, new Callback() {

                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(SettingsActivity.this).load(thumb_image).placeholder(R.drawable.cm).into(circleImageView);


                    }
                });





            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        chg_sts.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String status_value=mstatus.getText().toString();
                Intent i=new Intent(SettingsActivity.this,StatusActivity.class);
                i.putExtra("status_value",status_value);
                startActivity(i);
            }
        });
        chg_imag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i,"Select Image"),GALLERY_PICK);

               /*CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);*/

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_PICK&&resultCode==RESULT_OK){
            Uri imageUri=data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);


        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                progressDialog.setTitle("Uploading Image");
                progressDialog.setMessage("Please wait while we upload and process the image");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                Uri resultUri = result.getUri();
                File thumb_path=new File(resultUri.getPath());
                 Bitmap thumb_compressd = new Compressor(this)
                         .setMaxWidth(200)
                         .setMaxHeight(200)
                         .setQuality(75)
                         .compressToBitmap(thumb_path);
                ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
                thumb_compressd.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
                final byte[] thumb_byte =byteArrayOutputStream.toByteArray();



                StorageReference filepath = mStorageRef.child("profile_images").child(uid+".jpg");
                final StorageReference thumb_filepath = mStorageRef.child("profile_images").child("thumbs").child(uid+".jpg");


                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){
                            @SuppressWarnings("VisibleForTests") final String download_url=task.getResult().getDownloadUrl().toString();

                            final UploadTask thumb_upload=thumb_filepath.putBytes(thumb_byte);
                            thumb_upload.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    @SuppressWarnings("VisibleForTests")String thumb_download=thumb_task
                                            .getResult()
                                            .getDownloadUrl()
                                            .toString();

                                    if(thumb_task.isSuccessful()){
                                        Map update_hashmap=new HashMap();
                                        update_hashmap.put("image",download_url);
                                        update_hashmap.put("thumb_image",thumb_download);


                                        database.updateChildren(update_hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    progressDialog.dismiss();

                                                    Toast.makeText(SettingsActivity.this,"Saved Successfully",Toast.LENGTH_SHORT).show();


                                                }
                                            }
                                        });
                                    }
                                }
                            });

                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this,"Error in Uploading",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
