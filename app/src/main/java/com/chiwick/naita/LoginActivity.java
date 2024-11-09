package com.chiwick.naita;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        db = FirebaseFirestore.getInstance();


        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);


        if (areCredentialsSaved()) {

            String course = sharedPreferences.getString("course", "");
            String id = sharedPreferences.getString("id", "");

            goToHomeActivity(course, id);
            return;
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                loginLecturer(email, password);
            }
        });
    }

    private boolean areCredentialsSaved() {
        String id = sharedPreferences.getString("id", null);
        String email = sharedPreferences.getString("email", null);
        String password = sharedPreferences.getString("password", null);
        String role = sharedPreferences.getString("role", null);
        String course = sharedPreferences.getString("course", null);


        return (id != null && !id.isEmpty()) &&
                (email != null && !email.isEmpty()) &&
                (password != null && !password.isEmpty()) &&
                (role != null && !role.isEmpty()) &&
                (course != null && !course.isEmpty());
    }

    private void saveCredentials(String id, String email, String password, String role, String course) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("id", id);
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putString("role", role);
        editor.putString("course", course);
        editor.apply();
    }

    private void loginLecturer(String email, String password) {
        db.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .whereEqualTo("role", "lecturer")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot lecturer = queryDocumentSnapshots.getDocuments().get(0);
                            String course = lecturer.getString("course");
                            String role = lecturer.getString("role");
                            String id = lecturer.getString("id");


                            saveCredentials(id, email, password, role, course);

                            goToHomeActivity(course, id);
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void goToHomeActivity(String course, String id) {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.putExtra("course", course);
        intent.putExtra("id", id);
        startActivity(intent);
        finish();
    }
}
