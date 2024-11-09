package com.chiwick.naita;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class ProfileFragment extends Fragment {

    private TextView nameTextView;
    private TextView emailTextView;
    private TextView courseTextView;
    private FirebaseFirestore db;
    private String userId;

    private SharedPreferences sharedPreferences;

    private Button logoutButton;


    public ProfileFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        courseTextView = view.findViewById(R.id.courseTextView);
        logoutButton = view.findViewById(R.id.logoutButton);


        db = FirebaseFirestore.getInstance();


        userId = getArguments() != null ? getArguments().getString("id") : null;


        if (userId != null) {
            loadUserData(userId);
        } else {
            Toast.makeText(requireActivity(), "User ID is not provided", Toast.LENGTH_SHORT).show();
        }

        sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        logoutButton.setOnClickListener(v -> logout());


        return view;
    }

    private void loadUserData(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    String email = documentSnapshot.getString("email");
                    String course = documentSnapshot.getString("course");


                    nameTextView.setText(name);
                    emailTextView.setText(email);
                    courseTextView.setText(course);
                } else {
                    Toast.makeText(requireActivity(), "User not found", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireActivity(), "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void logout() {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();


        Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();


        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}
