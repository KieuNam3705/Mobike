package com.example.seminar;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.imageview.ShapeableImageView;

public class MainActivity extends AppCompatActivity {

    private Uri currentImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView nametxt = findViewById(R.id.nametxt);
        TextView emailtxt = findViewById(R.id.emailtxt);
        ShapeableImageView avatar = findViewById(R.id.avatar);
        Button editbtn = findViewById(R.id.editbtn);

        Bundle getBundle = getIntent().getExtras();
        if (getBundle != null){
            String getName = getBundle.getString("savedName");
            String getEmail = getBundle.getString("savedEmail");
            String getImage = getBundle.getString("changedImage");

            nametxt.setText(getName);
            emailtxt.setText(getEmail);
            if(getImage != null){
                currentImageUri = Uri.parse(getImage);
                avatar.setImageURI(currentImageUri);
            }
        }

        editbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getInfo = new Intent(getApplicationContext(), edit.class);
                Bundle sendBundle = new Bundle();
                sendBundle.putString("nameKey", nametxt.getText().toString());
                sendBundle.putString("emailKey", emailtxt.getText().toString());
                if (currentImageUri != null) {
                    sendBundle.putString("imageKey", currentImageUri.toString());
                }
                getInfo.putExtras(sendBundle);

                startActivityForResult(getInfo, 100);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Bundle bundle = data.getExtras(); // Nên lấy dữ liệu từ Bundle
            if (bundle != null) {
                String name = bundle.getString("savedName");
                String email = bundle.getString("savedEmail");
                String image = bundle.getString("changedImage");

                if (name != null) ((TextView) findViewById(R.id.nametxt)).setText(name);
                if (email != null) ((TextView) findViewById(R.id.emailtxt)).setText(email);
                if (image != null) {
                    currentImageUri = Uri.parse(image);
                    ((ShapeableImageView) findViewById(R.id.avatar)).setImageURI(currentImageUri);
                }
            }
        }
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

}