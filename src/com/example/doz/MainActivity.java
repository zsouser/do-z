package com.example.doz;

import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.*;
import android.view.*;
import android.content.*;
import android.database.sqlite.*;
import android.database.*;
import android.graphics.Color;
public class MainActivity extends Activity implements ListView.OnItemClickListener {

	public static final int LOCATIONS = 1, LIST = 2;
	public int flag = LIST;
	public ListView list;
	public SQLiteDatabase db;
	public LocationManager manager;
	public LocationListener listener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		db = (new SQLHelper(this)).getWritableDatabase();
		list = (ListView)findViewById(R.id.list);
		if (savedInstanceState != null && savedInstanceState.containsKey("flag"))
			flag = savedInstanceState.getInt("flag");
		startListening();
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		loadList(flag);
	}
	
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("flag", flag);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.create:
			Intent i = null;
			if (flag == LOCATIONS)
				i = new Intent(MainActivity.this, LocationActivity.class);
			else
				i = new Intent(MainActivity.this, NewActivity.class);
			startActivity(i,null);
			return true;
		case R.id.locations:
			loadList(LOCATIONS);
			return true;
		case R.id.items:
			loadList(LIST);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void sendNotifications(int location) {
		Toast.makeText(this,"Sent out notification "+location,1).show();
	}
	private void startListening() {
		manager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		listener = new LocationListener() {
			public void onLocationChanged(Location location) {
				Toast.makeText(getApplication(),"Location changed",0).show();
				ArrayList<Place> locations = getLocations();
				for (Place p : locations) {
					float[] results = new float[3];
					Location.distanceBetween(location.getLatitude(), location.getLongitude(), p.lat, p.lng, results);
					Toast.makeText(getApplication(),"Location changed = "+results[0],0).show();
					if (results[0] < 100) {
						sendNotifications(p.id);
					}
				}
				
			}
			public void onStatusChanged(String provider, int status, Bundle extras) { }
			public void onProviderEnabled(String provider) {
				Toast.makeText(getApplication(),(CharSequence)("Provider " + provider + " enabled"),0).show();
			}
			public void onProviderDisabled(String provider) {
				Toast.makeText(getApplication(),(CharSequence)("Provider " + provider + " enabled"),0).show();
			}
		};
		manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 100, listener);

	}
	
	private ArrayList<Place> getLocations() {
		ArrayList<Place> locations = new ArrayList<Place>();
		Cursor c = db.rawQuery("SELECT id, name, lat, lng FROM locations", null);
		if (c.moveToFirst()) do {
			locations.add(new Place(c.getInt(0),c.getString(1),c.getDouble(2),c.getDouble(3)));
		} while (c.moveToNext());
		return locations;
	}
	
	private ArrayList<Item> getItems() {
		ArrayList<Item> items = new ArrayList<Item>();
		Cursor c = db.rawQuery("SELECT id, description FROM things", null);
		if (c.moveToFirst()) do {
			items.add(new Item(c.getInt(0),c.getString(1)));
		} while (c.moveToNext());
		return items;
	}
	
	private void loadList(int flag) {
		this.flag = flag;
		if (flag == LOCATIONS) {
			ArrayList<Place> locations = getLocations();
			if (locations.isEmpty()) 
				Toast.makeText(this,"No Locations",1).show();
			//else 
				list.setAdapter(new ArrayAdapter<Place>(this, android.R.layout.simple_list_item_1, locations));
		} else if (flag == LIST) {
			ArrayList<Item> items = getItems();
			if (items.isEmpty()) 
				Toast.makeText(this,"Nothing to do!",1).show();
			//else
				list.setAdapter(new ArrayAdapter<Item>(this, android.R.layout.simple_list_item_1, items));
		}
		list.setOnItemClickListener(this);
		
	}
	
	public void onItemClick(AdapterView<?> parent, View view,int position, long id) {	
		Intent i = null;
		Toast.makeText(this,"selected",1).show();
		Bundle extras = new Bundle();
		if (flag == LOCATIONS) {
			i = new Intent(MainActivity.this, LocationActivity.class);
			extras.putInt("id",((Place)parent.getAdapter().getItem(position)).id);
		} else if (flag == LIST) {

			i = new Intent(MainActivity.this, NewActivity.class);
			extras.putInt("id",((Item)parent.getAdapter().getItem(position)).id);
		}
		if (i != null) {
			i.putExtras(extras);
			startActivity(i);
		}
	}

	private class Item {
		public final String text;
		public final int id;
		public Item(int id, String text) {
			this.text = text;
			this.id = id;
		}
		
		public String toString() {
			return text;
		}
	}
	
	private class Place {
		String name;
		int id;
		double lat, lng;
		public Place(int id, String name, double lat, double lng) {
			this.id = id;
			this.name = name;
			this.lat = lat;
			this.lng = lng;
		}
		public String toString() {
			return name;
		}
	}
}
