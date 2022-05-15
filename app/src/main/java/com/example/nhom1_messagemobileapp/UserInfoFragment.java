package com.example.nhom1_messagemobileapp;

import android.app.Dialog;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nhom1_messagemobileapp.entity.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserInfoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UserInfoFragment(String uid) {
        this.uid = uid;
    }

    public UserInfoFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserInfoFragment newInstance(String param1, String param2) {
        UserInfoFragment fragment = new UserInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private String uid;

    private TextView tvName;
    private ImageView imgAvatar;
    private Button btnDarkMode, btnEditInfo, btnChangePassword, btnLogout;
    private User theUser;
    private ProgressBar progressBar;
    private boolean isHiddenPassword;
    private boolean flagHiddenCurrentPassword = true;
    private boolean flagHiddenNewPassword = true;
    private boolean flagHiddenReNewPassword = true;

    private boolean isDarkMode = false;
    private UiModeManager uiModeManager;

    private FirebaseAuth mAuth;
    private FirebaseUser account;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);
        // Inflate the layout for this fragment

//        tìm đối tượng trong view
        tvName = view.findViewById(R.id.userInfo_tvName);
        imgAvatar = view.findViewById(R.id.userInfo_imgAvatar);
        btnDarkMode = view.findViewById(R.id.userInfo_btnDarkMode);
        btnEditInfo = view.findViewById(R.id.userInfo_btnEditInfo);
        btnChangePassword = view.findViewById(R.id.userInfo_btnChangePassword);
        btnLogout = view.findViewById(R.id.userInfo_btnLogout);
        progressBar = view.findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

//        cấu hình firebase
        mAuth = FirebaseAuth.getInstance();
        account = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        myRef = database.getReference("user").child(uid);

//        read database and set value
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                theUser = dataSnapshot.getValue(User.class);
                tvName.setText(theUser.getName());
//                set link image
                Picasso.get().load(theUser.getAvatar()).into(imgAvatar);
                imgAvatar.setClipToOutline(true);

                progressBar.setVisibility(View.GONE);
                imgAvatar.setVisibility(View.VISIBLE);
                btnEditInfo.setEnabled(true);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getActivity(), "Kết nối internet không ổn định", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });

//        bắt sự kiện button
        btnDarkMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setNightMode(getActivity());
            }
        });

        btnEditInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (progressBar.getVisibility() == View.GONE) {
                    Intent intentUpdateUserInfo = new Intent(getActivity(), UpdateUserInfoActivity.class);
                    intentUpdateUserInfo.putExtra("user", theUser);
                    intentUpdateUserInfo.putExtra("uid", uid);
                    getActivity().startActivity(intentUpdateUserInfo);
                } else {
                    Toast.makeText(getActivity(), "Đăng lấy thông tin của bạn vui lòng đợi ...", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (progressBar.getVisibility() == View.GONE) {
                    openChangePasswordDialog(Gravity.CENTER);
                } else {
                    Toast.makeText(getActivity(), "Đăng lấy thông tin của bạn vui lòng đợi ...", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getActivity(), "Đăng xuất thành công", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        });

        return view;
    }

    private Dialog createDialog(int gravity, int layoutId) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layoutId);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            windowAttributes.gravity = gravity;
            window.setAttributes(windowAttributes);
            //        click ra ngoài để tắt
            dialog.setCancelable(true);
            return dialog;
        }
        return null;
    }

    private void openChangePasswordDialog(int gravity) {
        final Dialog dialog = createDialog(gravity, R.layout.layout_dialog_change_password);

        if (dialog == null)
            return;

        EditText edtPassword = dialog.findViewById(R.id.dialogChangePassword_edtPassword);
        EditText edtNewPassword = dialog.findViewById(R.id.dialogChangePassword_edtNewPassword);
        EditText edtReNewPassword = dialog.findViewById(R.id.dialogChangePassword_edtNewRePassword);
        Button btnSave = dialog.findViewById(R.id.dialogChangePassword_btnSave);
        Button btnCancel = dialog.findViewById(R.id.dialogChangePassword_btnCancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = edtPassword.getText().toString().trim();
                String newPassword = edtNewPassword.getText().toString().trim();
                String reNewPassword = edtReNewPassword.getText().toString().trim();

                if (password.isEmpty() || password.length() <= 0) {
                    Toast.makeText(getActivity(), "Mật khẩu không được để trống", Toast.LENGTH_LONG).show();
                    return;
                }
                if (newPassword.isEmpty() || newPassword.length() <= 0) {
                    Toast.makeText(getActivity(), "Mật khẩu mới không được để trống", Toast.LENGTH_LONG).show();
                    return;
                }
                if (reNewPassword.isEmpty() || reNewPassword.length() <= 0) {
                    Toast.makeText(getActivity(), "Mật khẩu mới nhập lại không được để trống", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!newPassword.equals(reNewPassword)) {
                    Toast.makeText(getActivity(), "Mật khẩu mới và mật khẩu nhập lại không giống nhau", Toast.LENGTH_LONG).show();
                    return;
                }
//                xác thực email + password vừa được nhập
                AuthCredential credential = EmailAuthProvider.getCredential(theUser.getEmail(), password);
                account.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    account.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getActivity(), "Cập nhật mật khẩu thành công", Toast.LENGTH_LONG).show();
                                                dialog.dismiss();
                                            } else {
                                                Toast.makeText(getActivity(), "Cập nhật mật khẩu thất bại", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(getActivity(), "Mật khẩu không chính xác", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        showAndHiddenPasswordField(edtPassword, flagHiddenCurrentPassword);
        showAndHiddenPasswordField(edtNewPassword, flagHiddenNewPassword);
        showAndHiddenPasswordField(edtReNewPassword, flagHiddenReNewPassword);

        dialog.show();
    }

    public void setNightMode(Context target) {
        int whiteColor = Color.parseColor("#ffffff");
        int blackColor = Color.parseColor("#000000");
        int seletedColor;
        UiModeManager uiManager = (UiModeManager) target.getSystemService(Context.UI_MODE_SERVICE);

        if (Build.VERSION.SDK_INT <= 22) {
            uiManager.enableCarMode(0);
        }

        if (!isDarkMode) {
            uiManager.setNightMode(UiModeManager.MODE_NIGHT_YES);
            seletedColor = whiteColor;
            isDarkMode = false;
        } else {
            uiManager.setNightMode(UiModeManager.MODE_NIGHT_NO);
            seletedColor = blackColor;
            isDarkMode = true;
        }
        tvName.setTextColor(seletedColor);
        btnDarkMode.setTextColor(seletedColor);
        btnEditInfo.setTextColor(seletedColor);
        btnChangePassword.setTextColor(seletedColor);
        btnLogout.setTextColor(seletedColor);
    }

    private void showAndHiddenPasswordField(EditText edt, boolean flagHiddenPassword) {
        isHiddenPassword = flagHiddenPassword;
        edt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (edt.getRight() - edt.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if (isHiddenPassword) {
                            edt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_hidepass, 0);
                            edt.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            edt.setSelection(edt.length());
                            isHiddenPassword = false;
                        } else {
                            edt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_showpass, 0);
                            edt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            edt.setSelection(edt.length());
                            isHiddenPassword = true;
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }
}