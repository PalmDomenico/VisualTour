package com.example.VisualTour.Main;


import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.VisualTour.ArCustomizationActivity;
import com.example.VisualTour.CourseModel;
import com.example.VisualTour.POIActivity;
import com.example.VisualTour.R;
import com.example.VisualTour.RequestHttp;
import com.example.VisualTour.databinding.HomeMappaBinding;
import com.mapbox.android.core.location.LocationEngine;
//import com.mapbox.android.core.location.LocationEngineListener;
//import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeMappa extends Fragment  implements OnMapReadyCallback, PermissionsListener {

    private MapView mapView;
    private HomeMappaBinding binding;
    private Location lastLocation;
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originlocation;
    private static JSONArray jArray = null;

    public HomeMappa(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(getContext().getApplicationContext(),getString(R.string.access_token));
        Mapbox.getInstance(getContext(), getString(R.string.access_token));
        binding = com.example.VisualTour.databinding.HomeMappaBinding.inflate(inflater, container, false);


         mapView=binding.mapview;
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);
        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchActivities();
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        HomeMappa.this.map = mapboxMap;

        mapboxMap.setStyle(Style.OUTDOORS,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                    }
                });
        RequestHttp rq=new RequestHttp();
        String str=null;
        Map<String, String> request = new HashMap<>();

        try {
            str= rq.richiesta(null,"POI");


        jArray = (JSONArray) new JSONTokener(str).nextValue();
        Map<Marker,Point> coordinates =new HashMap<>();
         Marker marker;
        for(int i=0; i< jArray.length();i++){

            marker= map.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(jArray.getJSONObject(i).getString("Latitudine")),Double.parseDouble(jArray.getJSONObject(i).getString("Longitudine")))));
            marker.setTitle(jArray.getJSONObject(i).getString("Nome"));
            coordinates.put(marker, Point.fromLngLat(marker.getPosition().getLongitude(),marker.getPosition().getLatitude()));

        }      } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    Location lastlocation;

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
  // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING_GPS);
            locationComponent.getLocationEngine().getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
                @Override
                public void onSuccess(LocationEngineResult result) {
                      lastlocation=result.getLastLocation();
                }

                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastlocation.getLatitude() ,
                    lastlocation.getLongitude()),13.0));
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



    @Override
    public  void onDestroy(){
        super.onDestroy();

    }
    private void switchActivities() {
        Intent switchActivityIntent = new Intent(getActivity(), POIActivity.class);
        startActivity(switchActivityIntent);
    }
    @SuppressWarnings("MissingPermission")
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



}