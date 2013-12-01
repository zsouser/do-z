package com.example.doz;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.*;
import com.google.android.gms.maps.model.*;

import android.database.sqlite.*;
import android.database.*;
import android.database.DatabaseUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import android.widget.Toast;

public class LocationActivity extends Activity implements OnMapClickListener, OnMarkerClickListener {

	public GoogleMap map;
	public LocationManager manager;
	public LocationListener listener;
	public EditText name;
	public LatLng here;
	public SQLiteDatabase db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);

		SQLHelper helper = new SQLHelper(this);
		db = helper.getWritableDatabase();
		
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		
		name = (EditText) findViewById(R.id.name);
		
		
		
		if (savedInstanceState != null && savedInstanceState.containsKey("name")) {
			name.setText(savedInstanceState.getString("name"));
		}
		if (savedInstanceState != null && !savedInstanceState.containsKey("id") && savedInstanceState.containsKey("lat") && savedInstanceState.containsKey("lng")) {
			here = new LatLng(savedInstanceState.getDouble("lat"),savedInstanceState.getDouble("lng"));
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(here,15));
		}
		
		manager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		listener = new LocationListener() {
			public void onLocationChanged(Location location) {
				here = new LatLng(location.getLatitude(),location.getLongitude());
				map.clear();
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(here,15));
				map.addMarker(new MarkerOptions().position(here).title("You Are Here"));
				manager.removeUpdates(this);
				if (getIntent() != null && getIntent().hasExtra("id")) {
					map.clear();
					getData(getIntent().getExtras().getInt("id"));
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
		manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
		
		if (getIntent() != null && getIntent().hasExtra("id")) {
			getData(getIntent().getExtras().getInt("id"));
		} else {
			map.setOnMapClickListener(this);
			map.setOnMarkerClickListener(this);
		}
	}
	
	public boolean onMarkerClick(Marker m) {
		onMapClick(m.getPosition());
		return true;
	}
	public void onMapClick(LatLng point) {
		if (name.getText().toString().isEmpty()) {
			name.requestFocus();
			Toast.makeText(this,(CharSequence)"Please name the location",1).show();
		} else {
			saveLocation(name.getText().toString(),point.latitude, point.longitude);
			setResult(2);
			finish();
		}
	}

	protected void onDestroy() {
		super.onDestroy();
		db.close();
		manager.removeUpdates(listener);
		listener = null;
		manager = null;
	}
	
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("name", name.getText().toString());
		outState.putDouble("lat", here.latitude);
		outState.putDouble("lng", here.longitude);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.location, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.delete:
			if (getIntent() != null && getIntent().hasExtra("id")) {
				deleteLocation(getIntent().getExtras().getInt("id"));
			}
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void deleteLocation(int id) {
		db.execSQL("DELETE FROM locations WHERE id = "+id);
	}
	
	private void saveLocation(String name, double lat, double lng) {
		db.execSQL("INSERT INTO locations (name, lat, lng) VALUES (?1, ?2, ?3)", new String[] {name, ""+lat, ""+lng});
	}
	
	private void getData(int id) {
		Cursor c = db.rawQuery("SELECT name,  lat, lng FROM locations WHERE id = "+id,null);
		if (c.moveToFirst()) {
			name.setText(c.getString(0));
			LatLng loc = new LatLng(c.getDouble(1),c.getDouble(2));
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,15));
			map.addMarker(new MarkerOptions().position(loc));
		}
	}
	

}
