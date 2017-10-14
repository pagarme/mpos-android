package me.pagar.mposandroidexample.listeners;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;

import me.pagar.mposandroid.Mpos;
import me.pagar.mposandroid.localtransactionsdb.Searcher;
import me.pagar.mposandroid.localtransactionsdb.Transaction;

public class LongClickListener implements AdapterView.OnItemLongClickListener
{
	private Context context;
	private ArrayList<BluetoothDevice> abecsList;

	public LongClickListener(Context context, ArrayList<BluetoothDevice> abecsList)
	{
		this.context = context;
		this.abecsList = abecsList;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		view.setSelected(true);
		BluetoothDevice device = abecsList.get(position);
		Log.d("Abecs", "SELECTED DEVICE " + device.getName());

		try {
			final Mpos mpos = new Mpos(abecsList.get(position), "ek_test_f9cws0bU9700VqWE4UDuBlKLbvX4IO", context);

			Searcher searcher = mpos.getTransactionsSearcher();

			Toast.makeText(context, String.valueOf(searcher.count()), Toast.LENGTH_LONG).show();

			getPage(searcher, 1);

			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void getPage(Searcher searcher, int i) {
		ArrayList<Transaction> list = searcher.page(i).run();

		int count = list.size();

		String counter = "Transaction count " + i + ": " + count;
		Log.d("Abecs", counter);
		Toast.makeText(context, counter, Toast.LENGTH_LONG).show();

		String date = "Transaction last date 1/" + i + ": " + list.get(0).createdAt;
		Log.d("Abecs", date);
		Toast.makeText(context, date, Toast.LENGTH_LONG).show();
	}
}

