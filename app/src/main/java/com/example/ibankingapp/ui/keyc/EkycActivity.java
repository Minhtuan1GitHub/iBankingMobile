package com.example.ibankingapp.ui.keyc;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.databinding.ActivityEkycBinding;
import com.example.ibankingapp.utils.CopyImage;
import com.example.ibankingapp.viewModel.keyc.KeycViewModel;

public class EkycActivity extends AppCompatActivity {
    private ActivityEkycBinding binding;
    private KeycViewModel viewModel;
    private static final int PICK_IMAGE = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEkycBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(KeycViewModel.class);

        binding.buttonSelectFace.setOnClickListener(v->selectImage());
        binding.buttonVerifyEkyc.setOnClickListener(v->viewModel.verifyEkyc());

        observeViewModel();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            try {
                Uri pickedUri = data.getData();

                // 👉 COPY về cache
                Uri safeUri = CopyImage.copyUriToCache(this, pickedUri);

                binding.imageFace.setImageURI(safeUri);
                viewModel.setFaceImage(safeUri);

            } catch (Exception e) {
                Toast.makeText(this, "Không đọc được ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void observeViewModel() {
        viewModel.getLoading().observe(this, loading -> {
            binding.buttonVerifyEkyc.setEnabled(!loading);

        });


        viewModel.getResult().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "eKYC thành công", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Xác thực thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
