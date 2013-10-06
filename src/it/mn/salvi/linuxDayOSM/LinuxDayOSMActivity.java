/*
 * LinuxDayOSMActivity.java
 * LinuxDayOSM
 * Copyright (C) Stefano Salvi 2010 <stefano@salvi.mn.it>
 *
 * LinuxDayOSM is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LinuxDayOSM is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * ldpi: 120
 * mdpi: 160
 * hdpi: 240 *
 * xhdpi: 320 (LG)
 */
package it.mn.salvi.linuxDayOSM;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ZoomControls;

public class LinuxDayOSMActivity extends Activity  {
	/*
	 * BusinessMap:
http://businessmap.it/dati_networking.txt
http://businessmap.it/dati_sviluppo.txt
http://businessmap.it/dati_web.txt
http://businessmap.it/dati_formazione.txt
http://businessmap.it/dati_consulenza.txt
	 * Calendar:
http://calendar.lugmap.it/forge/events/geoevents.txt
	 */

    static final String CSVTitles = "\"Organizzatore\",\"Luogo\",\"Indirizzo\",\"Città\",\"Provincia\",\"Sito Web\",\"Longitudine\",\"Latitudine\"";
    static final String LugManCSV = "\"Associazione Culturale LugMAN (Linux Users Group MANtova)\",\"Istituto Superiore E. Fermi\",\"Strada Spolverina, 5\",\"Mantova\",\"MN\",\"http://www.lugman.net/mediawiki/index.php?title=Linux_day\",\"GEOMETRYCOLLECTION(POINT(10.76773 45.13915))\"";

    static final double topLatItaly = 46.920255315374526;
    static final double leftLonItaly = 5.80078125;
    static final double bottomLatItaly = 36.527294814546245;
    static final double righrLonItaly = 19.16015625;
    static final double fermiLat = 45.14161339283485;
    static final double fermiLon = 10.767443776130676;

    private OsmBrowser mOsmBrowser;

    static final int baseZoom = 12;
    private double homeLatitude = 0.0;
    private double homeLongitude = 0.0;
    private HereTag here = null;

    private LocationManager locationManager;
    private LocationListener listenerCoarse;
    private LocationListener listenerFine;
     
    // Holds the most up to date location.
    private Location currentLocation;
     
    // Set to false when location services are
    // unavailable.
    private boolean locationAvailable = true;
    
    Greeting greeting;
    
    private class LoadListsOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
        	loadTags();
        	return null;
        }
    	
        @Override
        protected void onPostExecute(String result) {               
            registerLocationListeners();
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);

    	DisplayMetrics metrics = new DisplayMetrics();    
		getWindowManager().getDefaultDisplay().getMetrics(metrics);    

    	mOsmBrowser = (OsmBrowser) findViewById(R.id.osmbrowser);
    	mOsmBrowser.setDisplayMetrics (metrics);	// Deve essere chiamato subito, perché imposta la dimensione, usata da tutti
    	centerItaly ();
    	String packageName =  getPackageName();
    	mOsmBrowser.setTilesDir(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + packageName + "/files/Tiles");
    	mOsmBrowser.setZoomButtons((ZoomControls) findViewById(R.id.zoom));
    	mOsmBrowser.setOpenLegenda((ImageButton)findViewById(R.id.open_legenda));

    	final ConnectivityManager connMgr = (ConnectivityManager)  
    			this.getSystemService(Context.CONNECTIVITY_SERVICE);   
    	final android.net.NetworkInfo wifi =  connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    	final android.net.NetworkInfo mobile =  connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    	if( !(wifi.isAvailable() && wifi.isConnected()) && !(mobile.isAvailable() && mobile.isConnected())) {
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setMessage("Nessuna connessione di rete - non posso caricare le mappe e le liste")
    		.setCancelable(false)
    		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    				finish();
    			}
    		});
    		AlertDialog alert = builder.create();
    		alert.show();
    	} else { 
    		greeting = new Greeting(mOsmBrowser.getContext(), false);

    		Handler handler = new Handler();
    		handler.postDelayed(
    				new Runnable() {
    					public void run() {
    						greeting.close();
    					}
    				}, 6000L);

    		new LoadListsOperation().execute("");
    		// changeLocation(locationManagerNet.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
    	}
    }
    
    private void registerLocationListeners() {
    	locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

    	// Initialize criteria for location providers
    	Criteria fine = new Criteria();
    	fine.setAccuracy(Criteria.ACCURACY_FINE);
    	Criteria coarse = new Criteria();
    	coarse.setAccuracy(Criteria.ACCURACY_COARSE);

    	// Get at least something from the device,
    	// could be very inaccurate though
    	if (locationManager.getBestProvider(fine, true) != null) {
    		currentLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(fine, true));
    	} else if (locationManager.getBestProvider(coarse, true) != null) {
    		currentLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(coarse, true));
    	}


    	if (listenerFine == null || listenerCoarse == null) {
    		createLocationListeners();
    	}

    	// Will keep updating about every 500 ms until
    	// accuracy is about 1000 meters to get quick fix.
    	if (locationManager.getBestProvider(coarse, true) != null) {
    		locationManager.requestLocationUpdates(locationManager.getBestProvider(coarse, true),30000, 1000, listenerCoarse);
    	}
    	// Will keep updating about every 500 ms until
    	// accuracy is about 50 meters to get accurate fix.
    	if (locationManager.getBestProvider(fine, true) != null) {
    		locationManager.requestLocationUpdates(locationManager.getBestProvider(fine, true), 30000, 50, listenerFine);
    	}
    	if (locationAvailable) {
    		changeLocation(currentLocation);
    	}
    }    

    /**
     *   Creates LocationListeners
     */
    private void createLocationListeners() {
    	listenerCoarse = new LocationListener() {
    		public void onStatusChanged(String provider,
    				int status, Bundle extras) {
    			switch(status) {
    			case LocationProvider.OUT_OF_SERVICE:
    			case LocationProvider.TEMPORARILY_UNAVAILABLE:
    				locationAvailable = false;
    				break;
    			case LocationProvider.AVAILABLE:
    				locationAvailable = true;
    			}
    		}
    		public void onProviderEnabled(String provider) {
    		}
    		public void onProviderDisabled(String provider) {
    		}
    		public void onLocationChanged(Location location) {
    			currentLocation = location;
    			if (location.getAccuracy() > 1000 &&
    					location.hasAccuracy())
    				locationManager.removeUpdates(this);
    			changeLocation(currentLocation);
    		}
    	};
    	listenerFine = new LocationListener() {
    		public void onStatusChanged(String provider,
    				int status, Bundle extras) {
    			switch(status) {
    			case LocationProvider.OUT_OF_SERVICE:
    			case LocationProvider.TEMPORARILY_UNAVAILABLE:
    				locationAvailable = false;
    				break;
    			case LocationProvider.AVAILABLE:
    				locationAvailable = true;
    			}
    		}
    		public void onProviderEnabled(String provider) {
    		}
    		public void onProviderDisabled(String provider) {
    		}
    		public void onLocationChanged(Location location) {
    			currentLocation = location;
    			if (location.getAccuracy() > 1000
    					&& location.hasAccuracy())
    				locationManager.removeUpdates(this);
    			changeLocation(currentLocation);
    		}
    	};
    }

    private void centerItaly () {
    	mOsmBrowser.centerArea(topLatItaly, leftLonItaly, bottomLatItaly, righrLonItaly);
    }

    
    // Initiating Menu XML file (menu.xml)
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.layout.menu, menu);
        return true;
    }
    
    /**
     * Event Handling for Individual menu item selected
     * Identify single menu item by it's id
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

    	switch (item.getItemId())
    	{
    	case R.id.menu_full:
    		centerItaly ();
    		return true;

    	case R.id.menu_center:
    		if (homeLatitude != 0.0 || homeLongitude != 0.0) {
    			mOsmBrowser.centerPoint  (homeLatitude, homeLongitude, mOsmBrowser.tileZoom);
    			mOsmBrowser.adjustAndLoad();
    			mOsmBrowser.postInvalidate();
    		}
    		return true;
    		
    	case R.id.menu_info:
    		new Greeting(mOsmBrowser.getContext(), true);
    		return true;

    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
	protected void changeLocation(Location location) {

        if (location != null && mOsmBrowser.tagList != null) {
            homeLongitude = location.getLongitude();
            homeLatitude = location.getLatitude();

            if (here == null) {
                GeoTag t = mOsmBrowser.tagList;
                int currZoom = baseZoom;
                here = new HereTag (null, new GeoPoint(homeLatitude, homeLongitude),getResources());
                while (t.next != null) {
                    t = t.next;
                }
                t.next = here;
                // printf ("longitudine %f (%f), latitudine %f (%f)\n", [location coordinate].longitude, fermiLon, [location coordinate].latitude, fermiLat);
                do {
                    mOsmBrowser.centerPoint (homeLatitude, homeLongitude, currZoom);
                    // printf ("top %d left %d bottom %d right %d Zoom %d\n", self.osmBrowser.absTopLeft.y, self.osmBrowser.absTopLeft.x, self.osmBrowser.absBottomRight.y, self.osmBrowser.absBottomRight.x,currZoom);
                    int scale = 1 << (18 - currZoom);
                    for (t = mOsmBrowser.tagList; t != null; t = t.next) {
                        if (t != here && t.isInto (mOsmBrowser.absTopLeft,  mOsmBrowser.absBottomRight, scale)) {
                            // printf ("%s %d %d\n", [ t.description UTF8String ], t.absolutePos.x, t.absolutePos.y);
                            break;
                        }
                    }
                    if (currZoom < 3) {
                        // printf ("currZoom < 3\n");
                        break;
                    }
                    if (t == null) {
                        currZoom --;
                        // printf ("Nuovo currZoom %d\n", currZoom);
                    }
                } while (t == null);
                mOsmBrowser.adjustAndLoad ();
    			mOsmBrowser.postInvalidate();
            } else {
                here.setCoords (new GeoPoint(homeLatitude, homeLongitude));
    			mOsmBrowser.postInvalidate();
            }
        }

    }

    @Override
    protected void onResume() {
    	// Make sure that when the activity has been
    	// suspended to background,
    	// the device starts getting locations again
    	registerLocationListeners();
    	super.onResume();
    }

    @Override
    protected void onPause() {
    	// Make sure that when the activity goes to
    	// background, the device stops getting locations
    	// to save battery life.
    	if (locationManager != null) {
    		if (listenerCoarse != null) {
    			locationManager.removeUpdates(listenerCoarse);
    		}
    		if (listenerFine != null) {
    			locationManager.removeUpdates(listenerFine);
    		}
    	}
    	super.onPause();
    }
    
    private GeoTag loadTagsCategory (GeoTag taglist, SharedPreferences preferences, boolean isCvs, int repo, int repoOld, Class<?> tag) {
		try {
			boolean firstElement = true;
			BufferedReader in = null;
			URL url = null;
			// URL url = new URL("http://www.linuxday.it/2011/data/");
			try {
				url = new URL(getResources().getString(repo));
				in = new BufferedReader(new InputStreamReader(url.openStream()));
			} catch (Exception e) {
				if (repoOld != 0) {
					try {
						Calendar now = Calendar.getInstance();   // This gets the current date and time.
						int year = now.get(Calendar.YEAR);
						String oldUrl = getResources().getString(repoOld).replace("%s", String.valueOf(year));
						System.out.println("Apro vecchio repository " + oldUrl);
						url = new URL(oldUrl);
						in = new BufferedReader(new InputStreamReader(url.openStream()));
					} catch (Exception e1) {
						in = null;
					}
				} else {
					in = null;					
				}
			}
			if (in != null) {
				String str = in.readLine().trim();
				String titles[] = (isCvs) ? OsmBrowser.csvParser(str) : OsmBrowser.tabParser(str);    // La prima linea sono i titoli
				while ((str = in.readLine()) != null) {
					if (!str.startsWith("\"\",")) { // Salta le linee vuote
						// System.out.println(str);
						try {   // Per intercettare gli errori di parsing delle coordinate
							// taglist = new LDTag(taglist, titles, str, getResources());
							Constructor<?> ctor = tag.getConstructor(GeoTag.class, String[].class, String.class, Resources.class);
							taglist = (GeoTag) ctor.newInstance(taglist, titles, str, getResources());
							if (firstElement) {
								taglist.initWithPreferences(preferences);
								firstElement=false;	
							}
							mOsmBrowser.setTags(taglist);   	
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return taglist;
    }
    
    private void loadTags () {
		GeoTag taglist = null;
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		taglist = loadTagsCategory (taglist, preferences, true, R.string.RepositoryName, R.string.RepositoryNameOld, LDTag.class);
		taglist = loadTagsCategory (taglist, preferences, false, R.string.LugMapRepositoryName, 0, LMTag.class);
		taglist = loadTagsCategory (taglist, preferences, false, R.string.BMNetRepositoryName, 0, BMNetTag.class);
		taglist = loadTagsCategory (taglist, preferences, false, R.string.BMDevRepositoryName, 0, BMDevTag.class);
		taglist = loadTagsCategory (taglist, preferences, false, R.string.BMWebRepositoryName, 0, BMWebTag.class);
		taglist = loadTagsCategory (taglist, preferences, false, R.string.BMEduRepositoryName, 0, BMEduTag.class);
		taglist = loadTagsCategory (taglist, preferences, false, R.string.BMPrjRepositoryName, 0, BMPrjTag.class);
		taglist = loadTagsCategory (taglist, preferences, false, R.string.CalendarRepositoryName, 0, CalendarTag.class);
    }
}
