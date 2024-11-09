package com.chiwick.naita;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<Student> studentList;

    private WeakReference<StudentFragment> fragmentRef;

    public StudentAdapter(List<Student> studentList, StudentFragment fragment) {
        this.studentList = studentList;
        this.fragmentRef = new WeakReference<>(fragment);
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.studentNameTextView.setText(student.getName());
        holder.writeNfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StudentFragment fragment = fragmentRef.get();
                if (fragment != null) {
                    fragment.prepareNfcWrite(student.getId());
                } else {

                    Toast.makeText(fragment.requireContext(), "Unable to mark attendance: Fragment not accessible.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView studentNameTextView;
        Button writeNfc;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentNameTextView = itemView.findViewById(R.id.studentNameTextView);
            writeNfc = itemView.findViewById(R.id.writeNfc);
        }
    }
}
