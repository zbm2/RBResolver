# RepeaterBook Connect API demo

## Access RepeaterBook data from your own Android app

* Access the latest RepeaterBook data from your own app.
* No complex repeater selection UI to write, by default the current RepeaterBook selection is returned.
* Repeater selection managed by the familar RepeaterBook app.
* RepeaterBook does not have to be running, just installed.
* High performance. Results returned directly from the RepaterBook app.
* No internet connection required. No network delays or bandwidth issues.
* Uses standard Android Content Provider / Content Resolver services.

## Getting Started

### Add a dependency to Android manifest.xml

```xml
    <queries>
        <package android:name="com.zbm2.repeaterbook"/>
    </queries>
```

### Fetch data from the RepeaterBook Content Provider and display it in a RecyclerView.

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

## Dependencies

* RepeaterBook app must be installed.
* RepeaterBook Connect subscription is required to use the API.


Contact us to find out more.


