package com.example.VisualTour.Percorsi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.VisualTour.ArCustomizationActivity;
import com.example.VisualTour.CourseModel;
import com.example.VisualTour.POI.DetailsPOI;
import com.example.VisualTour.R;
import com.example.VisualTour.RequestHttp;
import com.example.VisualTour.databinding.AddpercorsiBinding;
import com.example.VisualTour.databinding.DettailsPercorsiBinding;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
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
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.navigation.NavigationEventListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Add extends Fragment implements OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener, CourseAdapterPercorsi.ItemClickListener {
    private AddpercorsiBinding binding;
    private DettailsPercorsiBinding bindingperc;
    private PermissionsManager permissionsManager;
    private Location lastLocation;
    private boolean startnavigation = false;
    private MapView mapView;
    private MapboxMap map;
    private  NavigationMapRoute navigationMapRoute;
    private RecyclerView courseRV;
    private ArrayList<CourseModel> courseModelArrayList;
    public static String ID=null;
    DirectionsRoute routeodfgoh;
    private static JSONArray jArray = null;
    private Map<Marker,Point> coordinates =new HashMap<>();
    DirectionsRoute routePosition;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AddpercorsiBinding.inflate(inflater, container, false);
        bindingperc = DettailsPercorsiBinding.inflate(inflater, container, false);

        Mapbox.getInstance(getContext().getApplicationContext(), getString(R.string.access_token)
        );

        mapView = binding.mapview;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        //binding.Ar.setVisibility(View.INVISIBLE);
        mapboxNavigation = new MapboxNavigation(
                getContext(),
                getString(R.string.access_token),
                MapboxNavigationOptions.builder().build()
        );

        binding.InviaPerc.setOnClickListener(v -> {

            if(binding.InviaPerc.getText().equals("Add")){
                CourseAdapterPercorsi.aggiunto=new ArrayList<>();

                try {
                    Invia();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }


            }else{
                StartRoute();
            }
        });
        binding.Ar.setOnClickListener(v -> switchActivities());
        return binding.getRoot();
    }
    private MapboxNavigation mapboxNavigation;
    private void switchActivities() {
        Intent switchActivityIntent = new Intent(getActivity(), ArCustomizationActivity.class);
        switchActivityIntent.putExtra("rotta", routeodfgoh.toJson());
        startActivity(switchActivityIntent);
    }
    private void StartRoute(){
        startnavigation=true;
        getRoute();
        NavigationLauncher.startNavigation(getActivity(),setupOptions(routeodfgoh));
    }

    private NavigationLauncherOptions setupOptions(DirectionsRoute directionsRoute) {
        NavigationLauncherOptions.Builder options = NavigationLauncherOptions.builder();
        options.directionsRoute(directionsRoute);
        return options.build();
    }


    private void PrintCard(){
        courseRV = binding.idRVCoursebis;
        RequestHttp rq=new RequestHttp();
        courseModelArrayList = new ArrayList<>();
        String str;

        if (CourseAdapterPercorsi.visualizzazione){//in visualizzazione
            binding.InviaPerc.setText("Avvia");
            try {


                Map<String, String> request = new HashMap<>();

                if(ID!=null){
                    request.put("ID",ID);
                    str= rq.richiesta(request,"Punti");
                }else{
                    str= rq.richiesta(null,"Punti");
                }

                System.out.println(str);

                jArray = (JSONArray) new JSONTokener(str).nextValue();
                for(int i=0; i< jArray.length();i++){
                    courseModelArrayList.add(new CourseModel(jArray.getJSONObject(i).getString("NomePunto"), 4, R.drawable.gf));
                }
                Marker marker;
                for(int i=0; i< jArray.length();i++){

                    marker= map.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(jArray.getJSONObject(i).getString("Latitudine")),Double.parseDouble(jArray.getJSONObject(i).getString("Longitudine")))));
                    marker.setTitle(jArray.getJSONObject(i).getString("NomePunto"));
                    marker.setIcon((IconFactory.getInstance(getContext()).fromResource(R.drawable.marker)));
                    coordinates.put(marker,Point.fromLngLat(marker.getPosition().getLongitude(),marker.getPosition().getLatitude()));
                    if(i==0){
                        SetCameraPosition(marker);
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            getRoute();
        }else{
            try {
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                str= (String) sharedPref.getAll().get("NomeUtente");

                Map<String, String> request = new HashMap<>();

                if(str != null){
                    request.put("NomeUtente",str);
                }
                str= rq.richiesta(null,"POI");
                jArray = (JSONArray) new JSONTokener(str).nextValue();
                for(int i=0; i< jArray.length();i++){
                    courseModelArrayList.add(new CourseModel(jArray.getJSONObject(i).getString("Nome"), 4, R.drawable.gf));
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        CourseAdapterPercorsi courseAdapter = new CourseAdapterPercorsi(courseModelArrayList, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        // in below two lines we are setting layoutmanager and adapter to our recycler view.
        courseRV.setLayoutManager(linearLayoutManager);
        courseRV.setAdapter(courseAdapter);
    }
    private void SetCameraPosition(Marker marker){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(marker.getPosition().getLatitude() ,
                marker.getPosition().getLongitude()),13.0));
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
                    lastLocation =result.getLastLocation();
                }

                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });

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
        }
    }




    private void Invia() throws JSONException, IOException {
        if(binding.DettagliPerc.getRoot().getVisibility()== View.INVISIBLE){
            binding.DettagliPerc.getRoot().setVisibility(View.VISIBLE);
            binding.idRVCoursebis.setVisibility(View.INVISIBLE);
        }else{

            RequestHttp rq=new RequestHttp();
            JSONObject punti=new JSONObject();
            JSONObject dati=new JSONObject();

            Map<String, String> map=new HashMap<>();
            ArrayList<Marker> set =new ArrayList<> (coordinates.keySet());
            int i;
            for(i =0; i<coordinates.size();i++){
                punti.put(set.get(i).getTitle(),i);
            }
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            String str= (String) sharedPref.getAll().get("NomeUtente");

            dati.put("NomeUtente",str);
            dati.put("Nome",binding.DettagliPerc.editPersonName.getText());
            dati.put("NumPunti",i);
            dati.put("Descrizione",binding.DettagliPerc.editDescrizione.getText());

            map.put("Punti",punti.toString());
            map.put("Dati",dati.toString());
            rq.richiesta(map,"PercorsiInsert");
            CourseAdapterPercorsi.visualizzazione=true;
            binding.DettagliPerc.getRoot().setVisibility(View.INVISIBLE);
            binding.idRVCoursebis.setVisibility(View.VISIBLE);
            binding.InviaPerc.setVisibility(View.INVISIBLE);
            PrintCard();
        }
    }

    private void getRoute(){
        if(coordinates.size()>=2) {

            Collection <Point> collection=coordinates.values();
            ArrayList<Point> arrayList=new ArrayList<>();
            NavigationRoute.Builder builder = null;
            for (Iterator<Point> i = collection.iterator(); i.hasNext(); ) {
                arrayList.add(i.next());
            }
            builder = NavigationRoute.builder(getContext())
                    .accessToken(Mapbox.getAccessToken())
                  .profile(DirectionsCriteria.PROFILE_WALKING)
                    .destination(arrayList.get(arrayList.size() - 1));
            if(!startnavigation) {
                builder.origin(arrayList.get(0));
            }else{
                if(lastLocation!=null) {
                   Point  origin= Point.fromLngLat(  lastLocation.getLongitude(),lastLocation.getLatitude());
                    builder.origin(origin);
                }
            }
            for (Point waypoint : coordinates.values()) {
                builder.addWaypoint(waypoint);
            }

            builder.build()
                    .getRoute(new Callback<DirectionsResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {

                            if (response.isSuccessful()) {

                                try {
                                    assert response.body() != null;



                                        if(!startnavigation) {
                                            if (navigationMapRoute != null) {
                                                navigationMapRoute.removeRoute();
                                            }
                                            routeodfgoh = response.body().routes().get(0);
                                            mapboxNavigation.startNavigation( routeodfgoh);
                                            navigationMapRoute = new NavigationMapRoute(null, mapView, map);
                                            navigationMapRoute.addRoute(routeodfgoh);

                                        }
                                        else{
                                            routeodfgoh = response.body().routes().get(0);

                                        }

                                    startnavigation=false;



                                } catch (Exception ex) {
                                    //Toast.makeText(MainActivity.this, ex.toString(), Toast.LENGTH_LONG);
                                }

                            }
                        }

                        @Override
                        public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                        }
                    });

        }

    }
    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        return false;
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map=mapboxMap;
        Add.this.map = mapboxMap;
        Style.Builder st=new Style.Builder().fromUri("mapbox://styles/gnnsch/cl1kd5iac000114lneuddy2l3");

        mapboxMap.setStyle(st,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                    }
                });
        PrintCard();
    }

    @Override
    public void Add(CourseModel dataModel, String id) throws JSONException {
        Marker marker;
        for(int i=0; i<= jArray.length();i++){
            if(id.equals(jArray.getJSONObject(i).getString("Nome"))){
                jArray.getJSONObject(i).getString("Nome");
                marker= map.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(jArray.getJSONObject(i).getString("Latitudine")),Double.parseDouble(jArray.getJSONObject(i).getString("Longitudine")))));
                marker.setTitle(jArray.getJSONObject(i).getString("Nome"));
                marker.setIcon((IconFactory.getInstance(getContext()).fromResource(R.drawable.marker)));
                coordinates.put(marker,Point.fromLngLat(marker.getPosition().getLongitude(),marker.getPosition().getLatitude()));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(marker.getPosition().getLatitude() ,
                        marker.getPosition().getLongitude()),13.0));
                break;

            }
        }
        getRoute();
    }

    @Override
    public void Remove(CourseModel dataModel, String id) throws JSONException {
        Set<Marker> set=coordinates.keySet();

        Point point;
        for (Iterator<Marker> i = set.iterator(); i.hasNext(); ){
            Marker marker= i.next();
            if(marker.getTitle().equals(id)){
                coordinates.remove(marker);
                marker.remove();
                break;
            }
        }
        getRoute();
    }

    @Override
    public void Details(CourseModel dataModel, String id) throws JSONException {

    }




    @Override
    public  void onDestroy(){
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
}
