package in.rjha.instagramclone.Fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import in.rjha.instagramclone.Model.UploadPost;
import in.rjha.instagramclone.R;

public class AddImageFragment extends Fragment {
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_GALLARY = 2;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private DatabaseReference mDatabase;
    StorageReference postrefrance;
    Uri uri;
    EditText desc;
    Button upload;
    CircleImageView profile_image;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_add_image, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        postrefrance= FirebaseStorage.getInstance().getReference("postpics");
        profile_image=view.findViewById(R.id.profile_image);
        upload=view.findViewById(R.id.upload);
        desc=view.findViewById(R.id.desc);
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               pickIMage();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uri!=null)
                {
                    upload(uri);
                }

            }
        });
        return view;
    }

    //making an intent for chossing a image file

    void pickIMage()
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(Intent.createChooser(intent,"select image"),1002);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1002){
            try {
                uri=data.getData();
                Bitmap bm= MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),data.getData());
                profile_image.setImageBitmap(bm);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), ""+e, Toast.LENGTH_SHORT).show();
            }
        }
        //the end  onActivityResult
    }
    // modify upload function
    public void upload(Uri uri)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String email = user.getEmail();
        final String description=desc.getText().toString();
        final String id=mDatabase.push().getKey();
        final String date = getCurrentDate(); // 오늘 날짜(업로드 날짜) 가져오기

        final ProgressDialog progressDialog=new ProgressDialog(getActivity());
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Uploading....");
        progressDialog.show();

        StorageReference fileRef = postrefrance.child("rjha_" + id);

        fileRef.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                String url = downloadUri.toString();

                UploadPost uploadPost = new UploadPost(id, description, email, url, date); // date 추가
                mDatabase.child("Posts").child(id).setValue(uploadPost)
                        .addOnSuccessListener(aVoid -> {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Post Added", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Failed to add post", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Failed to get download URL", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(getActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
        });


    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

}