package kr.co.soulsoft.neurosky;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Button btnBT, btnScan, btnCheck;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setUpBT();
        onClick();
    }

    @SuppressLint("MissingPermission")
    private void onClick() {
        btnBT.setOnClickListener(view -> turnOnBT());

        btnScan.setOnClickListener(view -> {
            bluetoothAdapter.startDiscovery();
        });

    }

    @SuppressLint("SetTextI18n")
    private void setUpBT() {
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "sorry, bluetooth is not available ", Toast.LENGTH_LONG).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            btnScan.setEnabled(false);
            btnBT.setText("TURN ON");
        } else {
            btnScan.setEnabled(true);
            btnBT.setText("TURN OFF");
        }
    }

    private void init() {
        btnBT = findViewById(R.id.btnBT);
        btnScan = findViewById(R.id.btnScan);
        btnCheck = findViewById(R.id.btnCheck);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(receiver, filter);
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                @SuppressLint("MissingPermission") String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.e("TAG", "device get Name : " + deviceName);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    //control bluetooth state
    @SuppressLint({"MissingPermission", "SetTextI18n"})
    private void turnOnBT() {
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            btnScan.setEnabled(true);
            btnBT.setText("TURN OFF");
        }else {
            bluetoothAdapter.disable();
            btnScan.setEnabled(false);
            btnBT.setText("TURN ON");
        }
    }
}