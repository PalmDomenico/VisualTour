package com.example.VisualTour.POI;

 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
 import androidx.fragment.app.Fragment;
 import androidx.navigation.fragment.NavHostFragment;
 import androidx.recyclerview.widget.LinearLayoutManager;
 import androidx.recyclerview.widget.RecyclerView;

 import com.example.VisualTour.Accesso.Login;
 import com.example.VisualTour.CourseModel;
 import com.example.VisualTour.Percorsi.CourseAdapterPercorsi;
 import com.example.VisualTour.R;
 import com.example.VisualTour.RequestHttp;
 import com.example.VisualTour.databinding.CardBinding;
 import com.example.VisualTour.databinding.DetailsPoiBinding;
 import com.example.VisualTour.databinding.PrivatePoiBinding;
 import com.example.VisualTour.databinding.PublicPoiBinding;

 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONTokener;

 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;

public class privatePOI extends Fragment implements CourseAdapterPOI.ItemClickListener{
    private PrivatePoiBinding binding;
    private CourseAdapterPOI mAdapter;
    // Arraylist for storing data
    private ArrayList<CourseModel> courseModelArrayList;
    private DetailsPoiBinding bindingdet;
    private static String str=null;
    private static JSONArray jArray = null;
    private RecyclerView courseRV;



    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        str= (String) sharedPref.getAll().get("NomeUtente");
        if (str==null){
            Login.RequestLogin=true;
            Login.RequestOrigin="POI";
            NavHostFragment.findNavController(privatePOI.this)
                    .navigate(R.id.action_privatePOI_to_login);
        }
        binding = PrivatePoiBinding.inflate(inflater, container, false);
        bindingdet= DetailsPoiBinding.inflate(inflater, container, false);
        //cb= CardBinding.inflate(inflater, container, false);

        courseRV = binding.idRVCourse;
        RequestHttp rq=new RequestHttp();
        courseModelArrayList = new ArrayList<>();
        String str=null;
        try {
             sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            str= (String) sharedPref.getAll().get("NomeUtente");

            Map<String, String> request = new HashMap<>();

            if(str != null){
                request.put("NomeUtente",str);
            }
            str= rq.richiesta(request,"POI");
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
        return binding.getRoot();
    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        binding.publicPOI.setOnClickListener(v -> NavHostFragment.findNavController(privatePOI.this)
                .navigate(R.id.action_privatePOI_to_publicPOI));
        super.onViewCreated(view, savedInstanceState);
        binding.AddPoi.setOnClickListener(v -> {
            DetailsPOI.OriginRequest="New";
            NavHostFragment.findNavController(privatePOI.this)
                    .navigate(R.id.action_privatePOI_to_dettagli);
        });

    }

    @Override
    public void onItemClick(CourseModel dataModel, String id) throws JSONException {


        NavHostFragment.findNavController(privatePOI.this)
                .navigate(R.id.action_privatePOI_to_dettagli);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        DetailsPOI.OriginRequest="Private";
        int i;
        for (i = 0; i <= jArray.length(); i++) {
            if (jArray.getJSONObject(i).getString("Nome").equals(id)) {
                sharedPref.edit().putString("ID",jArray.getJSONObject(i).getString("ID")).apply();
                sharedPref.edit().putString("Nome", jArray.getJSONObject(i).getString("Nome")).apply();
                sharedPref.edit().putString("Tipo", jArray.getJSONObject(i).getString("Tipo")).apply();
                sharedPref.edit().putString("Latitudine", jArray.getJSONObject(i).getString("Latitudine")).apply();
                sharedPref.edit().putString("Longitudine", jArray.getJSONObject(i).getString("Longitudine")).apply();
                sharedPref.edit().putString("Comune", jArray.getJSONObject(i).getString("Comune")).apply();
                sharedPref.edit().putString("Provincia", jArray.getJSONObject(i).getString("Provincia")).apply();
                sharedPref.edit().putString("Regione", jArray.getJSONObject(i).getString("Regione")).apply();
                sharedPref.edit().putString("Descrizione", jArray.getJSONObject(i).getString("Descrizione")).apply();
                break;
            }
        }
    }
}