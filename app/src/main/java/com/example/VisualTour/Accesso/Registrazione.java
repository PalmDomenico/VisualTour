package com.example.VisualTour.Accesso;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.VisualTour.R;
import com.example.VisualTour.RequestHttp;
import com.example.VisualTour.databinding.RegistrazioneBinding;

public class Registrazione extends Fragment {

    private RegistrazioneBinding binding;

    private EditText Email;
    private EditText Password;
    private EditText NomeUtente;
    private TextView ErrorReg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = RegistrazioneBinding.inflate(inflater, container, false);

        Email=binding.EmailBox;
        Password= binding.PasswordBoxReg;
        NomeUtente=binding.NomeBox;
        ErrorReg=binding.ErrorReg;


        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        binding.LoginReg.setOnClickListener(new View.OnClickListener() {
            String ret="";
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(Registrazione.this)
                        .navigate(R.id.action_registrazione_to_login);
            }
        });

        super.onViewCreated(view, savedInstanceState);
         binding.Registrazione.setOnClickListener(new View.OnClickListener() {
             String ret="";
            @Override
            public void onClick(View view) {
                        RequestHttp richiesta=new RequestHttp();
                        try  {

                            ret=richiesta.richiestaRegistrazione(NomeUtente.getText().toString(),Email.getText().toString(),Password.getText().toString());
                            System.out.println(ret);
                            switch (ret) {
                                case "true":
                                    binding.ErrorReg.setVisibility(View.VISIBLE);
                                    ErrorReg.setText("Account registrato");
                                    break;
                                default:
                                    binding.ErrorReg.setVisibility(View.VISIBLE);
                                    ErrorReg.setText(ret);
                                    Password.setText("");
                                    NomeUtente.setText("");
                                    Email.setText("");
                                    break;
                            }



                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }



        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}