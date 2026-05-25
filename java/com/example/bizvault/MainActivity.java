package com.example.bizvault;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.bizvault.databinding.ActivityMainBinding;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DocumentAdapter adapter;
    private List<Document> documentList;

    // Premium App-Related Image Assets
    private static final String MAIN_BG_URL = "https://images.unsplash.com/photo-1563986768609-322da13575f3?q=80&w=2070&auto=format&fit=crop"; // Secure Digital Dashboard
    private static final String EMPTY_VAULT_URL = "https://images.unsplash.com/photo-1614064641938-3bbee52942c7?q=80&w=2070&auto=format&fit=crop"; // Cyber Security Shield

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load Executive Background
        Glide.with(this).load(MAIN_BG_URL).centerCrop().into(binding.ivMainBg);

        setSupportActionBar(binding.toolbar);

        if (FirebaseUtil.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        documentList = new ArrayList<>();
        adapter = new DocumentAdapter(documentList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddDocumentActivity.class));
        });

        setupSearchAndSorting();
        fetchDocuments();
    }

    private void setupSearchAndSorting() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });

        binding.chipGroupSort.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipDefault) {
                adapter.sortByDefault();
            } else if (checkedId == R.id.chipImportant) {
                adapter.showImportantOnly();
            } else if (checkedId == R.id.chipExpiry) {
                adapter.sortByExpiry();
            }
        });
    }

    private void fetchDocuments() {
        FirebaseUtil.getDocumentsCollection()
                .whereEqualTo("userId", FirebaseUtil.getCurrentUserId())
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        documentList.clear();
                        for (DocumentSnapshot snap : value.getDocuments()) {
                            Document doc = snap.toObject(Document.class);
                            if (doc != null) {
                                doc.setId(snap.getId());
                                documentList.add(doc);
                            }
                        }
                        
                        if (documentList.isEmpty()) {
                            showEmptyState();
                        } else {
                            hideEmptyState();
                        }

                        adapter.updateList(new ArrayList<>(documentList));
                        applyCurrentSort();
                    }
                });
    }

    private void applyCurrentSort() {
        int checkedId = binding.chipGroupSort.getCheckedChipId();
        if (checkedId == R.id.chipImportant) {
            adapter.showImportantOnly();
        } else if (checkedId == R.id.chipExpiry) {
            adapter.sortByExpiry();
        } else {
            adapter.sortByDefault();
        }
    }

    private void showEmptyState() {
        binding.recyclerView.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.VISIBLE);
        Glide.with(this).load(EMPTY_VAULT_URL).centerInside().into(binding.ivEmpty);
    }

    private void hideEmptyState() {
        binding.recyclerView.setVisibility(View.VISIBLE);
        binding.emptyState.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Logout");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 1) {
            FirebaseUtil.logout();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}