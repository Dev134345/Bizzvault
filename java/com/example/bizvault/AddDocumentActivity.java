package com.example.bizvault;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.bizvault.databinding.ActivityAddDocumentBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddDocumentActivity extends AppCompatActivity {

    private ActivityAddDocumentBinding binding;
    private final List<Uri> assetUris = new ArrayList<>();
    private Uri currentPhotoUri;
    private static final int PERMISSION_REQUEST_CODE = 101;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    assetUris.add(currentPhotoUri);
                    addThumbnail(currentPhotoUri, false);
                }
            }
    );

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(),
            uris -> {
                if (uris != null) {
                    for (Uri uri : uris) {
                        assetUris.add(uri);
                        addThumbnail(uri, false);
                    }
                }
            }
    );

    private final ActivityResultLauncher<String[]> pdfLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenMultipleDocuments(),
            uris -> {
                if (uris != null) {
                    for (Uri uri : uris) {
                        assetUris.add(uri);
                        addThumbnail(uri, true);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityAddDocumentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupCategorySpinner();
        requestInitialPermissions();
        
        binding.etExpiry.setOnClickListener(v -> showDatePicker());
        binding.btnAddAttachment.setOnClickListener(v -> showAttachmentOptions());
        binding.btnSave.setOnClickListener(v -> startSecureSaveProcess());
    }

    private void showAttachmentOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_attachment_options, null);
        builder.setView(view);
        
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        view.findViewById(R.id.optionCamera).setOnClickListener(v -> {
            checkCameraPermission();
            dialog.dismiss();
        });

        view.findViewById(R.id.optionGallery).setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
            dialog.dismiss();
        });

        view.findViewById(R.id.optionPdf).setOnClickListener(v -> {
            pdfLauncher.launch(new String[]{"application/pdf"});
            dialog.dismiss();
        });

        dialog.show();
    }

    private void requestInitialPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void setupCategorySpinner() {
        String[] categories = {"License", "ID", "Passport", "Invoice", "Certificate", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        binding.actCategory.setAdapter(adapter);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            binding.etExpiry.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            photoFile = File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        } catch (IOException ignored) {}

        if (photoFile != null) {
            currentPhotoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
            cameraLauncher.launch(takePictureIntent);
        }
    }

    private void addThumbnail(Uri uri, boolean isPdf) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_thumbnail, binding.layoutThumbnails, false);
        ImageView iv = view.findViewById(R.id.ivThumb);
        if (isPdf) {
            iv.setImageResource(android.R.drawable.ic_menu_save);
            iv.setPadding(20, 20, 20, 20);
        } else {
            Glide.with(this).load(uri).into(iv);
        }
        binding.layoutThumbnails.addView(view, binding.layoutThumbnails.getChildCount() - 1);
    }

    private void startSecureSaveProcess() {
        String name = binding.etDocName.getText().toString().trim();
        String category = binding.actCategory.getText().toString();
        
        if (name.isEmpty() || category.isEmpty() || assetUris.isEmpty()) {
            Toast.makeText(this, "Missing info or assets", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.loadingOverlay.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);

        new Thread(() -> {
            try {
                StringBuilder localPaths = new StringBuilder();
                for (Uri uri : assetUris) {
                    Uri savedUri = saveToInternalStorage(uri);
                    if (localPaths.length() > 0) localPaths.append("|");
                    localPaths.append(savedUri.toString());
                }

                runOnUiThread(() -> finalizeInstantSave(localPaths.toString()));
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    binding.loadingOverlay.setVisibility(View.GONE);
                    binding.btnSave.setEnabled(true);
                    Toast.makeText(this, "Vault Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private Uri saveToInternalStorage(Uri uri) throws IOException {
        String type = getContentResolver().getType(uri);
        String extension = type != null && type.contains("pdf") ? ".pdf" : ".jpg";
        String fileName = "VAULT_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000) + extension;
        
        File docDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (docDir != null && !docDir.exists()) docDir.mkdirs();
        
        File file = new File(docDir, fileName);
        try (InputStream is = getContentResolver().openInputStream(uri);
             FileOutputStream os = new FileOutputStream(file)) {
            if (is == null) throw new IOException("Source asset unavailable");
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
    }

    private void finalizeInstantSave(String urls) {
        String name = binding.etDocName.getText().toString().trim();
        String category = binding.actCategory.getText().toString();
        String expiry = binding.etExpiry.getText().toString();
        boolean isImportant = binding.switchImportant.isChecked();
        long timestamp = System.currentTimeMillis();

        Document doc = new Document(FirebaseUtil.getCurrentUserId(), name, category, expiry, urls, isImportant ? 1 : 0, timestamp);
        FirebaseUtil.getDocumentsCollection().add(doc);
        
        try { scheduleReminders(expiry, name, isImportant); } catch (Exception ignored) {}
        Toast.makeText(this, "Asset Secured in Vault", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void scheduleReminders(String expiryDate, String docName, boolean isImportant) {
        if (expiryDate == null || expiryDate.isEmpty()) return;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(expiryDate);
            if (date != null) {
                for (int i = 3; i >= 0; i--) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendar.add(Calendar.DAY_OF_YEAR, -i);
                    calendar.set(Calendar.HOUR_OF_DAY, 12);
                    calendar.set(Calendar.MINUTE, 0);

                    if (calendar.getTimeInMillis() > System.currentTimeMillis()) {
                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        Intent intent = new Intent(this, ReminderReceiver.class);
                        intent.putExtra("docName", docName);
                        intent.putExtra("daysLeft", i);
                        int requestCode = (docName.hashCode() + i);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);

                        if (alarmManager != null) {
                            if (isImportant && i <= 1) {
                                Intent showIntent = new Intent(this, MainActivity.class);
                                PendingIntent showPendingIntent = PendingIntent.getActivity(this, requestCode, showIntent, PendingIntent.FLAG_IMMUTABLE);
                                AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), showPendingIntent);
                                alarmManager.setAlarmClock(info, pendingIntent);
                            } else {
                                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                            }
                        }
                    }
                }
            }
        } catch (ParseException ignored) {}
    }
}