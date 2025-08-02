package com.zbm2.rbresolver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private RepeaterCursorAdapter itemAdapter;
    Double latitude = 51.8234, longitude = -0.3798;
    Uri CONTENT_URI = Uri.parse("content://com.zbm2.repeaterbook.RBContentProvider/repeaters");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the ActionBar title
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("RepeaterBook Connect Demo");

        // Initialize SwipeRefreshLayout
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadData();
            swipeRefreshLayout.setRefreshing(false);
        });
        loadData();
    }

    private void loadData() {
        // Set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 51.8234, -0.3798
        String[] selectionArgs = {String.valueOf(latitude), String.valueOf(longitude)};

        // Get the Cursor from the Content Provider
        try {
            Cursor cursor = getContentResolver().query(CONTENT_URI, null, null, selectionArgs, null);

            itemAdapter = new RepeaterCursorAdapter(this, cursor);
            recyclerView.setAdapter(itemAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the cursor when the activity is destroyed
        if (itemAdapter != null) {
            itemAdapter.swapCursor(null);
        }
    }

    // *********************** Setup Location ***********************

    private void getGpsLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getBaseContext()) != ConnectionResult.SUCCESS) {
            Log.d("DEBUG", "Unable to get nearby repeaters because Android device is missing Google Play Services, needed to get GPS location.");
            showErrorSnackbar("Google Play Services is missing, it's needed for GPS location.");
            return;
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPositionPermissions();
            return;
        }

        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Use the location
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            loadData();
                        } else {
                            showErrorSnackbar("Failed to find your GPS location (it came back null).");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showErrorSnackbar("Failed to find your GPS location.");
                    }
                });
    }

    // Android permission stuff
    private static final int REQUEST_LOCATION_PERMISSION_CODE = 1;

    protected void requestPositionPermissions() {
        // Check that the user allows our app to get position, otherwise ask for the permission.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("This app needs the fine location permission")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_LOCATION_PERMISSION_CODE);
                            }
                        })
                        .create()
                        .show();

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION_CODE);
            }
        } else {
            getGpsLocation(); // Already have the permissions
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted.
                    getGpsLocation();
                } else {
                    // Permission denied
                    Log.d("DEBUG", "Warning: Need fine location permission to find nearby repeaters, but user denied it.");
                    showErrorSnackbar("Can't get your GPS location because the permission was denied.");
                }
                return;
            }
        }
    }

    private void showErrorSnackbar(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}