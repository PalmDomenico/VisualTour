package com.example.VisualTour.Percorsi;
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

import com.example.VisualTour.CourseModel;
import com.example.VisualTour.R;
import com.example.VisualTour.RequestHttp;
import com.example.VisualTour.databinding.PrivateMapBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PrivateMap extends Fragment implements CourseAdapterPercorsi.ItemClickListener{
    private PrivateMapBinding binding;
    private RecyclerView courseRV;
    private ArrayList<CourseModel> courseModelArrayList;
    private static JSONArray jArray = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = PrivateMapBinding.inflate(inflater, container, false);
        courseRV = binding.idRVCourse;
        RequestHttp rq=new RequestHttp();

        courseModelArrayList = new ArrayList<>();
        String str;
        try {
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            str= (String) sharedPref.getAll().get("NomeUtente");

            Map<String, String> request = new HashMap<>();

            if(str != null){
                request.put("NomeUtente",str);
            }
            str= rq.richiesta(request,"Percorsi");
            jArray = (JSONArray) new JSONTokener(str).nextValue();
            for(int i=0; i< jArray.length();i++){
                courseModelArrayList.add(new CourseModel(jArray.getJSONObject(i).getString("Nome"), 4, R.drawable.gf,jArray.getJSONObject(i).getString("ID")));
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        CourseAdapterPercorsi.visualizzazione=true;
        CourseAdapterPercorsi courseAdapter = new CourseAdapterPercorsi(courseModelArrayList, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        courseRV.setLayoutManager(linearLayoutManager);
        courseRV.setAdapter(courseAdapter);
        return binding.getRoot();
    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        binding.publicMap.setOnClickListener(v ->{

            NavHostFragment.findNavController(PrivateMap.this)
                    .navigate(R.id.action_privateMap_to_publicMap);
        });
        binding.addMap.setOnClickListener(v-> {
            CourseAdapterPercorsi.visualizzazione=false;

            NavHostFragment.findNavController(PrivateMap.this)
        .navigate(R.id.action_privateMap_to_add2);});
    }


    @Override
    public void Add(CourseModel dataModel, String id) throws JSONException {

    }

    @Override
    public void Remove(CourseModel dataModel, String id) throws JSONException {

    }

    @Override
    public void Details(CourseModel dataModel, String id) throws JSONException {
        CourseAdapterPercorsi.visualizzazione=true;
        Add.ID=id;
        NavHostFragment.findNavController(PrivateMap.this)
                        .navigate(R.id.action_privateMap_to_add2);

    }
}
