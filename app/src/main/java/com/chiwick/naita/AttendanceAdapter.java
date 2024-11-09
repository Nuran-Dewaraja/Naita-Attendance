package com.chiwick.naita;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Map;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private final ArrayList<Map<String, String>> studentsList;

    public AttendanceAdapter(ArrayList<Map<String, String>> studentsList) {
        this.studentsList = studentsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, String> student = studentsList.get(position);
        holder.studentIdTextView.setText(student.get("name"));
        holder.attendanceStatusTextView.setText(student.get("attendanceStatus"));
    }

    @Override
    public int getItemCount() {
        return studentsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView studentIdTextView;
        TextView attendanceStatusTextView;

        ViewHolder(View itemView) {
            super(itemView);
            studentIdTextView = itemView.findViewById(R.id.studentIdTextView);
            attendanceStatusTextView = itemView.findViewById(R.id.attendanceStatusTextView);
        }
    }
}
