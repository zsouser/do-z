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
import android.app.*;
import android.view.Menu;
import android.widget.*;
import android.view.*;
import android.content.*;
import android.database.sqlite.*;
import android.database.*;
import android.graphics.Color;
public class MainActivity extends Activity implements ListView.OnItemClickListener, Button.OnClickListener {

	public static final int LOCATIONS = 1, LIST = 2;
	public int flag = LIST;
	public ListView list;
	public Button create;
	public SQLiteDatabase db;
	public LocationManager manager;
	public LocationListener listener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		db = (new SQLHelper(this)).getWritableDatabase();
		list = (ListView)findViewById(R.id.list);
		create = (Button)findViewById(R.id.create);
		create.setOnClickListener(this);
		if (savedInstanceState != null && savedInstanceState.containsKey("flag"))
			flag = savedInstanceState.getInt("flag");
		if (savedInstanceState == null || !savedInstanceState.containsKey("listening") || !savedInstanceState.getBoolean("listening"))
			startListening();
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		loadList(flag);
	}
	
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("listening", true);
		outState.putInt("flag", flag);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == 2) {
        	//Toast.makeText(this,"You will be notified the next time you are there.",0).show();
            //startListening();
        }
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
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
	
	public void sendNotifications(int location, String name) {
		Cursor c = db.rawQuery("SELECT description FROM things WHERE location = "+location, null);
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		if (c.moveToFirst()) do {
			Notification.Builder builder = new Notification.Builder(this)
		    .setContentTitle("You're at "+name+"!")
		    .setSmallIcon(R.drawable.ic_launcher)
		    .setContentText(c.getString(0));
			nm.notify((int)(System.currentTimeMillis()/1000),builder.build());
		} while (c.moveToNext());
	}
	
	private void startListening() {
		manager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		listener = new LocationListener() {
			public void onLocationChanged(Location location) {
				ArrayList<Place> locations = getLocations();
				for (Place p : locations) {
					float[] results = new float[3];
					Location.distanceBetween(location.getLatitude(), location.getLongitude(), p.lat, p.lng, results);
					if (results[0] < 100) {
						sendNotifications(p.id, p.name);
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
		manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 150, listener);

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
			create.setText("New Location");
			ArrayList<Place> locations = getLocations();
			if (locations.isEmpty()) 
				Toast.makeText(this,"No Locations",1).show();
			//else 
				list.setAdapter(new ArrayAdapter<Place>(this, android.R.layout.simple_list_item_1, locations));
		} else if (flag == LIST) {
			create.setText("New Task");
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

	public void onClick(View v) {
		Intent i = null;
		if (flag == LOCATIONS)
			i = new Intent(MainActivity.this, LocationActivity.class);
		else
			i = new Intent(MainActivity.this, NewActivity.class);
		startActivityForResult(i,2);
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
