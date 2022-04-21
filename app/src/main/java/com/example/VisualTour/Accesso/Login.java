package com.example.VisualTour.Accesso;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.VisualTour.Main.MainActivity;
import com.example.VisualTour.POI.privatePOI;
import com.example.VisualTour.R;
import com.example.VisualTour.RequestHttp;
import com.example.VisualTour.databinding.ActivityMainBinding;
import com.example.VisualTour.databinding.LoginBinding;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;

public class Login extends Fragment {
    private EditText Utente;
    private EditText Password;
    private Button Registra;
    private LoginBinding binding;
    public static boolean RequestLogin=false;
    public static String RequestOrigin=null;
    private ActivityMainBinding bindingActivity;

    @Override
    public void onDestroy() {
        RequestLogin=false;
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String str=null;
        str= (String) sharedPref.getAll().get("NomeUtente");
        if (str!=null){
            NavHostFragment.findNavController(Login.this)
                    .navigate(R.id.action_login_to_account);
        }
        if(RequestOrigin=="POI" && !RequestLogin){
            NavHostFragment.findNavController(Login.this)
                    .navigate(R.id.action_login_to_publicPOI);
        }
           AppBarConfiguration mAppBarConfiguration;
        bindingActivity = ActivityMainBinding.inflate(getLayoutInflater());





        binding = LoginBinding.inflate(inflater, container, false);
        Utente= binding.UtenteBox;
        Password= binding.PasswordBox;
         return binding.getRoot();

    }
    public static final String FILENAME = "my_file_name";
    String siteName = "Datrevo";
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.Registra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(Login.this)
                        .navigate(R.id.action_login_to_registrazione);
            }
        });


        binding.Login.setOnClickListener(view1 -> {

                RequestHttp richiesta=new RequestHttp();

            String str= null;
            try {
                str = richiesta.richiestaLogin(Utente.getText().toString(), Password.getText().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            switch (str) {
                        case "Account non registrato":
                            binding.Errori.setVisibility(View.VISIBLE);
                            binding.Errori.setText("Account non registrato");
                            break;
                        case "Nome Utente o password errati":
                            binding.Errori.setVisibility(View.VISIBLE);
                            binding.Errori.setText("Nome Utente o password errati");

                            break;
                        case "Nome Utente e Password validi":
                            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                            sharedPref.edit().clear().apply();
                            sharedPref.edit().putString("NomeUtente",Utente.getText().toString()).apply();
                            sharedPref.getAll();
                            if(!RequestLogin){
                            NavHostFragment.findNavController(Login.this)
                                    .navigate(R.id.action_login_to_account);
                                }else{
                                    if(RequestOrigin.equals("POI")){
                                NavHostFragment.findNavController(Login.this)
                                        .navigate(R.id.action_login_to_privatePOI);
                                    }else if (RequestOrigin.equals("Mappe")){
                                        NavHostFragment.findNavController(Login.this)
                                                .navigate(R.id.action_login_to_privateMap);
                                    }
                            }
                            break;
                    }



        });

    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();

    }

}