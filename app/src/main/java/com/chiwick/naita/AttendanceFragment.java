package com.chiwick.naita;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AttendanceFragment extends Fragment {

    private FirebaseFirestore db;
    private String course;
    private RecyclerView recyclerView;
    private AttendanceAdapter attendanceAdapter;
    private ArrayList<Map<String, String>> studentsList;

    public AttendanceFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance, container, false);


        db = FirebaseFirestore.getInstance();


        course = getArguments() != null ? getArguments().getString("course") : null;


        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        studentsList = new ArrayList<>();
        attendanceAdapter = new AttendanceAdapter(studentsList);
        recyclerView.setAdapter(attendanceAdapter);


        if (course != null) {
            loadStudents(course);
        } else {
            Toast.makeText(requireActivity(), "Course not provided", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadStudents(String course) {
        db.collection("users")
                .whereEqualTo("role", "student")
                .whereEqualTo("course", course)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(requireActivity(), "Failed to load students: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (value != null) {
                            studentsList.clear();
                            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                            for (QueryDocumentSnapshot document : value) {
                                String studentId = document.getId();
                                String lastAttendance = document.getString("lastAttendance");
                                String name = document.getString("name");


                                Map<String, String> studentData = new HashMap<>();
                                studentData.put("id", studentId);
                                studentData.put("name", name);
                                studentData.put("attendanceStatus", today.equals(lastAttendance) ? "Present" : "Absent");
                                studentsList.add(studentData);
                            }


                            attendanceAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}
