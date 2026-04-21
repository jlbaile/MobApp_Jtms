package com.example.jtms30032026;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ReportFragment extends Fragment {

    private Button btnStartDate, btnEndDate, btnSelectJeepneys, btnGenerate, btnExportExcel;
    private TextView tvFilterSummary;
    private TextView tvTotalTrips, tvTotalPassengers, tvTotalRevenue;
    private RecyclerView rvReport;
    private ReportAdapter reportAdapter;
    private List<ReportItem> reportItems = new ArrayList<>();
    private ProgressBar progressBar;

    private String startDate, endDate;
    private final SimpleDateFormat sdf        = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displaySdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    private List<String> allJeepneyIds    = new ArrayList<>();
    private List<String> allJeepneyLabels = new ArrayList<>();
    private boolean[]    jeepneyChecked;
    private Set<String>  selectedJeepneyIds = new HashSet<>();

    // Snapshot of last generated data for export
    private int              lastTotalTrips      = 0;
    private int              lastTotalPassengers = 0;
    private double           lastTotalRevenue    = 0;
    private double           lastFarePrice       = 0;
    private List<ReportItem> lastBreakdown       = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnStartDate      = view.findViewById(R.id.btnStartDate);
        btnEndDate        = view.findViewById(R.id.btnEndDate);
        btnSelectJeepneys = view.findViewById(R.id.btnSelectJeepneys);
        btnGenerate       = view.findViewById(R.id.btnGenerateReport);
        btnExportExcel    = view.findViewById(R.id.btnExportExcel);
        tvFilterSummary   = view.findViewById(R.id.tvFilterSummary);
        tvTotalTrips      = view.findViewById(R.id.tvReportTotalTrips);
        tvTotalPassengers = view.findViewById(R.id.tvReportTotalPassengers);
        tvTotalRevenue    = view.findViewById(R.id.tvReportTotalRevenue);
        rvReport          = view.findViewById(R.id.rvReport);
        progressBar       = view.findViewById(R.id.reportProgressBar);

        reportAdapter = new ReportAdapter(reportItems);
        rvReport.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReport.setAdapter(reportAdapter);

        // Default date range: current month
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        startDate = sdf.format(cal.getTime());
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        endDate = sdf.format(cal.getTime());
        updateDateButtons();

        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));
        btnSelectJeepneys.setOnClickListener(v -> showJeepneySelector());
        btnGenerate.setOnClickListener(v -> fetchReport());
        btnExportExcel.setOnClickListener(v -> exportToExcel());

        // Export disabled until first report is generated
        btnExportExcel.setEnabled(false);

        loadJeepneys();
    }

    // ─── Date Picker ──────────────────────────────────────────────────────────

    private void showDatePicker(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        try {
            Date d = sdf.parse(isStart ? startDate : endDate);
            if (d != null) cal.setTime(d);
        } catch (Exception ignored) {}

        new DatePickerDialog(requireContext(), (dp, year, month, day) -> {
            cal.set(year, month, day);
            if (isStart) startDate = sdf.format(cal.getTime());
            else         endDate   = sdf.format(cal.getTime());
            updateDateButtons();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateButtons() {
        try {
            btnStartDate.setText("From: " + displaySdf.format(sdf.parse(startDate)));
            btnEndDate.setText("To: "     + displaySdf.format(sdf.parse(endDate)));
        } catch (Exception e) {
            btnStartDate.setText("From: " + startDate);
            btnEndDate.setText("To: "     + endDate);
        }
    }

    // ─── Jeepney Multi-Select ─────────────────────────────────────────────────

    private void loadJeepneys() {
        new Thread(() -> {
            try {
                URL url = new URL(AppConfig.BASE_URL + "homeread.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONArray arr = new JSONArray(sb.toString());
                allJeepneyIds.clear();
                allJeepneyLabels.clear();

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    allJeepneyIds.add(obj.getString("jeepney_id"));
                    allJeepneyLabels.add(obj.getString("plate_number") + " — " + obj.getString("driver_name"));
                }

                jeepneyChecked = new boolean[allJeepneyIds.size()];
                Arrays.fill(jeepneyChecked, true);
                selectedJeepneyIds.addAll(allJeepneyIds);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateJeepneyButtonLabel();
                        fetchReport();
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to load jeepneys", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    private void showJeepneySelector() {
        if (allJeepneyLabels.isEmpty()) {
            Toast.makeText(getContext(), "No jeepneys loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] labels = allJeepneyLabels.toArray(new String[0]);
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Select Jeepneys")
                .setMultiChoiceItems(labels, jeepneyChecked, (dialog, which, isChecked) -> {
                    jeepneyChecked[which] = isChecked;
                    if (isChecked) selectedJeepneyIds.add(allJeepneyIds.get(which));
                    else           selectedJeepneyIds.remove(allJeepneyIds.get(which));
                })
                .setPositiveButton("Done", (d, w) -> updateJeepneyButtonLabel())
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Select All", (d, w) -> {
                    Arrays.fill(jeepneyChecked, true);
                    selectedJeepneyIds.addAll(allJeepneyIds);
                    updateJeepneyButtonLabel();
                })
                .show();
    }

    private void updateJeepneyButtonLabel() {
        int count = selectedJeepneyIds.size();
        int total = allJeepneyIds.size();
        if (count == 0)          btnSelectJeepneys.setText("Jeepneys: None");
        else if (count == total) btnSelectJeepneys.setText("Jeepneys: All (" + total + ")");
        else                     btnSelectJeepneys.setText("Jeepneys: " + count + " of " + total + " selected");
    }

    // ─── Fetch Report ─────────────────────────────────────────────────────────

    private void fetchReport() {
        if (selectedJeepneyIds.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one jeepney", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        btnGenerate.setEnabled(false);
        btnExportExcel.setEnabled(false);
        tvFilterSummary.setText("Loading...");

        new Thread(() -> {
            try {
                StringBuilder postData = new StringBuilder();
                postData.append("start_date=").append(URLEncoder.encode(startDate, "UTF-8"));
                postData.append("&end_date=").append(URLEncoder.encode(endDate, "UTF-8"));

                if (selectedJeepneyIds.size() < allJeepneyIds.size()) {
                    for (String id : selectedJeepneyIds)
                        postData.append("&jeepney_ids[]=").append(URLEncoder.encode(id, "UTF-8"));
                }

                URL url = new URL(AppConfig.BASE_URL + "reportread.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                OutputStream os = conn.getOutputStream();
                os.write(postData.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject json = new JSONObject(sb.toString());
                if (!json.getBoolean("success")) throw new Exception("Server error");

                int       totalTrips      = json.getInt("total_trips");
                int       totalPassengers = json.getInt("total_passengers");
                double    totalRevenue    = json.getDouble("total_revenue");
                double    farePrice       = json.getDouble("fare_price");
                JSONArray breakdownArr    = json.getJSONArray("breakdown");

                List<ReportItem> newItems = new ArrayList<>();
                for (int i = 0; i < breakdownArr.length(); i++) {
                    JSONObject obj = breakdownArr.getJSONObject(i);
                    newItems.add(new ReportItem(
                            obj.getString("jeepney_id"),
                            obj.getString("plate_number"),
                            obj.getString("driver_name"),
                            obj.getInt("capacity"),
                            obj.getInt("trip_count"),
                            obj.getInt("est_passengers"),
                            obj.getDouble("revenue")
                    ));
                }

                // Save snapshot for export
                lastTotalTrips      = totalTrips;
                lastTotalPassengers = totalPassengers;
                lastTotalRevenue    = totalRevenue;
                lastFarePrice       = farePrice;
                lastBreakdown       = new ArrayList<>(newItems);

                String fromDisplay = startDate, toDisplay = endDate;
                try {
                    fromDisplay = displaySdf.format(sdf.parse(startDate));
                    toDisplay   = displaySdf.format(sdf.parse(endDate));
                } catch (Exception ignored) {}
                String summary = fromDisplay + "  →  " + toDisplay
                        + "   |   " + selectedJeepneyIds.size() + " jeepney(s)";

                final String finalSummary = summary;
                final int    fTrips       = totalTrips;
                final int    fPassengers  = totalPassengers;
                final double fRevenue     = totalRevenue;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvFilterSummary.setText(finalSummary);
                        tvTotalTrips.setText(String.valueOf(fTrips));
                        tvTotalPassengers.setText(String.valueOf(fPassengers));
                        tvTotalRevenue.setText(String.format("₱%.2f", fRevenue));
                        reportAdapter.updateData(newItems);
                        progressBar.setVisibility(View.GONE);
                        btnGenerate.setEnabled(true);
                        btnExportExcel.setEnabled(true);
                    });
                }

            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                                "Error fetching report: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        tvFilterSummary.setText("Failed to load");
                        progressBar.setVisibility(View.GONE);
                        btnGenerate.setEnabled(true);
                    });
                }
            }
        }).start();
    }

    // ─── Excel Export ─────────────────────────────────────────────────────────

    private void exportToExcel() {
        if (lastBreakdown.isEmpty()) {
            Toast.makeText(getContext(), "No report data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                // ✅ IMPORTANT: Do NOT use CellStyle, Font, IndexedColors, or FillPatternType.
                // POI 3.17 on Android crashes with NoClassDefFoundError: java.awt.Color
                // when createCellStyle() is called. Plain cell values work fine.
                Workbook workbook = new HSSFWorkbook();
                Sheet sheet = workbook.createSheet("JTMS Report");

                // ── Row 0: Title ──
                sheet.createRow(0).createCell(0).setCellValue("JTMS - Jeepney Trip Report");

                // ── Row 1: Date range ──
                sheet.createRow(1).createCell(0).setCellValue("Period: " + startDate + " to " + endDate);

                // ── Row 2: Fare price ──
                sheet.createRow(2).createCell(0).setCellValue("Fare Price: P" + String.format("%.2f", lastFarePrice));

                // ── Row 3: blank ──
                sheet.createRow(3);

                // ── Row 4: Summary header ──
                Row summaryHeader = sheet.createRow(4);
                summaryHeader.createCell(0).setCellValue("Total Trips");
                summaryHeader.createCell(1).setCellValue("Est. Total Passengers");
                summaryHeader.createCell(2).setCellValue("Total Revenue");

                // ── Row 5: Summary data ──
                Row summaryData = sheet.createRow(5);
                summaryData.createCell(0).setCellValue(lastTotalTrips);
                summaryData.createCell(1).setCellValue(lastTotalPassengers);
                summaryData.createCell(2).setCellValue(lastTotalRevenue);

                // ── Row 6: blank ──
                sheet.createRow(6);

                // ── Row 7: Per-jeepney breakdown header ──
                Row breakdownHeader = sheet.createRow(7);
                breakdownHeader.createCell(0).setCellValue("Plate Number");
                breakdownHeader.createCell(1).setCellValue("Driver Name");
                breakdownHeader.createCell(2).setCellValue("Capacity");
                breakdownHeader.createCell(3).setCellValue("Trips");
                breakdownHeader.createCell(4).setCellValue("Est. Passengers");
                breakdownHeader.createCell(5).setCellValue("Revenue");

                // ── Rows 8+: Per-jeepney data ──
                int rowIdx = 8;
                for (ReportItem item : lastBreakdown) {
                    Row r = sheet.createRow(rowIdx++);
                    r.createCell(0).setCellValue(item.getPlateNumber());
                    r.createCell(1).setCellValue(item.getDriverName());
                    r.createCell(2).setCellValue(item.getCapacity());
                    r.createCell(3).setCellValue(item.getTripCount());
                    r.createCell(4).setCellValue(item.getEstPassengers());
                    r.createCell(5).setCellValue(item.getRevenue());
                }

                // ── Write file ──
                String fileName = "JTMS_Report_" + startDate + "_to_" + endDate + ".xls";
                Uri fileUri;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // ✅ Android 10+ — use MediaStore
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                    values.put(MediaStore.Downloads.MIME_TYPE, "application/vnd.ms-excel");
                    values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                    fileUri = requireContext().getContentResolver()
                            .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    if (fileUri == null) throw new IOException("Failed to create file in Downloads");
                    try (OutputStream out = requireContext().getContentResolver().openOutputStream(fileUri)) {
                        workbook.write(out);
                    }
                } else {
                    // ✅ Android 9 and below — use FileProvider (NOT Uri.fromFile)
                    File exportDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    if (exportDir != null && !exportDir.exists()) exportDir.mkdirs();
                    File file = new File(exportDir, fileName);
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        workbook.write(fos);
                    }
                    fileUri = FileProvider.getUriForFile(
                            requireContext(),
                            "com.example.jtms30032026.provider",
                            file
                    );
                }

                workbook.close();

                final Uri    finalUri  = fileUri;
                final String finalName = fileName;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                                "Exported: " + finalName + "\nSaved to Downloads",
                                Toast.LENGTH_LONG).show();

                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("application/vnd.ms-excel");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, finalUri);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(shareIntent, "Share Report via"));
                    });
                }

            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(),
                                    "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        }).start();
    }
}