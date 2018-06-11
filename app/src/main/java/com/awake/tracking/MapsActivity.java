package com.awake.tracking;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;
import com.akexorcist.googledirection.config.GoogleDirectionConfiguration;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.constant.Unit;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements View.OnClickListener, OnMapReadyCallback, DirectionCallback {
    @SerializedName("distance")
    private double distance;
    private LatLng startingPoint = new LatLng(-1.300176, 36.776714);
    private LatLng endPoint = new LatLng(-1.2108, 36.7950);
    private LatLng nextPoint = new LatLng(-1.2537, 36.7442);
    private GoogleMap mMap;

    TextView txtDistance;
    Button btn, btn_calc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        txtDistance = (TextView) findViewById(R.id.txtdistance);
        btn = findViewById(R.id.btn_request_direction);
        btn.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }


    /**request direction function  creates the path to be followed from X to Y and Z**/
    public void requestDirection() {
        GoogleDirectionConfiguration.getInstance().setLogEnabled(true);
        String serverKey = "AIzaSyANHN7InbEbQuKXS2c5C_OC_ZveEa8i9qY";
        GoogleDirection.withServerKey(serverKey)
                .from(startingPoint)
                .and(nextPoint)
                .to(endPoint)
                .unit(Unit.METRIC)
                .transportMode(TransportMode.DRIVING)
                .execute(this);


    }
    private void setCameraWithCoordinationBounds(Route route) {
        /** Camera dispacex the coordinates**/
        LatLng southwest = route.getBound().getSouthwestCoordination().getCoordination();
        LatLng northeast = route.getBound().getNortheastCoordination().getCoordination();
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        /*if the requestDirection function is success this function will execute
        drawing the routes for the paths*/
        Toast.makeText(this, "success with status:" + direction.getStatus(), Toast.LENGTH_LONG).show();
        if (direction.isOK()) {
            Route route = direction.getRouteList().get(0);

            int legCount = route.getLegList().size();
            for (int index = 0; index < legCount; index++) {
                Leg leg = route.getLegList().get(index);
                mMap.addMarker(new MarkerOptions().position(leg.getStartLocation().getCoordination()));
                if (index == legCount - 1) {
                    mMap.addMarker(new MarkerOptions().position(leg.getEndLocation().getCoordination()));
                }
                List<Step> stepList = leg.getStepList();
                ArrayList<PolylineOptions> polylineOptionList = DirectionConverter.createTransitPolyline(this, stepList,
                        5, Color.RED, 3, Color.BLUE);
                for (PolylineOptions polylineOption : polylineOptionList) {
                    mMap.addPolyline(polylineOption);
                }
            }
            setCameraWithCoordinationBounds(route);
            btn.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, "Failed status:" + direction.getStatus(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Toast.makeText(this, "Failed to get:" + t, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_request_direction) {
            requestDirection();
        }

    }
//function used to calculate the distance from the 3 points
    public void distanceTo(double dest) {
        Location startingPoint = new Location("Point X");
        startingPoint.setLatitude(-1.300176);
        startingPoint.setLongitude(36.776714);
        Location nextPoint = new Location("Point Y");
        nextPoint.setLatitude(-1.2537);
        nextPoint.setLongitude(36.7442);
        Location endPoint = new Location("Point Z");
        endPoint.setLatitude(-1.2108);
        endPoint.setLongitude(36.7950);
        double distance1 = startingPoint.distanceTo(nextPoint);
        double distance2 = nextPoint.distanceTo(endPoint);
        float distance = (float) (distance1 + distance2);

    }
    //calculating the cost of transport.
    public void calculateCost() {
        int costPerKm = 30;
        double price;
        price = distance / 1000 * costPerKm;
    }

}
