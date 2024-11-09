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
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NfcFragment extends Fragment {

    private NfcAdapter mNfcAdapter;
    private String studentIdToWrite;
    private String course;
    private FirebaseFirestore db;

    public NfcFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nfc, container, false);


        db = FirebaseFirestore.getInstance();


        course = getArguments() != null ? getArguments().getString("course") : null;


        mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

        if (mNfcAdapter == null) {
            Toast.makeText(getActivity(), "NFC is not supported on this device", Toast.LENGTH_SHORT).show();
        }


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
        } else {
            Log.d("NFC", "NFC Adapter is null or disabled");
            Toast.makeText(getActivity(), "NFC is not enabled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(getActivity());
    }


    public void handleNfcIntent(Intent intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {

                if (studentIdToWrite == null) {
                    readFromNFC(intent);
                } else {

                    writeToNFC(tag, studentIdToWrite);
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

    private void readFromNFC(Intent intent) {
        Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (messages != null) {
            NdefMessage[] ndefMessages = new NdefMessage[messages.length];
            for (int i = 0; i < messages.length; i++) {
                ndefMessages[i] = (NdefMessage) messages[i];
            }
            NdefRecord record = ndefMessages[0].getRecords()[0];
            byte[] payload = record.getPayload();


            if (payload.length > 3) {
                String text = new String(payload, 3, payload.length - 3);
                markAttendance(text);
            } else {
                Toast.makeText(getActivity(), "NFC payload is too short", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "No NFC message found", Toast.LENGTH_SHORT).show();
        }
    }

    public void markAttendance(String studentId) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        DocumentReference attendanceRef = db.collection("users").document(studentId)
                .collection("attendance").document(today);

        attendanceRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Toast.makeText(getActivity(), "Attendance already marked for today.", Toast.LENGTH_SHORT).show();
            } else {
                Map<String, Object> attendanceData = new HashMap<>();
                attendanceData.put("date", today);
                attendanceData.put("status", "present");

                attendanceRef.set(attendanceData).addOnSuccessListener(aVoid -> {
                    db.collection("users").document(studentId)
                            .update("lastAttendance", today)
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(getActivity(), "Attendance marked", Toast.LENGTH_SHORT).show();
                            });
                });
            }
        });
    }
}
