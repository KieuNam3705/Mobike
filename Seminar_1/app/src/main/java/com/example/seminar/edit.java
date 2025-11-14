package com.example.seminar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.imageview.ShapeableImageView;

public class edit extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ShapeableImageView avatar;
    private Uri currentImageUri;

    EditText nameedt, emailedt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        avatar = findViewById(R.id.avatar);

        nameedt = (EditText) findViewById(R.id.nameedt);
        emailedt = (EditText) findViewById(R.id.emailedt);

        Button savebtn = (Button) findViewById(R.id.savedbtn);
        Button editimagebtn = (Button) findViewById(R.id.editimagebtn);
        savebtn.setOnClickListener(savedClick);
        editimagebtn.setOnClickListener(selectImage);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            String getName = bundle.getString("nameKey");
            String getEmail = bundle.getString("emailKey");
            String getImage = bundle.getString("imageKey");

            nameedt.setText(getName);
            emailedt.setText(getEmail);
            if (getImage != null ){
                currentImageUri = Uri.parse(getImage);
                avatar.setImageURI(currentImageUri);
            }
        }
    }

    private View.OnClickListener savedClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent savedInfo = new Intent(getApplicationContext(), MainActivity.class);
            Bundle savedBundle = new Bundle();
            savedBundle.putString("savedName", nameedt.getText().toString());
            savedBundle.putString("savedEmail", emailedt.getText().toString());
            if (currentImageUri != null) {
                savedBundle.putString("changedImage", currentImageUri.toString());
            }
            savedInfo.putExtras(savedBundle);
            setResult(RESULT_OK, savedInfo);
            Toast.makeText(edit.this, "Đã lưu thông tin", Toast.LENGTH_SHORT).show();
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    };

    private View.OnClickListener selectImage = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            currentImageUri = data.getData();
            try {
                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                getContentResolver().takePersistableUriPermission(currentImageUri, takeFlags);
                avatar.setImageURI(currentImageUri);
            } catch (SecurityException e) {
                Toast.makeText(this, "Không có quyền truy cập ảnh", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode != RESULT_OK) {
            Toast.makeText(this, "Chưa chọn ảnh nào", Toast.LENGTH_SHORT).show();
        }
    }

}