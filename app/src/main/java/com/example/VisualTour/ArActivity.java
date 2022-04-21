package com.example.VisualTour;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.LegStep;
import com.mapbox.api.directions.v5.models.RouteLeg;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.offroute.OffRouteListener;
import com.mapbox.services.android.navigation.v5.route.RouteFetcher;
import com.mapbox.services.android.navigation.v5.route.RouteListener;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.vision.VisionManager;
import com.mapbox.vision.ar.VisionArManager;
import com.mapbox.vision.ar.core.models.ManeuverType;
import com.mapbox.vision.ar.core.models.Route;
import com.mapbox.vision.ar.core.models.RoutePoint;
import com.mapbox.vision.ar.view.gl.VisionArView;
import com.mapbox.vision.mobile.core.interfaces.VisionEventsListener;
import com.mapbox.vision.mobile.core.models.AuthorizationStatus;
import com.mapbox.vision.mobile.core.models.Camera;
import com.mapbox.vision.mobile.core.models.Country;
import com.mapbox.vision.mobile.core.models.FrameSegmentation;
import com.mapbox.vision.mobile.core.models.classification.FrameSignClassifications;
import com.mapbox.vision.mobile.core.models.detection.FrameDetections;
import com.mapbox.vision.mobile.core.models.position.GeoCoordinate;
import com.mapbox.vision.mobile.core.models.position.VehicleState;
import com.mapbox.vision.mobile.core.models.road.RoadDescription;
import com.mapbox.vision.mobile.core.models.world.WorldDescription;
import com.mapbox.vision.performance.ModelPerformance.On;
import com.mapbox.vision.performance.ModelPerformanceMode;
import com.mapbox.vision.performance.ModelPerformanceRate;
import com.mapbox.vision.utils.VisionLogger;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Example shows how Vision and VisionAR SDKs are used to draw AR lane over the video stream from camera.
 * Also, Mapbox navigation services are used to build route and  navigation session.
 */
public class ArActivity extends BaseActivity implements RouteListener, ProgressChangeListener, OffRouteListener {

    private static final String TAG = ArActivity.class.getSimpleName();

    // Handles navigation.
    private MapboxNavigation mapboxNavigation;
    // Fetches route from points.
    private RouteFetcher routeFetcher;
    private RouteProgress lastRouteProgress;
    private LocationEngine locationEngine;
    private LocationEngineCallback<LocationEngineResult> locationCallback;

    private boolean visionManagerWasInit = false;
    private boolean navigationWasStarted = false;

    // This dummy points will be used to build route. For real world test this needs to be changed to real values for
    // source and target locations.
    private final Point  ROUTE_DESTINATION= Point.fromLngLat(  17.304790,40.786770);
    private final Point  ROUTE_ORIGIN= Point.fromLngLat(17.312918,40.789667 );

    @Override
    protected void initViews() {
        setContentView(R.layout.activity_ar_navigation);
    }

    protected void setArRenderOptions(@NotNull final VisionArView visionArView) {
        visionArView.setFenceVisible(true);
    }

    @Override
    protected void onPermissionsGranted() {
        startVisionManager();
        startNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startVisionManager();
        startNavigation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopVisionManager();
        stopNavigation();
    }

    private void startVisionManager() {
        if (allPermissionsGranted() && !visionManagerWasInit) {
            // Create and start VisionManager.
            VisionManager.create();
            VisionManager.setModelPerformance(new On(ModelPerformanceMode.DYNAMIC, ModelPerformanceRate.LOW.INSTANCE));
            VisionManager.start();
            VisionManager.setVisionEventsListener(new VisionEventsListener() {
                @Override
                public void onAuthorizationStatusUpdated(@NotNull AuthorizationStatus authorizationStatus) {
                }

                @Override
                public void onFrameSegmentationUpdated(@NotNull FrameSegmentation frameSegmentation) {
                }

                @Override
                public void onFrameDetectionsUpdated(@NotNull FrameDetections frameDetections) {
                }

                @Override
                public void onFrameSignClassificationsUpdated(@NotNull FrameSignClassifications frameSignClassifications) {
                }

                @Override
                public void onRoadDescriptionUpdated(@NotNull RoadDescription roadDescription) {
                }

                @Override
                public void onWorldDescriptionUpdated(@NotNull WorldDescription worldDescription) {
                }

                @Override
                public void onVehicleStateUpdated(@NotNull VehicleState vehicleState) {
                }

                @Override
                public void onCameraUpdated(@NotNull Camera camera) {
                }

                @Override
                public void onCountryUpdated(@NotNull Country country) {
                }

                @Override
                public void onUpdateCompleted() {
                }
            });

            VisionArView visionArView = findViewById(R.id.mapbox_ar_view);

            // Create VisionArManager.
            VisionArManager.create(VisionManager.INSTANCE);
            visionArView.setArManager(VisionArManager.INSTANCE);
            setArRenderOptions(visionArView);

            visionManagerWasInit = true;
        }
    }

    private void stopVisionManager() {
        if (visionManagerWasInit) {
            VisionArManager.destroy();
            VisionManager.stop();
            VisionManager.destroy();

            visionManagerWasInit = false;
        }
    }

    private void startNavigation() {
        if (allPermissionsGranted() && !navigationWasStarted) {
            // Initialize navigation with your Mapbox access token.
            mapboxNavigation = new MapboxNavigation(
                    this,
                    getString(R.string.access_token),
                    MapboxNavigationOptions.builder().build()
            );

            // Initialize route fetcher with your Mapbox access token.
            routeFetcher = new RouteFetcher(this, getString(R.string.access_token));
            routeFetcher.addRouteListener(this);

            locationEngine = LocationEngineProvider.getBestLocationEngine(this);

            LocationEngineRequest arLocationEngineRequest = new LocationEngineRequest.Builder(0)
                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                    .setFastestInterval(1000)
                    .build();

            locationCallback = new LocationEngineCallback<LocationEngineResult>() {
                @Override
                public void onSuccess(LocationEngineResult result) {

                }

                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            };

            try {
                locationEngine.requestLocationUpdates(arLocationEngineRequest, locationCallback, Looper.getMainLooper());
            } catch (SecurityException se) {
                VisionLogger.Companion.e(TAG, se.toString());
            }

            initDirectionsRoute();

            // Route need to be reestablished if off route happens.
            mapboxNavigation.addOffRouteListener(this);
            mapboxNavigation.addProgressChangeListener(this);

            navigationWasStarted = true;
        }
    }

    private void stopNavigation() {
        if (navigationWasStarted) {
            locationEngine.removeLocationUpdates(locationCallback);

            mapboxNavigation.removeProgressChangeListener(this);
            mapboxNavigation.removeOffRouteListener(this);

            navigationWasStarted = false;
        }
    }

    private void initDirectionsRoute() {
        // Get route from predefined points.
          String str = getIntent().getExtras().getString("rotta");

        DirectionsRoute dir= DirectionsRoute.fromJson(str);

                        // Start navigation session with retrieved route.
                         mapboxNavigation.startNavigation(dir);

                        // Set route progress.
                        VisionArManager.setRoute(new Route(
                                getRoutePoints(dir),
                                dir.duration().floatValue(),
                                "",
                                ""
                        ));



    }

    @Override
    public void onErrorReceived(Throwable throwable) {
        if (throwable != null) {
            throwable.printStackTrace();
        }

        mapboxNavigation.stopNavigation();
        Toast.makeText(this, "Can not calculate the route requested", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponseReceived(@NotNull DirectionsResponse response, RouteProgress routeProgress) {
        mapboxNavigation.stopNavigation();
        if (response.routes().isEmpty()) {
            Toast.makeText(this, "Can not calculate the route requested", Toast.LENGTH_SHORT).show();
        } else {
            DirectionsRoute route = response.routes().get(0);

            mapboxNavigation.startNavigation(route);

            // Set route progress.
            VisionArManager.setRoute(new Route(
                    getRoutePoints(route),
                    (float) routeProgress.durationRemaining(),
                    "",
                    ""
            ));
        }
    }

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {
        lastRouteProgress = routeProgress;
    }

    @Override
    public void userOffRoute(Location location) {
        routeFetcher.findRouteFromRouteProgress(location, lastRouteProgress);
    }

    private RoutePoint[] getRoutePoints(@NotNull DirectionsRoute route) {
        ArrayList<RoutePoint> routePoints = new ArrayList<>();

        List<RouteLeg> legs = route.legs();
        if (legs != null) {
            for (RouteLeg leg : legs) {

                List<LegStep> steps = leg.steps();
                if (steps != null) {
                    for (LegStep step : steps) {
                        RoutePoint point = new RoutePoint((new GeoCoordinate(
                                step.maneuver().location().latitude(),
                                step.maneuver().location().longitude()
                        )), mapToManeuverType(step.maneuver().type()));

                        routePoints.add(point);

                        List<Point> geometryPoints = buildStepPointsFromGeometry(step.geometry());
                        for (Point geometryPoint : geometryPoints) {
                            point = new RoutePoint((new GeoCoordinate(
                                    geometryPoint.latitude(),
                                    geometryPoint.longitude()
                            )), ManeuverType.None);

                            routePoints.add(point);
                        }
                    }
                }
            }
        }

        return routePoints.toArray(new RoutePoint[0]);
    }

    private List<Point> buildStepPointsFromGeometry(String geometry) {
        return PolylineUtils.decode(geometry, Constants.PRECISION_6);
    }

    private ManeuverType mapToManeuverType(@Nullable String maneuver) {
        if (maneuver == null) {
            return ManeuverType.None;
        }
        switch (maneuver) {
            case "turn":
                return ManeuverType.Turn;
            case "depart":
                return ManeuverType.Depart;
            case "arrive":
                return ManeuverType.Arrive;
            case "merge":
                return ManeuverType.Merge;
            case "on ramp":
                return ManeuverType.OnRamp;
            case "off ramp":
                return ManeuverType.OffRamp;
            case "fork":
                return ManeuverType.Fork;
            case "roundabout":
                return ManeuverType.Roundabout;
            case "exit roundabout":
                return ManeuverType.RoundaboutExit;
            case "end of road":
                return ManeuverType.EndOfRoad;
            case "new name":
                return ManeuverType.NewName;
            case "continue":
                return ManeuverType.Continue;
            case "rotary":
                return ManeuverType.Rotary;
            case "roundabout turn":
                return ManeuverType.RoundaboutTurn;
            case "notification":
                return ManeuverType.Notification;
            case "exit rotary":
                return ManeuverType.RotaryExit;
            default:
                return ManeuverType.None;
        }
    }
}
