package com.dnd.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_BACKGROUND_LOCATION_PERMISSION = 2;
    private static final String TAG = "CellInfoLogger"; // Tag for Logcat

    private TextView cellInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the TextView to show cell information
        cellInfoTextView = findViewById(R.id.cellInfoTextView);

        // Check and request location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Request foreground location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            // If permission is granted, proceed to get the cell info
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 and above, also check for background location permission
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Request background location permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            REQUEST_BACKGROUND_LOCATION_PERMISSION);
                } else {
                    // Permission granted, get cell info
                    getCellInfo();
                }
            } else {
                // If not Android 10+, just proceed to get cell info
                getCellInfo();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        getCellInfo();
                    } else {
                        Toast.makeText(this, "Background location permission required", Toast.LENGTH_LONG).show();
                    }
                } else {
                    getCellInfo();
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_BACKGROUND_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCellInfo();
            } else {
                Toast.makeText(this, "Background location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCellInfo() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        if (telephonyManager != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                // Loop through all available cell info
                for (CellInfo cellInfo : telephonyManager.getAllCellInfo()) {
                    if (cellInfo instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                        CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();

                        int cellId = cellIdentityLte.getCi(); // Full Cell ID (mCi)
                        int mcc = cellIdentityLte.getMcc(); // Mobile Country Code (MCC)
                        int mnc = cellIdentityLte.getMnc(); // Mobile Network Code (MNC)
                        int lac = cellIdentityLte.getTac(); // Location Area Code (LAC)

                        // Log cell info
                        Log.d(TAG, "Cell ID (mCi): " + cellId);
                        Log.d(TAG, "MCC: " + mcc);
                        Log.d(TAG, "MNC: " + mnc);
                        Log.d(TAG, "LAC: " + lac);

                        // Display cell info in the TextView
                        String cellInfoText = "Cell ID (mCi): " + cellId + "\n" +
                                "MCC: " + mcc + "\n" +
                                "MNC: " + mnc + "\n" +
                                "LAC: " + lac;

                        cellInfoTextView.setText(cellInfoText);
                    }
                }
            } else {
                Toast.makeText(this, "Permission required to access cell info", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "TelephonyManager unavailable", Toast.LENGTH_SHORT).show();
        }
    }
}
