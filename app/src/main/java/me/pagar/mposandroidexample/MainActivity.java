package me.pagar.mposandroidexample;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import me.pagar.mposandroidexample.listeners.ClickListener;

public class MainActivity extends AppCompatActivity implements Logger{

	private ListView listView;
	private ArrayList<String> mDeviceList = new ArrayList<>();
	private ArrayList<BluetoothDevice> abecsList = new ArrayList<>();
	private ClickListener clickListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		clickListener = new ClickListener(this, abecsList, this);

		listView = (ListView) findViewById(R.id.listView);
		listView.setOnItemClickListener(clickListener);

		getBluetoothScanPermission();

		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothAdapter.startDiscovery();

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);
	}

	public void getBluetoothScanPermission() {
		int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
		ActivityCompat.requestPermissions(this,
				new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
				MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				abecsList.add(device);
				mDeviceList.add(device.getName() + "\n" + device.getAddress());
				listView.setAdapter(new ArrayAdapter<>(context,
						android.R.layout.simple_list_item_1, mDeviceList));
				listView.setOnItemClickListener(clickListener);
			}
		}
	};

	@Override
	public void Log(String type, String text) {
		Log.d(type, text);
	}
}

