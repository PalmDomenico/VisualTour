package com.example.VisualTour.Main;


import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.VisualTour.ArCustomizationActivity;
import com.example.VisualTour.CourseModel;
import com.example.VisualTour.POIActivity;
import com.example.VisualTour.R;
import com.example.VisualTour.RequestHttp;
import com.example.VisualTour.databinding.HomeMappaBinding;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.mapbox.android.core.location.LocationEngine;
//import com.mapbox.android.core.location.LocationEngineListener;
//import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.exceptions.IconBitmapChangedException;
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
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

public class HomeMappa extends Fragment implements OnMapReadyCallback, PermissionsListener {

    private MapView mapView;
    private HomeMappaBinding binding;
    private Location lastLocation;
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originlocation;
    private static JSONArray jArray = null;

    public HomeMappa() {

    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(getContext().getApplicationContext(), getString(R.string.access_token));
        Mapbox.getInstance(getContext(), getString(R.string.access_token));
        binding = com.example.VisualTour.databinding.HomeMappaBinding.inflate(inflater, container, false);


        View view = inflater.inflate(R.layout.home_mappa, container, false);
        binding.mapview.getRenderView().setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                centra();
                System.out.println("prova");
            }

            return false;
        });
        mapView = binding.mapview;
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

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        HomeMappa.this.map = mapboxMap;
        Style.Builder st = new Style.Builder().fromUri("mapbox://styles/gnnsch/cl1kd5iac000114lneuddy2l3");
        mapboxMap.setStyle(st,
                style -> {
                    List<Feature> markerCoordinates = new ArrayList<>();
                    RequestHttp rq = new RequestHttp();
                    String str = null;
                    Map<String, String> request = new HashMap<>();
                    try {
                        str = rq.richiesta(null, "POI");
                        jArray = (JSONArray) new JSONTokener(str).nextValue();
                        Map<Marker, Point> coordinates = new HashMap<>();
                        Marker marker;
                        for (int i = 0; i < jArray.length(); i++) {
                            marker = map.addMarker(new MarkerOptions().position(new LatLng(
                                    Double.parseDouble(jArray.getJSONObject(i).getString("Latitudine")),
                                    Double.parseDouble(jArray.getJSONObject(i).getString("Longitudine")))));
                            marker.setTitle(jArray.getJSONObject(i).getString("Nome"));
                            marker.setIcon((IconFactory.getInstance(getContext()).fromResource(R.drawable.marker)));
                            coordinates.put(marker, Point.fromLngLat(marker.getPosition().getLongitude(), marker.getPosition().getLatitude()));
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                    enableLocationComponent(style);
                });
        map.addOnCameraMoveListener(new MapboxMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                centra();
            }
        });
        binding.centra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                centraCamera();
                binding.centra.setVisibility(View.INVISIBLE);
            }
        });
        binding.centra.setVisibility(View.INVISIBLE);

    }

    LocationComponent locationComponent;
    Location lastlocation;

    private void centra() {
        binding.centra.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {

        LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(getContext())
                .build();

        locationComponent = map.getLocationComponent();
        locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(getContext(), loadedMapStyle)
                        .locationComponentOptions(customLocationComponentOptions)
                        .build());
        locationComponent.setLocationComponentEnabled(true);


        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {

            // locationComponent.activateLocationComponent(
            // LocationComponentActivationOptions.builder(getContext(), loadedMapStyle).build());

// Enable to make component visible
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING_GPS);


            locationComponent.getLocationEngine().getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
                @Override
                public void onSuccess(LocationEngineResult result) {
                    lastlocation = result.getLastLocation();

                }

                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });
            map.moveCamera(CameraUpdateFactory.tiltTo(1000));
            latitudine = lastlocation.getLatitude();
            longitudine = lastlocation.getLongitude();

            centraCamera();
            locationComponent.setRenderMode(RenderMode.COMPASS);

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    @SuppressLint("MissingPermission")
    public void getLocationEngine() {
        final LocationEngineResult locationEngineResult;
        locationComponent.getLocationEngine().getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(result.getLastLocation().getLatitude() ,
                        result.getLastLocation().getLongitude()),16.5),1000);
            }
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });

    }

    public Location getOriginlocation() {
        return originlocation;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    private void centraCamera(){

        getLocationEngine();

    }
    double latitudine;
    double longitudine;
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