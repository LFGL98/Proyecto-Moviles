package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

public class Mapa extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //El buscador requiere una subscripcion con tarjeta de credito en console cloud
    private GoogleMap mMap;
    Double latitude, longitude;

    private static final int PERMISSION_REQUEST_CODE = 7001;
    private static final int PLAY_SERVICE_REQUEST = 7002;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLACEMENT = 10;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_4);

        String apiKey = "AIzaSyDZaxRFRuxU21aaLpn3bqTH8Vpko66Wj68";


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
            PlacesClient placesClient = Places.createClient(this);
        } else {
            Toast.makeText(com.example.myapplication.Mapa.this, "*INICIANDO*", Toast.LENGTH_LONG).show();
        }

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // PlaceFields
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        // autocompleteFragment.setOnPlaceSelectedListener(MapsActivity.this);

        try {

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    displayLocation(place.getLatLng().latitude, place.getLatLng().longitude, place.getName());
                    Toast.makeText(getApplicationContext(), "NOMBRE: " + place.getName() + "DIRECCION: " + place.getLatLng(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(getApplicationContext(), "" + status.toString(), Toast.LENGTH_LONG).show();

                }

            });
        } catch (Exception e) {
            Toast.makeText(com.example.myapplication.Mapa.this, e.toString(), Toast.LENGTH_LONG).show();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setUpLocation();

    }

    ///////////////////////////////////////////////
    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation != null) {

/// mi posicion actual
            latitude = mLocation.getLatitude();
            longitude = mLocation.getLongitude();
            LatLng miPosicion = new LatLng(latitude, longitude);

            //show marker
            mMap.addMarker(new MarkerOptions()
                    .position(miPosicion)
                    .title("Mi posicion")

            );

            //Animate camera to your position
            //  mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));


            //Animate camera to your position
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);

        }

    }


    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_REQUEST).show();
            else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    ///////////////////////////////////////////////
    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        displayLocation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
/////
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }


////////////////////////////

    private void displayLocation(final double latitude, final double longitude, String name) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLocation != null) {
                //show marker
                mMap.addMarker(new MarkerOptions().
                        position(new LatLng(latitude, longitude))
                        .title("Destino"));
                //Animate camera to your position
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));

            }

        } catch (Exception e) {

            Toast.makeText(getApplicationContext(), "" + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

}
