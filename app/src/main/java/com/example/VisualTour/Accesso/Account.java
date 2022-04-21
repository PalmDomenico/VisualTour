package com.example.VisualTour.Accesso;

import androidx.fragment.app.Fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.VisualTour.R;
import com.example.VisualTour.databinding.AccountBinding;
import com.example.VisualTour.databinding.RegistrazioneBinding;


public class Account extends Fragment {

    private AccountBinding binding;
    private Button btn;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = AccountBinding.inflate(inflater, container, false);
        btn= binding.Disconetti;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                sharedPref.edit().clear().apply();
                NavHostFragment.findNavController(Account.this)
                        .navigate(R.id.action_account_to_login);
            }
        });
        // Inflate the layout for this fragment
        return binding.getRoot();
    }
}
