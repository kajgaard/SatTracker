package com.example.sattracker.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.sattracker.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private FragmentProfileBinding binding;

    private  EditText editTextView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        editTextView = binding.editTextHours;
        //profileViewModel.getHours().observe(getViewLifecycleOwner(),(Observer<? super Double>) editTextView);
        //editTextView.setOnClickListener(this);


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        //editTextView.getText().clear(); //or you can use editText.setText("");
    }
}