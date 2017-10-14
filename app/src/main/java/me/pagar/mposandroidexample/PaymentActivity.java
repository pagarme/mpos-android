package me.pagar.mposandroidexample;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import me.pagar.mposandroidexample.listeners.PayListener;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class PaymentActivity extends AppCompatActivity implements OnRequestPermissionsResultCallback {

	private ListView listView;
	private ArrayList<String> mDeviceList = new ArrayList<>();
	private ArrayList<BluetoothDevice> abecsList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.payment);

		PayListener payListener = new PayListener(this, abecsList);

		listView = (ListView) findViewById(R.id.listView);
		listView.setOnItemClickListener(payListener);

		getBluetoothScanPermission();
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (registered) {
			unregisterReceiver(mReceiver);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.payment, menu);
		return true;
	}


	int REQUEST_CODE = 1;
	String PERMISSION = ACCESS_COARSE_LOCATION;
	int GRANTED = PERMISSION_GRANTED;
	public void getBluetoothScanPermission() {
		ActivityCompat.requestPermissions(this, new String[]{PERMISSION}, REQUEST_CODE);
	}

	@Override
	public void onRequestPermissionsResult(
		int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults
	) {
		if (requestCode == REQUEST_CODE)
		{
			for (int p = 0; p < permissions.length; p++) {
				String permission = permissions[p];
				if (permission.equals(PERMISSION)) {
					if (grantResults[p] == GRANTED) {
						scanBluetooth();
					} else {
						Toast.makeText(
							this, R.string.denyPermission, Toast.LENGTH_LONG
						).show();
					}
				}
			}
		}
	}

	public void scanBluetooth()
	{
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothAdapter.startDiscovery();

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);
		registered = true;
	}

	private boolean registered = false;
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {

				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				if (!abecsList.contains(device)) {
					abecsList.add(device);

					mDeviceList.add(device.getName() + "\n" + device.getAddress());

					listView.setAdapter(new ArrayAdapter<>(context,
							android.R.layout.simple_list_item_1, mDeviceList));
				}
			}
		}
	};


	public void goToRefund(MenuItem item) {
		Intent refund = new Intent(this, RefundActivity.class);
		startActivity(refund);
	}



}

