package com.chiwick.naita;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentFragment extends Fragment {

    private RecyclerView studentRecyclerView;
    private StudentAdapter studentAdapter;
    private List<Student> studentList = new ArrayList<>();
    private FirebaseFirestore db;
    private String course;
    private NfcAdapter mNfcAdapter;
    private String studentIdToWrite;

    private LinearLayoutManager layoutManager;

    public StudentFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student, container, false);

        studentRecyclerView = view.findViewById(R.id.studentRecyclerView);
        db = FirebaseFirestore.getInstance();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

        layoutManager = new LinearLayoutManager(requireContext());
        studentRecyclerView.setLayoutManager(layoutManager);
        studentAdapter = new StudentAdapter(studentList, StudentFragment.this);
        studentRecyclerView.setAdapter(studentAdapter);

        if (mNfcAdapter == null) {
            Toast.makeText(getActivity(), "NFC is not supported on this device", Toast.LENGTH_SHORT).show();
            return view;
        }


        course = getArguments() != null ? getArguments().getString("course") : null;

        loadStudents();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
            IntentFilter[] nfcIntentFilter = new IntentFilter[]{tagDetected};

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getActivity(), 0, new Intent(getActivity(), getActivity().getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
            mNfcAdapter.enableForegroundDispatch(getActivity(), pendingIntent, nfcIntentFilter, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(getActivity());
        }
    }

    public void handleNfcIntent(Intent intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {

                if (studentIdToWrite != null) {
                    writeToNFC(tag, studentIdToWrite);
                } else {

                    Toast.makeText(getActivity(), "No student ID set for writing to NFC", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void prepareNfcWrite(String studentId) {
        studentIdToWrite = studentId;
        Toast.makeText(getActivity(), "Tap NFC tag to write student ID: " + studentId, Toast.LENGTH_SHORT).show();
    }

    private void writeToNFC(Tag tag, String studentId) {
        NdefMessage ndefMessage = createNdefMessage(studentId);
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (ndef.isWritable()) {
                    ndef.writeNdefMessage(ndefMessage);
                    Toast.makeText(getActivity(), "NFC tag written successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "NFC tag is not writable", Toast.LENGTH_SHORT).show();
                }
                ndef.close();
            } else {
                Toast.makeText(getActivity(), "NFC format is not supported", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Failed to write NFC tag", Toast.LENGTH_SHORT).show();
        }
    }

    private NdefMessage createNdefMessage(String text) {
        NdefRecord ndefRecord = NdefRecord.createTextRecord("en", text);
        return new NdefMessage(new NdefRecord[]{ndefRecord});
    }

    private void loadStudents() {
        db.collection("users")
                .whereEqualTo("role", "student")
                .whereEqualTo("course", course)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        studentList.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Student student = document.toObject(Student.class);
                            if (student != null) {
                                studentList.add(student);
                            }
                        }
                        studentAdapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to load students", Toast.LENGTH_SHORT).show();
                });
    }
}
