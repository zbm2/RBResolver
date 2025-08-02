# RepeaterBook Connect API

**Empower your Android applications with amateur radio repeater data, sourced directly from the user's RepeaterBook installation using this efficient Content Provider service.**

RepeaterBook Connect allows third-party Android apps to seamlessly integrate and display repeater information. This provides a powerful way to leverage the comprehensive RepeaterBook dataset without needing to manage data synchronization, network calls, or complex UI elements for repeater selection.

## Key Advantages for Developers

*   **Zero UI Development for Selection:** Leverage RepeaterBook's sophisticated filtering, sorting, and location-based selection. Your app gets the data the user has already configured.
*   **Always Up-to-Date Data:** Reflects the latest information available in the user's RepeaterBook app.
*   **Simplified Data Access:** Focus on your app's core functionality instead of repeater data management.
*   **Offline First:** Operates entirely offline, querying the local RepeaterBook database. Ideal for apps used in areas with poor or no connectivity.
*   **High Performance:** Direct local queries mean fast results without network latency.
*   **Standard Android Patterns:** Easy to integrate using familiar `ContentResolver` and `Cursor` objects.
*   **Reduced App Complexity:** Offload the burden of data selection, sorting, fetching, parsing, and storage to RepeaterBook.

## :warning: Important: Subscription & Installation Prerequisites

For your application to successfully retrieve data via the RepeaterBook Connect API:

1.  **RepeaterBook App Installation:** The official RepeaterBook Android application **must be installed** on the user's device.
2.  **Active RepeaterBook Connect Subscription:** The user **must have an active "RepeaterBook Connect" subscription** within their RepeaterBook application.

Your application should:
*   Gracefully check for the presence of the RepeaterBook app.
*   Guide users on how to install RepeaterBook and/or acquire a subscription if data access fails due to these prerequisites.
*   Clearly inform users why this integration is beneficial and that it relies on their existing RepeaterBook setup.

## Technical Overview

The RepeaterBook Connect API is implemented as an Android `ContentProvider`. Your application will use a `ContentResolver` to query this provider.

*   **Content URI:** `content://com.zbm2.repeaterbook.RBContentProvider/repeaters`
*   **Data Columns:** The returned `Cursor` will contain columns representing repeater attributes. Key columns include:
    *   `_id` (long): Unique row ID.
    *   `callsign` (String): Repeater callsign.
    *   `frequency` (String): Downlink frequency.
    *   `uplink_freq` (String): Uplink frequency.
    *   `tone` (String): Uplink CTCSS tone or DCS code.
    *   `downlink_tone` (String): Downlink CTCSS tone or DCS code.
    *   `mode` (String): e.g., FM, D-STAR, DMR, C4FM.
    *   `trust` (int): Data source/trust level.
    *   `status` (String): e.g., On-Air, Off-Air.
    *   `country_code` (String): e.g., US, GB.
    *   `state_code` (String): e.g., CA, NY.
    *   `county_code` (String): e.g., Los Angeles.
    *   `city` (String): Nearest city.
    *   `landmark` (String): Landmark.
    *   `latitude` (double): Latitude in decimal degrees.
    *   `longitude` (double): Longitude in decimal degrees.
    *   `notes` (String): Additional notes.
    *   `distance` (double): Calculated distance if location provided in query.
    *   *(You can find a more comprehensive list by inspecting the cursor in the demo app or future documentation).*
*   **Query Parameters:**
    *   `projection` (String[]): Specify which columns you need. `null` for all available columns.
    *   `selection` (String): Typically `null`. The selection logic (filtering, sorting, location proximity) is primarily handled by the settings within the RepeaterBook app itself.
    *   `selectionArgs` (String[]): Your app can provide `latitude` (as `String`) and `longitude` (as `String`) in this array if you wish to query for repeaters around a *specific* location, overriding the RepeaterBook app's current location focus for that query. If `null` or empty, it uses RepeaterBook's current context.
        *   Example: `String[] selectionArgs = { "51.8234", "-0.3798" };`
    *   `sortOrder` (String): Typically `null`. The sort order is determined by the user's settings in the RepeaterBook app.

## Permissions

Your application does not need to declare any special permissions to query this Content Provider. Standard access to content providers is implicit.

## Getting Started / Integration

To integrate the RepeaterBook Connect API into your Android application, follow these steps:

1.  **(Prerequisite) User Setup:**
    Ensure your application guides users to have the RepeaterBook app installed and an active "RepeaterBook Connect" subscription. Your app should gracefully handle cases where these prerequisites are not met. (Refer to the ":warning: Important: Subscription & Installation Prerequisites" section for more details).

2.  **Declare Package Visibility (Android 11+):**
    If your application targets Android 11 (API level 30) or higher, you **must** declare your app's need to query the RepeaterBook application in your `AndroidManifest.xml` file. Add the following `<queries>` element inside your top-level `<manifest>` tag:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.yourapp">

    <!-- This allows your app to discover and query the RepeaterBook Content Provider -->
    <!-- on Android 11 (API 30) and higher. -->
    <queries>
        <package android:name="com.zbm2.repeaterbook" />
    </queries>

    <application
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        ...>
        
        <!-- Your activities, services, etc. -->

    </application>
</manifest>
```
This declaration is necessary for your application to discover and interact with the RepeaterBook Content Provider on devices running Android 11 and newer. For more comprehensive details, you can consult the official Android documentation on [Package Visibility in Android 11](https://developer.android.com/training/package-visibility).

3.  **Query the Content Provider:**
    Use Android's `ContentResolver` to query the repeater data. Refer to the "Example Usage (Java)" section above for a practical code sample. It is crucial to handle potential `null` cursors and exceptions that may arise during the query process.

    *   **Content URI:** `content://com.zbm2.repeaterbook.RBContentProvider/repeaters`

4.  **Process the Data:**
    Once a `Cursor` containing data is successfully retrieved, iterate through it to extract the specific repeater information your application requires. Refer to the "Technical Overview" section for a list of common data columns available through the provider.

## Example Usage (Java)

```java
...
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
...
```

<!--
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MyRepeaterConsumerActivity extends AppCompatActivity {

    public static final Uri REPEATERBOOK_CONTENT_URI = Uri.parse("content://com.zbm2.repeaterbook.RBContentProvider/repeaters");
    private static final String TAG = "RepeaterData";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Your layout setup, e.g., setContentView(R.layout.activity_my_repeater_consumer);

        loadRepeaterData(null, null); // Load based on RepeaterBook's context
        // Or to load for a specific location:
        // loadRepeaterData(51.8234, -0.3798);
    }

    private void loadRepeaterData(Double latitude, Double longitude) {
        String[] selectionArgs = null;
        if (latitude != null && longitude != null) {
            selectionArgs = new String[]{String.valueOf(latitude), String.valueOf(longitude)};
        }

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                REPEATERBOOK_CONTENT_URI,
                null, // Projection (null for all columns)
                null, // Selection (handled by RepeaterBook app or use selectionArgs for location)
                selectionArgs, // Selection arguments (optional: latitude, longitude)
                null  // Sort order (handled by RepeaterBook app)
            );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    // Example: Getting column indices once for efficiency
                    int callsignColumnIndex = cursor.getColumnIndex("callsign");
                    int frequencyColumnIndex = cursor.getColumnIndex("frequency");
                    int latitudeColumnIndex = cursor.getColumnIndex("latitude");
                    int longitudeColumnIndex = cursor.getColumnIndex("longitude");
                    // Add other column indices you need...

                    do {
                        String callsign = (callsignColumnIndex != -1) ? cursor.getString(callsignColumnIndex) : "N/A";
                        String frequency = (frequencyColumnIndex != -1) ? cursor.getString(frequencyColumnIndex) : "N/A";
                        double lat = (latitudeColumnIndex != -1) ? cursor.getDouble(latitudeColumnIndex) : 0.0;
                        double lon = (longitudeColumnIndex != -1) ? cursor.getDouble(longitudeColumnIndex) : 0.0;

                        Log.d(TAG, "Callsign: " + callsign +
                                   ", Frequency: " + frequency +
                                   ", Lat: " + lat +
                                   ", Lon: " + lon);
                        // ... process other columns and use the data ...

                    } while (cursor.moveToNext());
                } else {
                    // No repeaters found for the current criteria
                    Toast.makeText(this, "No repeater data found.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "No data returned from provider for the given criteria.");
                }
            } else {
                // Cursor is null - Content Provider might not be available or other issue
                Toast.makeText(this, "Failed to query RepeaterBook. Is it installed and subscription active?", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Cursor returned null. RepeaterBook app or subscription might be missing.");
            }
        } catch (Exception e) {
            // Handle exceptions, e.g., SecurityException if provider is not exported correctly,
            // or IllegalStateException if ContentProvider is not found.
            Toast.makeText(this, "Error accessing RepeaterBook data: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error querying RepeaterBook ContentProvider", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }
}
```
-->

## Demo Application

This repository includes a demo application RBresolver that illustrates how to:
*   Query the RepeaterBook Connect API.
*   Handle potential errors and scenarios where RepeaterBook or the subscription is unavailable.
*   Display the retrieved data in a simple list.

## Future Considerations
*   **Detailed Error Codes:** Future versions might provide more specific error feedback through the provider.
*   **Additional Query Capabilities:** We may expand query options based on developer feedback.

## Contributing
Feedback and contributions to the demo application are welcome! Please open an issue or submit a pull request. For the API itself, feature requests can be made via the RepeaterBook support channels.

## License
The demo application code in this repository is licensed under the [MIT License]. The RepeaterBook Connect API usage is governed by the RepeaterBook terms of service and subscription agreement.

<!--
* Access the latest RepeaterBook data from your own app.
* No complex repeater selection UI to write, by default the current RepeaterBook selection and sorting are returned.
* Repeater selection and sorting managed by the familar RepeaterBook app.
* RepeaterBook does not have to be running, just installed.
* High performance. Results returned directly from the RepaterBook app.
* No internet connection required. No network delays or bandwidth issues.
* Uses standard Android Content Provider / Content Resolver services.
-->
