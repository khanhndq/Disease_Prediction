package com.example.diseaseprediction.ui.account;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.diseaseprediction.AccountEdit;
import com.example.diseaseprediction.MainActivity;
import com.example.diseaseprediction.R;
import com.example.diseaseprediction.firebase.FirebaseConstants;
import com.example.diseaseprediction.object.Account;
import com.example.diseaseprediction.object.DoctorInfo;
import com.example.diseaseprediction.object.Specialization;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    private static final String TAG = "AccountFragment";
    private final int PICK_IMAGE_REQUEST = 71;

    //Firebase
    private StorageReference sRef;
    private DatabaseReference mRef;
    private FirebaseUser fUser;

    private Account mAccount;
    private DoctorInfo mDoctor;
    private Specialization mSpecialization;
    private ArrayAdapter<Specialization> specializationAdapter;
    private ArrayList<Specialization> specialization;
    private Uri imgPath;

    private TextView account_txt_name, account_txt_gender, account_txt_phone, account_txt_email,
        account_txt_address, account_doctor_txt_specialization, account_doctor_txt_experience,
        account_doctor_txt_description;

    private AutoCompleteTextView account_spinner_gender, account_doctor_spinner_specialization;

    private LinearLayout account_layout_normal, account_layout_doctor;
    private Button account_btn_edit;
    private CircleImageView account_img_avatar;
    private ImageView account_img_avatar_upload;
    private ShimmerFrameLayout account_shimmer_avatar;

    private Context context;


    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set toolbar
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.menu_account));
        ((MainActivity) getActivity()).setIconToolbar();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view,
                              @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getActivity().getApplicationContext();


        //Set data for spinner
        ArrayList<String> gender = new ArrayList<>();
        gender.add(context.getString(R.string.default_gender_male));
        gender.add(context.getString(R.string.default_gender_female));
        ArrayAdapter genderAdapter = new ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item, gender);
        account_spinner_gender.setAdapter(genderAdapter);

        //Click button edit
        account_btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AccountEdit.class);
                startActivity(intent);

            }
        });

        account_img_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                // Accepted image types
                String[] imageTypes = {"image/png", "image/jpg", "image/jpeg"};
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, imageTypes);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.filechooser_picture)),
                    PICK_IMAGE_REQUEST);
            }
        });

        account_img_avatar_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.filechooser_picture)),
                    PICK_IMAGE_REQUEST);
            }
        });


        //get data and load it to UI
        getDataForUI();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        container.removeAllViews();
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_account, container, false);
        //Get current user
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        //Find view by id
        findViews(view);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Open file image mobile
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
            && data != null && data.getData() != null) {
            imgPath = data.getData();
            uploadImage();
        }
    }

    /**
     * Find view by ID
     *
     * @param view View of current activity
     */
    private void findViews(View view) {
        try {
            // shimmer
            account_shimmer_avatar = view.findViewById(R.id.account_shimmer_avatar);
            account_shimmer_avatar.startShimmer();

            account_layout_normal = view.findViewById(R.id.account_layout_normal);
            account_layout_doctor = view.findViewById(R.id.account_layout_doctor);
            //Button
            account_btn_edit = view.findViewById(R.id.account_btn_edit);


            //Find view
            account_txt_name = view.findViewById(R.id.account_txt_name);

            account_txt_gender = view.findViewById(R.id.account_txt_gender);

            account_txt_phone = view.findViewById(R.id.account_txt_phone);

            account_txt_email = view.findViewById(R.id.account_txt_email);

            account_txt_address = view.findViewById(R.id.account_txt_address);

            account_img_avatar = view.findViewById(R.id.account_img_avatar);

            account_img_avatar_upload = view.findViewById(R.id.account_img_avatar_upload);

            account_doctor_txt_experience = view.findViewById(R.id.account_doctor_txt_experience);

            account_doctor_txt_description = view.findViewById(R.id.account_doctor_txt_description);

            account_doctor_txt_specialization = view.findViewById(R.id.account_doctor_txt_specialization);
            account_doctor_spinner_specialization = view.findViewById(R.id.account_doctor_spinner_specialization);

            //Spinner
            account_spinner_gender = view.findViewById(R.id.account_spinner_gender);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "getMessagesFirebase()");
        }
    }

    /**
     * Get data for UI
     */
    private void getDataForUI() {
        try {
            //get user by id
            mAccount = new Account();
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_ACCOUNT);
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //check user exist in firebase
                    //if exist then set UI
                    if (snapshot.hasChild(fUser.getUid())) {
                        mAccount = snapshot.child(fUser.getUid()).getValue(Account.class);
                        try {
                            //Set Doctor information by type
                            if (mAccount.getTypeID() == 0) {
                                account_layout_doctor.setVisibility(View.VISIBLE);
                                loadDataOfDoctor();
                            } else {
                                account_layout_doctor.setVisibility(View.GONE);
                            }
                            //Set name
                            if (!mAccount.getName().equals("Default")) {
                                String name = mAccount.getName();
                                account_txt_name.setText(Html.fromHtml(context.getString(R.string.account_txt_title_name_format, name)));
                            }

                            //Set gender
                            int gender = mAccount.getGender();
                            String genderStr = account_spinner_gender.getAdapter().getItem(gender).toString();
                            account_txt_gender.setText(Html.fromHtml(context.getString(R.string.account_txt_title_gender_format, genderStr)));

                            //Set phone
                            if (!mAccount.getPhone().equals("Default")) {
                                String phone = mAccount.getPhone();
                                account_txt_phone.setText(Html.fromHtml(context.getString(R.string.account_txt_title_phone_format, phone)));
                            }

                            //Set email
                            if (!mAccount.getEmail().equals("Default")) {
                                String email = mAccount.getEmail();
                                account_txt_email.setText(Html.fromHtml(context.getString(R.string.account_txt_title_email_format, email)));
                            }

                            //Set address
                            if (!mAccount.getAddress().equals("Default")) {
                                String address = mAccount.getAddress();
                                account_txt_address.setText(Html.fromHtml(context.getString(R.string.account_txt_title_address_format, address)));
                            }

                            //Set image
                            if (!mAccount.getImage().equals("Default")) {
                                Glide.with(context)
                                    .load(mAccount.getImage())
                                    .error(R.mipmap.ic_default_avatar_round)
                                    .into(account_img_avatar);
                            } else {
                                account_img_avatar.setImageResource(R.mipmap.ic_default_avatar_round);
                            }
                            // Stop shimmer and display user layout
                            account_shimmer_avatar.stopShimmer();
                            account_shimmer_avatar.setVisibility(View.GONE);

                            account_layout_normal.setVisibility(View.VISIBLE);
                            account_btn_edit.setVisibility(View.VISIBLE);
                        } catch (NullPointerException e) {
                            Log.d(TAG, "Account. Account ID null", e);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "getDataForUI()");
        }
    }

    /**
     * Load all data of current account
     */
    private void loadDataOfDoctor() {
        try {
            //get user by id
            mDoctor = new DoctorInfo();
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_DOCTOR_INFO);
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //check user exist in firebase
                    //if exist then set UI
                    System.out.println(fUser.getUid());
                    if (snapshot.hasChild(fUser.getUid())) {
                        mDoctor = snapshot.child(fUser.getUid()).getValue(DoctorInfo.class);
                        try {
                            //Set spinner
                            specialization = new ArrayList<>();
                            mRef =
                                FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_SPECIALIZATION);
                            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot sh : snapshot.getChildren()) {
                                        mSpecialization = sh.getValue(Specialization.class);
                                        try {
                                            assert mSpecialization != null;
                                            if (mDoctor.getSpecializationID().equals(mSpecialization.getSpecializationID())) {
                                                String specialization = mSpecialization.getName();
                                                account_doctor_txt_specialization.setText(Html.fromHtml(context.getString(R.string.account_info_doctor_specialization_format, specialization)));
                                                account_doctor_spinner_specialization.setText(specialization);
                                            }
                                            specialization.add(mSpecialization);
                                        } catch (NullPointerException e) {
                                            Log.d(TAG, "Account. loadDataOfDoctor", e);
                                        }
                                    }
                                    //Set spinner
                                    specializationAdapter = new ArrayAdapter<>(context,
                                        R.layout.support_simple_spinner_dropdown_item, specialization);
                                    account_doctor_spinner_specialization.setAdapter(specializationAdapter);
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }

                            });


                            if (mDoctor.getExperience() != -1) {
                                String experience = String.valueOf(mDoctor.getExperience());
                                account_doctor_txt_experience.setText(Html.fromHtml(context.getString(R.string.account_info_doctor_experience_format, experience)));
                            }

                            if (!mDoctor.getShortDescription().equals("Default")) {
                                String description = mDoctor.getShortDescription();
                                account_doctor_txt_description.setText(Html.fromHtml(context.getString(R.string.account_info_doctor_description_format, description)));
                            }
                        } catch (NullPointerException e) {
                            Log.d(TAG, "Account. loadDataOfDoctor", e);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "loadDataOfDoctor()");
        }
    }

    /**
     * Upload file img to storage firebase
     */
    private void uploadImage() {
        try {
            if (imgPath != null) {
                final ProgressDialog progressDialog = new ProgressDialog(requireContext());
                progressDialog.setTitle(getString(R.string.upload_img_waiting));
                progressDialog.show();

                //Get reference "images" in storage firebase
                sRef =
                    FirebaseStorage.getInstance().getReference().child(FirebaseConstants.STORAGE_IMG + "/" + UUID.randomUUID().toString());
                sRef.putFile(imgPath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(requireContext(), getString(R.string.upload_img_done), Toast.LENGTH_SHORT).show();
                            //Get url of image in storage firebase
                            sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    mRef =
                                        FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_ACCOUNT);
                                    // Update image path of user
                                    mRef.child(fUser.getUid()).child("image").setValue(uri.toString());
                                    // load local image
                                    Glide.with(context)
                                        .load(imgPath)
                                        .error(R.mipmap.ic_default_avatar_round)
                                        .into(account_img_avatar);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(requireContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "uploadImage()");
        }
    }
}