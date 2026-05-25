package com.example.bizvault;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bizvault.databinding.ActivityViewDetailsBinding;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Arrays;
import java.util.List;

public class ViewDetailsActivity extends AppCompatActivity {

    private ActivityViewDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }

        String docId = getIntent().getStringExtra("docId");
        if (docId != null) {
            loadDocumentDetails(docId);
        } else {
            Toast.makeText(this, "Error loading document", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadDocumentDetails(String docId) {
        FirebaseUtil.getDocumentsCollection().document(docId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Document doc = documentSnapshot.toObject(Document.class);
                    if (doc != null) {
                        binding.tvDetailName.setText(doc.getName());
                        binding.tvDetailCategory.setText(doc.getCategory());
                        binding.tvDetailExpiry.setText("Expires: " + doc.getExpiry());

                        if (doc.getPath() != null && !doc.getPath().isEmpty()) {
                            List<String> urls = Arrays.asList(doc.getPath().split("\\|"));
                            ImagePagerAdapter adapter = new ImagePagerAdapter(urls);
                            binding.viewPager.setAdapter(adapter);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ViewDetailsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}