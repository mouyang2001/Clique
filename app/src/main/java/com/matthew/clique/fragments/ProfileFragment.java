package com.matthew.clique.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.matthew.clique.BackdropActivity;
import com.matthew.clique.MainActivity;
import com.matthew.clique.R;
import com.matthew.clique.models.User;
import com.theartofdev.edmodo.cropper.CropImage;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {

    }

    private CircleImageView profileImage;
    private ImageView profileBackdrop;
    private TextView profileNameField, bioField;
    private ProgressBar progressBar;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private StorageReference storage;

    private String userName;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.circleImageViewProfileImage);
        profileBackdrop = view.findViewById(R.id.imageViewProfileBackdrop);
        profileNameField = view.findViewById(R.id.textViewProfileName);
        progressBar = view.findViewById(R.id.progressBarProfile);
        bioField = view.findViewById(R.id.textViewProfileBio);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true).build();
        firebaseFirestore.setFirestoreSettings(settings);
        storage = FirebaseStorage.getInstance().getReference();

        userId = firebaseAuth.getUid();

        final MainActivity mainActivity = (MainActivity)getActivity();

        firebaseFirestore
                .collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);

                        String profileImageUri = user.getProfile_image();
                        if (profileImageUri != null) {
                            Glide.with(getContext()).load(profileImageUri).into(profileImage);
                        }

                        String backdropImageUri = user.getBackdrop_image();
                        if (backdropImageUri != null) {
                            Glide.with(getContext()).load(backdropImageUri).centerCrop().into(profileBackdrop);
                        }

                        String name = user.getFirst_name() + " " + user.getLast_name();
                        profileNameField.setText(name);

                        if (user.getBio() != null) {
                            bioField.setText(user.getBio());
                        }
                    }
                });

        profileBackdrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), BackdropActivity.class);
                getContext().startActivity(intent);
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        CropImage.activity()
                                .setAspectRatio(1,1)
                                .start(getContext(), ProfileFragment.this);

                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                profileImage.setImageURI(resultUri);

                StorageReference imagePath = storage.child("profile_images").child(userId+".jpg");
                uploadImage(imagePath, resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void uploadImage(StorageReference path, Uri uri) {
        final StorageReference imagePath = path;
        imagePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        firebaseFirestore
                                .collection("Users")
                                .document(userId)
                                .update("profile_image", uri.toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getActivity(), "Profile image changed", Toast.LENGTH_SHORT).show();
                                        } else {
                                            String e = task.getException().getMessage();
                                            Toast.makeText(getActivity(), e, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }
                });
            }
        });
    }
}
