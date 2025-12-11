package com.example.seminar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class edit extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_MEDIA_PERMISSION = 101;

    private ShapeableImageView avatar;
    private Uri currentImageUri;
    private String currentPhotoPath;

    EditText nameedt, emailedt;

    // ActivityResultLauncher cho chụp ảnh
    private ActivityResultLauncher<Intent> takePictureLauncher;

    // ActivityResultLauncher cho chọn ảnh từ gallery
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        avatar = findViewById(R.id.avatar);
        nameedt = (EditText) findViewById(R.id.nameedt);
        emailedt = (EditText) findViewById(R.id.emailedt);

        // Khởi tạo ActivityResultLauncher cho chụp ảnh
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            // Ảnh đã được chụp và lưu tại currentImageUri
                            if (currentImageUri != null) {
                                avatar.setImageURI(currentImageUri);
                            }
                        } else {
                            Toast.makeText(edit.this, "Chụp ảnh thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Khởi tạo ActivityResultLauncher cho chọn ảnh từ gallery
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            currentImageUri = result.getData().getData();
                            try {
                                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                                getContentResolver().takePersistableUriPermission(currentImageUri, takeFlags);
                                avatar.setImageURI(currentImageUri);
                            } catch (SecurityException e) {
                                Toast.makeText(edit.this, "Không có quyền truy cập ảnh", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(edit.this, "Chưa chọn ảnh nào", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        Button savebtn = (Button) findViewById(R.id.savedbtn);
        Button editimagebtn = (Button) findViewById(R.id.editimagebtn);
        savebtn.setOnClickListener(savedClick);
        editimagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickerDialog();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String getName = bundle.getString("nameKey");
            String getEmail = bundle.getString("emailKey");
            String getImage = bundle.getString("imageKey");

            nameedt.setText(getName);
            emailedt.setText(getEmail);
            if (getImage != null) {
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

    // Dialog cho phép người dùng chọn: Chụp ảnh hoặc Chọn từ thư viện
    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ảnh đại diện");
        builder.setItems(new CharSequence[]{"Chụp ảnh mới", "Chọn từ thư viện"}, (dialog, which) -> {
            switch (which) {
                case 0: // Chụp ảnh
                    dispatchTakePictureIntent();
                    break;
                case 1: // Chọn từ thư viện
                    chooseImageFromGallery();
                    break;
            }
        });
        builder.show();
    }

    // Chọn ảnh từ thư viện - sử dụng ActivityResultLauncher
    private void chooseImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        pickImageLauncher.launch(intent);
    }

    // Chụp ảnh mới - sử dụng ActivityResultLauncher
    private void dispatchTakePictureIntent() {
        // Kiểm tra quyền đọc media (cho Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_MEDIA_PERMISSION);
                return;
            }
        }

        // Kiểm tra quyền camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
            return;
        }

        // Tạo file ảnh
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Lỗi tạo file ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoFile != null) {
            Uri photoUri = FileProvider.getUriForFile(this,
                    "com.example.seminar.fileprovider",
                    photoFile);
            currentImageUri = photoUri;

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            takePictureLauncher.launch(takePictureIntent);
        }
    }

    // Tạo file ảnh mới
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();  // Mở lại camera sau khi cấp quyền
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền Camera", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_MEDIA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();  // Mở lại camera sau khi cấp quyền READ_MEDIA
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền truy cập ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }
}