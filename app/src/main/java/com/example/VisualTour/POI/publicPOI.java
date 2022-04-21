package com.example.VisualTour.POI;

import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.VisualTour.Accesso.Login;
import com.example.VisualTour.CourseModel;
import com.example.VisualTour.R;
import com.example.VisualTour.RequestHttp;
import com.example.VisualTour.databinding.CardBinding;
import com.example.VisualTour.databinding.DetailsPoiBinding;
import com.example.VisualTour.databinding.PublicPoiBinding;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.ArrayList;


public class publicPOI extends Fragment implements CourseAdapterPOI.ItemClickListener {
    private PublicPoiBinding binding;
    private RecyclerView courseRV;
    private CardBinding cb;
    private CourseAdapterPOI mAdapter;
    // Arraylist for storing data
    private ArrayList<CourseModel> courseModelArrayList;
    private  DetailsPoiBinding bindingdet;
    private static String str=null;
    private static JSONArray jArray = null;

    @Override
    public void onDestroyView() {

        super.onDestroyView();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = PublicPoiBinding.inflate(inflater, container, false);
        bindingdet= DetailsPoiBinding.inflate(inflater, container, false);
        cb= CardBinding.inflate(inflater, container, false);

        courseRV = binding.idRVCourse;
        RequestHttp rq=new RequestHttp();

        courseModelArrayList = new ArrayList<>();

        try {
              str= rq.richiesta(null,"POI");
            jArray = (JSONArray) new JSONTokener(str).nextValue();
            for(int i=0; i<= jArray.length();i++){
                courseModelArrayList.add(new CourseModel(jArray.getJSONObject(i).getString("Nome"), 4, R.drawable.gf));
            }

            } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        CourseAdapterPOI courseAdapter = new CourseAdapterPOI(courseModelArrayList, this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        // in below two lines we are setting layoutmanager and adapter to our recycler view.
        courseRV.setLayoutManager(linearLayoutManager);
        courseRV.setAdapter(courseAdapter);
        setButton();
        return binding.getRoot();

    }



    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);



        //courseRV.setOnClickListener(v -> System.out.println("m"));


    }

    private void setButton(){
        binding.privatePOI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                str= (String) sharedPref.getAll().get("NomeUtente");
                if (str==null){
                    Login.RequestLogin=true;
                    Login.RequestOrigin="POI";
                    NavHostFragment.findNavController(publicPOI.this)
                            .navigate(R.id.action_publicPOI_to_login);
                }else{
                    NavHostFragment.findNavController(publicPOI.this)
                            .navigate(R.id.action_publicPOI_to_privatePOI);
                }
            }
        });
    }

    @Override
    public  void onDestroy(){
        super.onDestroy();

    }

    @Override
    public void onStart() {

        super.onStart();


    }

    @Override
    public void onResume() {

        super.onResume();

    }
    @Override
    public void onPause() {

        super.onPause();

    }
    @Override
    public void onStop() {

        super.onStop();


    }
    @Override
    public void onLowMemory() {

        super.onLowMemory();

    }
    @Override
    public void onSaveInstanceState(Bundle out) {

        super.onSaveInstanceState(out);

    }

    @Override
    public void onItemClick(CourseModel dataModel, String id) throws JSONException {

        NavHostFragment.findNavController(publicPOI.this)
                        .navigate(R.id.action_publicPOI_to_dettagli);
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        DetailsPOI.OriginRequest="Public";

        int i;
        for (i=0; i<=jArray.length(); i++){// apertura dettagli e passaggio valori a dettagli
            if (jArray.getJSONObject(i).getString("Nome").equals(id)){
                sharedPref.edit().putString("ID",jArray.getJSONObject(i).getString("ID")).apply();
                sharedPref.edit().putString("Nome",jArray.getJSONObject(i).getString("Nome")).apply();
                sharedPref.edit().putString("Tipo",jArray.getJSONObject(i).getString("Tipo")).apply();
                sharedPref.edit().putString("Latitudine",jArray.getJSONObject(i).getString("Latitudine")).apply();
                sharedPref.edit().putString("Longitudine",jArray.getJSONObject(i).getString("Longitudine")).apply();
                sharedPref.edit().putString("Comune",jArray.getJSONObject(i).getString("Comune")).apply();
                sharedPref.edit().putString("Provincia",jArray.getJSONObject(i).getString("Provincia")).apply();
                sharedPref.edit().putString("Regione",jArray.getJSONObject(i).getString("Regione")).apply();
                sharedPref.edit().putString("Descrizione",jArray.getJSONObject(i).getString("Descrizione")).apply();
                 break;
            }
        }
         /*
        Fragment fragment = DetailsPOI.newInstance(dataModel.getTitle());


        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
         transaction.replace(R.id.nav_host_fragment_content_main, fragment, "detail_fragment");

        //transaction.hide(getActivity().getSupportFragmentManager().findFragmentByTag("main_fragment"));
        //transaction.add(R.id.prova2, fragment);
        transaction.addToBackStack(null);
        transaction.commit();*/
    }
}
