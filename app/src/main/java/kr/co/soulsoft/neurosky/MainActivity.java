package kr.co.soulsoft.neurosky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import kr.co.soulsoft.neurosky.adapter.BtRecyclerVAdapter;

public class MainActivity extends AppCompatActivity {
    private Button btnBT, btnScan, btnPaired;
    private BluetoothAdapter bluetoothAdapter;
    private RecyclerView btRecyclerView;

    public static CreateConnectThread createConnectThread;


    public static Handler handler;

    public static BluetoothSocket mmSocket;

    private final ArrayList<BluetoothDevice> btDevices = new ArrayList<>();
    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setUpBT();
        onClick();

         /*
        Second most important piece of Code. GUI Handler
         */
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg){
                if (msg.what == CONNECTING_STATUS) {
                    switch (msg.arg1) {
                        case 1:
                            Log.e("TAG", "connected");
                            break;
                        case -1:
                            Log.e("TAG", "fail to connect ");
                            break;
                    }
                }
            }
        };
    }


    @SuppressLint("MissingPermission")
    private void onClick() {
        btnBT.setOnClickListener(view -> turnOnBT());

        btnScan.setOnClickListener(view -> {
            btDevices.clear();
            bluetoothAdapter.startDiscovery();
        });

        btnPaired.setOnClickListener(view->{
            btDevices.clear();
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                btDevices.addAll(pairedDevices);
            }
            setUpRecyclerView();
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
        btRecyclerView = findViewById(R.id.btRecyclerView);
        btnPaired = findViewById(R.id.btnPaired);


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
                btDevices.add(device);
                setUpRecyclerView();
            }

        }
    };

    @SuppressLint("MissingPermission")
    private void setUpRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        btRecyclerView.setLayoutManager(layoutManager);
        BtRecyclerVAdapter btRecyclerVAdapter = new BtRecyclerVAdapter(btDevices);
        btRecyclerView.setAdapter(btRecyclerVAdapter);

        btRecyclerVAdapter.OnItemClickListener(device->{
           Toast.makeText(this,"selected : "+device.getName(),Toast.LENGTH_LONG).show();
            createConnectThread = new CreateConnectThread(bluetoothAdapter,device);
            createConnectThread.start();
        });
    }

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





    /* ============================ Thread to Create Bluetooth Connection =================================== */
    public static class CreateConnectThread extends Thread {

        private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        @SuppressLint("MissingPermission")
        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, BluetoothDevice bluetoothDevice) {
            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.
             */
            BluetoothSocket tmp = null;

            try {
                /*
                Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties,the method below may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e("TAG", "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        @SuppressLint("MissingPermission")
        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e("TAG", "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.

        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("TAG", "Could not close the client socket", e);
            }
        }
    }
}