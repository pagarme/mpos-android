package me.pagar.mposandroidexample.listeners;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;

import me.pagar.mposandroid.Mpos;
import me.pagar.mposandroidexample.Config;

public class PayListener implements AdapterView.OnItemClickListener
{
	private Context context;
	private ArrayList<BluetoothDevice> abecsList;

	public PayListener(Context context, ArrayList<BluetoothDevice> abecsList)
	{
		this.context = context;
		this.abecsList = abecsList;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
		view.setSelected(true);
		BluetoothDevice device = abecsList.get(position);
		Log.d("Abecs", "SELECTED DEVICE " + device.getName());

		try {
			final Mpos mpos = new Mpos(abecsList.get(position), Config.encryptionKey, context);

			mpos.addListener(new MposHandleListener(context, mpos, 321));

			Log.d("Abecs", "Telling to initialize");
			mpos.openConnection(false);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

