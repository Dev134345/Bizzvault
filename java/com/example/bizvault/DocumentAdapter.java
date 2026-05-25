package com.example.bizvault;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bizvault.databinding.ItemCategoryHeaderBinding;
import com.example.bizvault.databinding.ItemDocumentBinding;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class DocumentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Object> displayList = new ArrayList<>();
    private List<Document> documentsFull = new ArrayList<>();
    private Context context;

    public DocumentAdapter(List<Document> documents) {
        setNewList(documents);
    }

    private void setNewList(List<Document> documents) {
        this.documentsFull = new ArrayList<>(documents);
        this.displayList = groupByCategory(documents);
        notifyDataSetChanged();
    }

    public void updateList(List<Document> newList) {
        setNewList(newList);
    }

    private List<Object> groupByCategory(List<Document> rawList) {
        Map<String, List<Document>> groupedMap = new TreeMap<>();
        for (Document doc : rawList) {
            String cat = doc.getCategory() == null ? "Other" : doc.getCategory();
            if (!groupedMap.containsKey(cat)) {
                groupedMap.put(cat, new ArrayList<>());
            }
            groupedMap.get(cat).add(doc);
        }

        List<Object> combined = new ArrayList<>();
        for (String category : groupedMap.keySet()) {
            combined.add(category); // Add Header
            combined.addAll(groupedMap.get(category)); // Add Items
        }
        return combined;
    }

    public void filter(String text) {
        List<Document> filteredList = new ArrayList<>();
        for (Document item : documentsFull) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        this.displayList = groupByCategory(filteredList);
        notifyDataSetChanged();
    }

    public void showImportantOnly() {
        List<Document> filteredList = new ArrayList<>();
        for (Document item : documentsFull) {
            if (item.getIsImportant() == 1) {
                filteredList.add(item);
            }
        }
        this.displayList = groupByCategory(filteredList);
        notifyDataSetChanged();
    }

    public void sortByExpiry() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        List<Document> sorted = new ArrayList<>(documentsFull);
        Collections.sort(sorted, (d1, d2) -> {
            try {
                Date date1 = sdf.parse(d1.getExpiry());
                Date date2 = sdf.parse(d2.getExpiry());
                if (date1 == null) return 1;
                if (date2 == null) return -1;
                return date1.compareTo(date2);
            } catch (ParseException e) {
                return 0;
            }
        });
        this.displayList = groupByCategory(sorted);
        notifyDataSetChanged();
    }

    public void sortByRecentlyUploaded() {
        List<Document> sorted = new ArrayList<>(documentsFull);
        Collections.sort(sorted, (d1, d2) -> Long.compare(d2.getTimestamp(), d1.getTimestamp()));
        this.displayList = groupByCategory(sorted);
        notifyDataSetChanged();
    }

    public void sortByDefault() {
        List<Document> sorted = new ArrayList<>(documentsFull);
        Collections.sort(sorted, (d1, d2) -> {
            if (d1.getIsImportant() != d2.getIsImportant()) {
                return Integer.compare(d2.getIsImportant(), d1.getIsImportant());
            }
            return d1.getName().compareToIgnoreCase(d2.getName());
        });
        this.displayList = groupByCategory(sorted);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return displayList.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        if (viewType == TYPE_HEADER) {
            ItemCategoryHeaderBinding binding = ItemCategoryHeaderBinding.inflate(LayoutInflater.from(context), parent, false);
            return new HeaderViewHolder(binding);
        } else {
            ItemDocumentBinding binding = ItemDocumentBinding.inflate(LayoutInflater.from(context), parent, false);
            return new ItemViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).binding.tvHeaderName.setText((String) displayList.get(position));
        } else {
            bindItem((ItemViewHolder) holder, (Document) displayList.get(position));
        }
    }

    private void bindItem(ItemViewHolder holder, Document doc) {
        holder.binding.tvDocName.setText(doc.getName());
        holder.binding.tvCategory.setText(doc.getCategory());
        holder.binding.tvExpiry.setText("Expires: " + doc.getExpiry());

        if (doc.getIsImportant() == 1) {
            holder.binding.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.type_important));
            holder.binding.tvDocName.setTextColor(ContextCompat.getColor(context, R.color.accent_cyan));
            holder.binding.ivImportant.setVisibility(View.VISIBLE);
            holder.binding.ivImportant.setColorFilter(ContextCompat.getColor(context, R.color.accent_cyan));
        } else {
            holder.binding.tvDocName.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            holder.binding.ivImportant.setVisibility(View.GONE);
            
            String category = doc.getCategory().toLowerCase();
            switch (category) {
                case "license": holder.binding.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.type_license)); break;
                case "id": holder.binding.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.type_id)); break;
                case "passport": holder.binding.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.type_passport)); break;
                case "invoice": holder.binding.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.type_invoice)); break;
                case "certificate": holder.binding.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.type_certificate)); break;
                default: holder.binding.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface_card)); break;
            }
        }

        if (doc.getPath() != null && !doc.getPath().isEmpty()) {
            String firstUrl = doc.getPath().split("\\|")[0];
            if (firstUrl.toLowerCase().endsWith(".pdf")) {
                holder.binding.ivThumbnail.setImageResource(android.R.drawable.ic_menu_save);
                holder.binding.ivThumbnail.setPadding(10, 10, 10, 10);
            } else {
                holder.binding.ivThumbnail.setPadding(0, 0, 0, 0);
                Glide.with(context).load(firstUrl).placeholder(R.mipmap.ic_launcher).into(holder.binding.ivThumbnail);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (doc.getPath().split("\\|")[0].toLowerCase().endsWith(".pdf")) {
                openPdf(doc.getPath().split("\\|")[0]);
            } else {
                Intent intent = new Intent(context, ViewDetailsActivity.class);
                intent.putExtra("docId", doc.getId());
                context.startActivity(intent);
            }
        });

        holder.binding.ivDelete.setOnClickListener(v -> showDeleteConfirmation(doc));
    }

    private void openPdf(String path) {
        try {
            Uri uri = Uri.parse(path);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(intent, "Open PDF with..."));
        } catch (Exception e) {
            Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation(Document doc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_delete_confirm, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnConfirmDelete).setOnClickListener(v -> {
            FirebaseUtil.getDocumentsCollection().document(doc.getId()).delete()
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Asset Purged", Toast.LENGTH_SHORT).show());
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ItemDocumentBinding binding;
        public ItemViewHolder(ItemDocumentBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        ItemCategoryHeaderBinding binding;
        public HeaderViewHolder(ItemCategoryHeaderBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}