package com.example.VisualTour.POI;



import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.VisualTour.Main.HomeMappa;
import com.example.VisualTour.RequestHttp;
import com.example.VisualTour.databinding.DetailsPoiBinding;
import com.mapbox.android.core.location.LocationEngine;


import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailsPOI extends Fragment implements OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener {
    private MapView mapView;
    private DetailsPoiBinding binding;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originlocation;
    private MapboxMap map;


    private PermissionsManager permissionsManager;
    private static SharedPreferences sharedPref  ;
    public static String OriginRequest=null;
    Location lastlocation;
    private boolean modifica=false;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DetailsPoiBinding.inflate(inflater, container, false);
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        Mapbox.getInstance(getContext().getApplicationContext(), "sk.eyJ1IjoiZG9tZW5pY29wYWxtaXNhbm8iLCJhIjoiY2wwYmR4aHNwMGpnNjNrcXNybGV4azA1cCJ9.uHzjcddM-CIK2gTJzr-9vA");


        binding.DettagliModificaPOI.dettagliView.setVisibility(View.INVISIBLE);
        binding.DettagliDatiPOI.getRoot().setVisibility((View.VISIBLE));


        if(OriginRequest.equals("Private")){//prepara il fragment visto dal punto di vista del proprietario quindi con il tasto modifica
            setModifica();
         }else
        if(OriginRequest.equals("New")){
            binding.Invia.setVisibility(View.VISIBLE);
            binding.Posizione.setVisibility(View.VISIBLE);

            nuovo();
        }else{
            binding.Posizione.setVisibility(View.INVISIBLE);

        }
        binding.Posizione.setOnClickListener(v -> {
            if(lastlocation!=null){
                MarkerOptions markerOptions=new MarkerOptions();
                if(marker!=null){
                    marker.remove();
                }
                marker=map.addMarker(markerOptions.position(new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude())));
            }

        });
        binding.Invia.setOnClickListener(v -> {

            if(OriginRequest.equals("New")){
                if(marker==null){
                    binding.ErrorMap.setVisibility(View.VISIBLE);
                }else{
                    requestHTTP("insert");
                    OriginRequest="Private";
                    invia();

                    binding.ErrorMap.setVisibility(View.INVISIBLE);
                    setModifica();

                }
            }else{
                requestHTTP("update");
                invia();

            }


        });


        binding.DettagliDatiPOI.NomeMon.setText((String)sharedPref.getAll().get("Nome"));
        binding.DettagliDatiPOI.TipoMon.setText((String)sharedPref.getAll().get("Tipo"));
        binding.DettagliDatiPOI.ComuneMon.setText((String)sharedPref.getAll().get("Comune"));
        binding.DettagliDatiPOI.ProvinciaMon.setText((String)sharedPref.getAll().get("Provincia"));
        binding.DettagliDatiPOI.RegioneMon.setText((String)sharedPref.getAll().get("Regione"));
        binding.DettagliDatiPOI.DescrizioneMon.setText((String)sharedPref.getAll().get("Descrizione"));
        mapView = binding.mapview;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return binding.getRoot();
    }
    private void setModifica(){

        binding.DettagliModificaPOI.dettagliView.setVisibility(View.INVISIBLE);
        binding.DettagliDatiPOI.getRoot().setVisibility((View.VISIBLE));

        binding.Modifica.setVisibility(View.VISIBLE);
        binding.Modifica.setOnClickListener(v -> {

            binding.Invia.setVisibility(View.INVISIBLE);
            modifica();
        });
    }
    private void invia(){//event when click on invia
        binding.DettagliModificaPOI.dettagliView.setVisibility(View.INVISIBLE);
        binding.DettagliDatiPOI.getRoot().setVisibility((View.VISIBLE));
        binding.Posizione.setVisibility(View.INVISIBLE);

        modifica=false;
        setTextViewMod();
        binding.Invia.setVisibility(View.INVISIBLE);
        binding.Modifica.setVisibility(View.VISIBLE);

    }
    private void nuovo(){
        binding.DettagliModificaPOI.dettagliView.setVisibility(View.VISIBLE);
        binding.DettagliDatiPOI.getRoot().setVisibility((View.INVISIBLE));
        setTextBox();
    }


    private void setTextBox(){//setta il testo nelle textbox
        binding.DettagliModificaPOI.textName.setText(binding.DettagliDatiPOI.NomeMon.getText());
        binding.DettagliModificaPOI.textTipo.setText(binding.DettagliDatiPOI.TipoMon.getText());
        binding.DettagliModificaPOI.textComune.setText(binding.DettagliDatiPOI.ComuneMon.getText());
        binding.DettagliModificaPOI.textProvincia.setText(binding.DettagliDatiPOI.ProvinciaMon.getText());
        binding.DettagliModificaPOI.textRegione.setText(binding.DettagliDatiPOI.RegioneMon.getText());
        binding.DettagliModificaPOI.textDescrizione.setText(binding.DettagliDatiPOI.DescrizioneMon.getText());
    }
    private void setTextViewMod(){
        binding.DettagliDatiPOI.NomeMon.setText(binding.DettagliModificaPOI.textName.getText().toString());
        binding.DettagliDatiPOI.TipoMon.setText(binding.DettagliModificaPOI.textTipo.getText().toString());
        binding.DettagliDatiPOI.ComuneMon.setText(binding.DettagliModificaPOI.textComune.getText().toString());
        binding.DettagliDatiPOI.ProvinciaMon.setText(binding.DettagliModificaPOI.textProvincia.getText().toString());
        binding.DettagliDatiPOI.RegioneMon.setText(binding.DettagliModificaPOI.textRegione.getText().toString());
        binding.DettagliDatiPOI.DescrizioneMon.setText(binding.DettagliModificaPOI.textDescrizione.getText().toString());
    }

    private void modifica() {//set view modifica
        modifica=true;
        binding.Modifica.setVisibility(View.INVISIBLE);
        binding.DettagliModificaPOI.dettagliView.setVisibility(View.VISIBLE);
        binding.DettagliDatiPOI.getRoot().setVisibility((View.INVISIBLE));
        setTextBox();
        binding.Invia.setVisibility(View.VISIBLE);
        binding.Posizione.setVisibility(View.VISIBLE);

    }

    private void requestHTTP(String type){
        RequestHttp requestHttp=new RequestHttp();
        Map<String,String> argument=new HashMap<>();
        argument.put("Nome",binding.DettagliModificaPOI.textName.getText().toString());
        argument.put("Tipo",binding.DettagliModificaPOI.textTipo.getText().toString());
        argument.put("Comune",binding.DettagliModificaPOI.textComune.getText().toString());
        argument.put("Provincia",binding.DettagliModificaPOI.textProvincia.getText().toString());
        argument.put("Regione",binding.DettagliModificaPOI.textRegione.getText().toString());
        argument.put("Descrizione",binding.DettagliModificaPOI.textDescrizione.getText().toString());
        argument.put("Latitudine",Double.toString(marker.getPosition().getLatitude()));
        argument.put("Longitudine",Double.toString(marker.getPosition().getLongitude()));
        argument.put("NomeUtente",(String) sharedPref.getAll().get("NomeUtente"));
        try {
            if(type.equals("update")){
                argument.put("ID", (String) sharedPref.getAll().get("ID"));
                String str =requestHttp.richiesta(argument,"POIupdate");

            }
            if(type.equals("insert")){
                String str =requestHttp.richiesta(argument,"POIinsert");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onStart() {

        super.onStart();
        mapView.onStart();

    }

    @Override
    public void onResume() {

        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {

        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {

        super.onStop();

        mapView.onStop();
    }

    @Override
    public void onLowMemory() {

        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle out) {

        super.onSaveInstanceState(out);
        mapView.onSaveInstanceState(out);
    }






    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map=mapboxMap;
        DetailsPOI.this.map = mapboxMap;

        mapboxMap.setStyle(Style.OUTDOORS,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                    }
                });

        String lat=(String)sharedPref.getAll().get("Latitudine");
        String lon=(String)sharedPref.getAll().get("Longitudine");

        map = mapboxMap;

        setCameraPosition(lat,lon);
        map.addOnMapClickListener(point -> {
            if(modifica){
                MarkerOptions markerOptions=new MarkerOptions();
                marker.remove();
                marker=map.addMarker(markerOptions.position(point));
            }else if(OriginRequest.equals("New")){
                MarkerOptions markerOptions=new MarkerOptions();
                if(marker!=null){
                    marker.remove();
                }
                marker=map.addMarker(markerOptions.position(point));

            }
            return false;
        });
    }





    Marker marker=null;

    private  void setCameraPosition(String Lat, String Lon){
        if(!OriginRequest.equals("New")){
            MarkerOptions markerOptions=new MarkerOptions();

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(Lat), Double.parseDouble(Lon)),13.0));
            marker=map.addMarker(markerOptions.position(new LatLng(Double.parseDouble(Lat), Double.parseDouble(Lon))));
        }

     }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {

         map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 13.0));

        return false;
    }



    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {

// Get an instance of the component
            LocationComponent locationComponent = map.getLocationComponent();

// Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(getContext(), loadedMapStyle).build());

// Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.getLocationEngine().getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
                @Override
                public void onSuccess(LocationEngineResult result) {
                    lastlocation=result.getLastLocation();
                }

                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });

// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        //Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            map.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            //Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            //finish();
        }
    }


}
