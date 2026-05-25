package com.example.bizvault;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseUtil {

    public static FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    public static FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }

    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static FirebaseFirestore getFirestore() {
        return FirebaseFirestore.getInstance();
    }

    public static CollectionReference getDocumentsCollection() {
        return getFirestore().collection("documents");
    }

    public static FirebaseStorage getStorage() {
        return FirebaseStorage.getInstance();
    }

    public static StorageReference getStorageReference() {
        return getStorage().getReference();
    }

    public static StorageReference getDocumentStorageDir() {
        String userId = getCurrentUserId();
        if (userId == null) return null;
        return getStorageReference().child("documents").child(userId);
    }

    public static void logout() {
        getAuth().signOut();
    }
}