package com.example.rythm;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class EditPlayListFragment extends Fragment {

    private Button btnSavePlayListData;


    public EditPlayListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_play_list, container, false);
        this.btnSavePlayListData = view.findViewById(R.id.btnSavePlayListData);

        this.btnSavePlayListData.setOnClickListener(v -> {

        });
        return view;
    }
}