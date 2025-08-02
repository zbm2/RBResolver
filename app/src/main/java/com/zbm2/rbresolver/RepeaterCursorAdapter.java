package com.zbm2.rbresolver;

import static androidx.core.app.ActivityCompat.startActivityForResult;
import static androidx.core.content.ContextCompat.startActivity;

import static java.lang.Math.abs;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class RepeaterCursorAdapter extends RecyclerView.Adapter<RepeaterCursorAdapter.ItemViewHolder> {

    // KV4P Activity callback values
    public static final int REQUEST_ADD_MEMORY = 0;
    public static final int REQUEST_EDIT_MEMORY = 1;
    public static final int REQUEST_SETTINGS = 2;
    public static final int REQUEST_FIRMWARE = 3;
    public static final int REQUEST_FIND_REPEATERS = 4;

    // Use an enum for column names to improve type safety and readability
    private enum Column {
        ID("id"),
        CALL("Call"),
        LOCATION("Location"),
        DISTANCE("Distance"),
        COMPASS_HEADING("CompassHeading"),
        SERVICE_TXT("ServiceTxt"),
        RX("RX"),
        TX("TX"),
        OFFSET("Offset"),
        CTCSS("CTCSS"),
        DCS("DCS");

        private final String name;

        Column(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private Cursor cursor;
    private final Context context;
    private final LayoutInflater inflater;

    public RepeaterCursorAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
        this.inflater = LayoutInflater.from(context);
        setHasStableIds(true);
    }

    public void swapCursor(Cursor newCursor) {
        if (newCursor == cursor) {
            return;
        }
        Cursor oldCursor = cursor;
        cursor = newCursor;
        if (oldCursor != null) {
            oldCursor.close();
        }
        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.repeater_row, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) {
            return;
        }

        // Use the Column enum for better readability
        final long id = getLong(Column.ID);
        final String call = getString(Column.CALL);
        final String location = getString(Column.LOCATION);
        final double distance = getDouble(Column.DISTANCE);
        final String compassHeading = getString(Column.COMPASS_HEADING);
        final String serviceTxt = getString(Column.SERVICE_TXT);
        final double rx = getDouble(Column.RX);
        final double tx = getDouble(Column.TX);
        final String offset = getString(Column.OFFSET);
        final double ctcss = getDouble(Column.CTCSS);
        final int dcs = getInt(Column.DCS);

        Repeater item = new Repeater(id, call, location, distance, compassHeading, serviceTxt, rx, tx, offset, ctcss, dcs);

        holder.callTextView.setText(item.getCall());
        holder.locationTextView.setText(item.getLocation());
        holder.rangeTextView.setText(String.format(Locale.getDefault(), "%.1f Miles  %s  %s", item.getDistance(), item.getCompassHeading(), item.getserviceTxt()));
        String outText = buildFrequencyString(item);
        holder.freqTextView.setText(outText);
        holder.itemView.setOnClickListener(v -> showDetailDialog(item));
    }

    // Extracted string building into a method
    private String buildFrequencyString(Repeater item) {
        String offsetOrSimplex = Objects.equals(item.getRX(), item.getTX()) ? "Simplex" : item.getOffset();
        String ctcssText = item.getCTCSS() > 0 ? String.format(Locale.getDefault(), "%.1f Hz ", item.getCTCSS()) : "";
        String dcsText = item.getDCS() > 0 ? String.format(Locale.getDefault(), "%d", item.getDCS()) : "";
        return String.format(Locale.getDefault(), "%.5f  %s  %s%s", item.getRX(), offsetOrSimplex, ctcssText, dcsText);
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            return getLong(Column.ID);
        }
        return RecyclerView.NO_ID;
    }

    private long getLong(Column column) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(column.getName()));
    }

    private String getString(Column column) {
        return cursor.getString(cursor.getColumnIndexOrThrow(column.getName()));
    }

    private double getDouble(Column column) {
        return cursor.getDouble(cursor.getColumnIndexOrThrow(column.getName()));
    }

    private int getInt(Column column) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(column.getName()));
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView rangeTextView;
        public TextView callTextView;
        public TextView locationTextView;
        public TextView freqTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            callTextView = itemView.findViewById(R.id.callTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            rangeTextView = itemView.findViewById(R.id.rangeTextView);
            freqTextView = itemView.findViewById(R.id.outTextView);
        }
    }

    private void closeDialog(AlertDialog dialog) {
        // Close the dialog
        dialog.dismiss();
    }

    private void showDetailDialog(Repeater item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View detailView = inflater.inflate(R.layout.item_detail_layout, null);

        TextView detailCallTextView = detailView.findViewById(R.id.detailCallTextView);
        TextView detailRXTextView = detailView.findViewById(R.id.detailRXTextView);
        TextView detailTXTextView = detailView.findViewById(R.id.detailTXTextView);
        TextView detailCTCSSTextView = detailView.findViewById(R.id.detailCTCSSTextView);
        TextView detailOffsetTextView = detailView.findViewById(R.id.detailOffsetTextView);

        detailCallTextView.setText(String.format("VFO Data For : %s", item.getCall()));

        detailRXTextView.setText(String.format(Locale.getDefault(), "RX : %.5f", item.getRX()));
        detailTXTextView.setText(String.format(Locale.getDefault(), "TX : %.5f", item.getTX()));
        detailOffsetTextView.setText(String.format("Offset: %s", item.getOffset()));
        detailCTCSSTextView.setText(String.format(Locale.getDefault(), "CTCSS: %.1f", item.getCTCSS()));

          builder.setView(detailView)
                .setPositiveButton("Add to KV4P Memory Channel", (dialog, which) -> {
                    if (dialog instanceof AlertDialog) {

                        Intent intent = new Intent("com.vagell.kv4pht.ADD_MEMORY_ACTION");
                        intent.putExtra("requestCode", REQUEST_ADD_MEMORY);//0
                        intent.putExtra("name", item.getCall());
                        intent.putExtra("selectedMemoryGroup", "RB Test");
                        intent.putExtra("activeFrequencyStr", String.format(Locale.getDefault(), "%.5f", item.getRX()));
                        intent.putExtra("offset", ((!Objects.equals(item.getRX(), item.getTX())) ? (item.getRX() < item.getTX() ? "Up" : "Down") : "None")); // Calculate Offset Hack (Simplex)
                        intent.putExtra("tone", String.format(Locale.getDefault(), "%.1f", item.getCTCSS()));
                        intent.putExtra("offsetFrequencyStr", String.format(Locale.getDefault(), "%d", (int)(Math.abs((item.getRX() -  item.getTX())*1000))));

                        startActivity((Context) context, intent, null);

                        closeDialog((AlertDialog) dialog);
                    }

                })
                .setNegativeButton("Cancel", null) // Using null instead of a lambda
                .create()
                .show();
    }
}