/* GooglePlacesMap.java
 * Project E - Eric Daniels
 */

package com.android.projecte.townportal;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

@SuppressLint ( "SetJavaScriptEnabled")
/*
 * Google Places Map Activity
 * Description: Used with Map Activity to display map of a user 
 *              selected category and a ListView of relative places.
 * Note: Uses fragments now to avoid deprecation
 *       http://developer.android.com/guide/components/fragments.html
 */
public class GooglePlacesMap extends Fragment implements AdapterView.OnItemSelectedListener {

    // Panama City Beach Coordinates
    final private double panamaLat = 30.205971, panamaLong = -85.858862;
    final private int defaultRadius = 5997, defaultZoom = 13;
    final private String defaultMapType = "roadmap";
    
    private String type, bestProvider, currentCoords, currentMapType; 
    private int currentRadius, currentZoom, savedFirstVisiblePosition;
    private GooglePlacesSearch gpSearch = null;
    private List<Place> places = new Vector<Place>();
    private ListView placesList = null;
    private ArrayAdapter<Place> adapter = null;
    private LocationManager locationManager;
    private Spinner spinner;
    private Location locationDetails;
    private WebView mapView;
    private TextView loadingText;
    private FragmentActivity context;
    private int currentSpinnerIndex;
    private boolean alreadyStarted = false;
    private List<ListViewTask> listViewTasks = new Vector<ListViewTask>();
    private List<DetailTask> detailTasks = new Vector<DetailTask>(); 
    private AtomicInteger loadingCounter;
    private EditText filter;
    private InputMethodManager imm;
    
    // Allow menu creation
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        View view = inflater.inflate( R.layout.activity_map, container, false );
        

        // Get views
        this.spinner = (Spinner) view.findViewById( R.id.spinner1 );
        this.mapView = (WebView) view.findViewById( R.id.mapview );
        this.placesList = (ListView) view.findViewById( R.id.list );
        this.loadingText = (TextView) getActivity().findViewById( R.id.loading );
        
        
        this.spinner.setOnItemSelectedListener( this );
        
        
        this.mapView.getSettings().setJavaScriptEnabled( true );
        this.mapView.addJavascriptInterface( new WebAppInterface(), "Android" );
        
        // Create custom adapter
        this.adapter = new ArrayAdapter<Place>( getActivity(), android.R.layout.simple_list_item_1, this.places ) {
            
            @Override
            // Support shading and two text items
            public View getView( int position, View convertView, ViewGroup parent ) {
                
                // Got some help from http://stackoverflow.com/questions/11722885/what-is-difference-between-android-r-layout-simple-list-item-1-and-android-r-lay
                
                Place place = (Place) this.getItem( position );
                
                convertView =  ( (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) )
                        .inflate( android.R.layout.simple_list_item_1, parent, false );
                
                TextView text1 = (TextView) convertView.findViewById( android.R.id.text1 );
                
                text1.setText( place.name );
                
                // Center see more text
                if ( position == 0 && place.name.equals( "Refresh" ) ) {
                	
                    text1.setGravity( Gravity.CENTER );
                    text1.setTextColor( getContext().getResources().getColor( R.color.darkBlue ) );
                }
                
                return convertView;
            }
        };
        
        this.placesList.setAdapter( this.adapter );
        
        this.placesList.setOnItemClickListener( new OnItemClickListener() {
            
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
            	
            	Place place = (Place) parent.getItemAtPosition( position );
            	
            	if ( position == 0 && place.name.equals( "Refresh" )  ) {
            		
            		new ListViewTask( savedFirstVisiblePosition ).execute();
                    
                    mapView.loadData( getMapHTML(), "text/html", "UTF-8" );
                    
            	} else
            		new DetailTask( place ).execute();
            }
        });
       
        filter = (EditText) view.findViewById(R.id.filter);
        
        // Hide KB + filter box when Done
        filter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                 if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_GO ||
                        event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                    imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(filter.getWindowToken(), 0);
                    filter.setVisibility(View.GONE);
                }
                return false;
            }
        });
        
        // Hide KB + filter box when filter loses focus
        filter.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                	filter.setVisibility(View.GONE);
                }
            }
        });
        
        return view;
    }
    
    
    // Filter option
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.option_filter, menu);
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
          case R.id.filterMenu:
        	  filter.setVisibility(0);
        	  filter.requestFocus();
              filter.addTextChangedListener(new TextWatcher() {
                  
                  @Override
                  public void onTextChanged(CharSequence c, int arg1, int arg2, int arg3) {
                      adapter.getFilter().filter(c);
                  }
                   
                  @Override
                  public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                          int arg3) {
                  }
                   
                  @Override
                  public void afterTextChanged(Editable arg0) {
                  }
              });
              break;
       }
      		return (super.onOptionsItemSelected(item));
       }   
    
    @Override
    public void onDestroy() {
    	
    	for ( ListViewTask task : this.listViewTasks )
    		task.cancel( true );
    	
    	for ( DetailTask task : this.detailTasks )
    		task.cancel( true );
    	
    	super.onDestroy();
    }
    
    @Override
    public void onPause() {
        
        this.savedFirstVisiblePosition = this.placesList.getFirstVisiblePosition();
        
        super.onPause();
    }
    
    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        
        super.onActivityCreated(savedInstanceState );
        
        if ( savedInstanceState != null ) {
            
            this.init();
            
            this.currentCoords = savedInstanceState.getString( "currentCoords" );
            this.currentRadius = savedInstanceState.getInt( "currentRadius" );
            this.currentZoom = savedInstanceState.getInt( "currentZoom" );
            this.currentSpinnerIndex = savedInstanceState.getInt( "currentSpinnerIndex" );
            this.currentMapType = savedInstanceState.getString( "currentMapType" );
            
            gpSearch = new GooglePlacesSearch( type, this.currentCoords, this.currentRadius );
            new ListViewTask( savedInstanceState.getInt( "firstVisiblePosition" ) ).execute();
            
            this.mapView.loadData( getMapHTML(), "text/html", "UTF-8" );
                    
        } else {
            
            if ( this.alreadyStarted ) {
                
                gpSearch = new GooglePlacesSearch( type, this.currentCoords, this.currentRadius );
                
                new ListViewTask( this.savedFirstVisiblePosition ).execute();
                
                this.mapView.loadData( getMapHTML(), "text/html", "UTF-8" );
                
            } else {
                
                this.init();
                
                this.currentCoords = this.getGoogleCoordinates( this.panamaLat, this.panamaLong );
                this.currentRadius = this.defaultRadius;
                this.currentZoom = this.defaultZoom;
                this.currentMapType = this.defaultMapType;
                
                newLocationSelected( this.spinner.getItemAtPosition( this.currentSpinnerIndex ).toString() );
            }
        }
    }
    
    @Override
    public void onSaveInstanceState( Bundle outState ) {
        
        outState.putString( "currentCoords", this.currentCoords );
        outState.putInt( "currentRadius", this.currentRadius );
        outState.putInt( "currentZoom", this.currentZoom );
        outState.putInt( "currentSpinnerIndex" , this.currentSpinnerIndex );
        outState.putString( "currentMapType", this.currentMapType );
        
        // placesList sometimes becomes null for no reason
        // suspected support.v4 bug
        if ( this.placesList == null )
        	outState.putInt( "firstVisiblePosition", 0 );
        
        else
        	outState.putInt( "firstVisiblePosition", this.placesList.getFirstVisiblePosition() );
        
        super.onSaveInstanceState( outState );
    }
    
    /*
     * Initialize
     * Description: Initializes the object for the first time or when it is restored.
     */
    private void init() {
    	
        this.alreadyStarted = true;
        
        Bundle arguments = this.getArguments();
        this.type = arguments.getString( "type" );
        this.loadingCounter = (AtomicInteger) arguments.getSerializable( "loadingCounter" );
        
        this.context = this.getActivity();
        
        this.currentSpinnerIndex = 0;
        
        // Acquire a reference to the system Location Manager
        this.locationManager = (LocationManager) this.context.getSystemService( Context.LOCATION_SERVICE );
        
        if ( this.locationManager == null ) {
                
            Toast toast = Toast.makeText( context, "error: Failed to use the Location Service.", Toast.LENGTH_SHORT );
            toast.setGravity( Gravity.CENTER_HORIZONTAL, 0, 0 );
            toast.show();
            this.spinner.setSelection( 1 );
            this.currentSpinnerIndex = 1;
            
        } else {
                
            // Find best provider for searching locations
            this.bestProvider = this.locationManager.getBestProvider( new Criteria(), true );
            
            if ( this.bestProvider == null ) {

                Toast toast = Toast.makeText( context, "error: Please enable Location Services.", Toast.LENGTH_SHORT );
                toast.setGravity( Gravity.CENTER_HORIZONTAL, 0, 0 );
                toast.show();
                this.spinner.setSelection( 1 );
                this.currentSpinnerIndex = 1;
                
            } else {
                
                // Ask for updates every once in a while but we don't actually care when we get them
                this.locationManager.requestLocationUpdates( this.bestProvider, 6000, 20,  new LocationListener() {

                    @Override
                    public void onLocationChanged(Location location) {}

                    @Override
                    public void onProviderDisabled(String provider) {}

                    @Override
                    public void onProviderEnabled(String provider) {}

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}
                        
                });
            }
        }
    }
    
    /*
     * Web App Interface
     * Description: Links to our Google Maps WebView in order to receive events
     * Help: http://developer.android.com/guide/webapps/webview.html
     */
    private class WebAppInterface {

        /** Perform a new search */
        @JavascriptInterface
        public void mapDragged( String coords, int radius ) {
            
            currentCoords = coords;
            currentRadius = radius;
            
            gpSearch = new GooglePlacesSearch( type, currentCoords, currentRadius );
            
            getActivity().runOnUiThread( new Runnable() {

				@Override
				public void run() {
					
					new ListViewTask().execute();
				}
            });
            
        }
        
        @JavascriptInterface
        public void zoomChanged( int radius, int zoom ) {
            
            currentRadius = radius;
            currentZoom = zoom;
            
            gpSearch = new GooglePlacesSearch( type, currentCoords, currentRadius );
            
            getActivity().runOnUiThread( new Runnable() {

				@Override
				public void run() {
					
					new ListViewTask().execute();
				}
            });
        }
        
        @JavascriptInterface
        public void maptypeidChanged( String mapType ) {
            
            currentMapType = mapType;
        }
    }
    
    /*
     * Get Google Coordinates
     * Description: Generates Google Places API coordinates
     */
    private String getGoogleCoordinates( double latitude, double longitude ) {
        
        return Double.toString( latitude ) + "," + Double.toString( longitude );
    }

    /*
     * Get Map HTML
     * Description: Gets map HTML for location selected
     */
    private String getMapHTML() {

        // HTML and JavaScript source code retrieved from
        // https://developers.google.com/maps/documentation/javascript/examples/place-search
        
        // Got help from http://stackoverflow.com/questions/10482854/google-map-radius
        
        String HTMLdata = 
                "<html> <head> <title>Place searches</title> <meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\">" + 
                        "<meta charset=\"utf-8\"> <link href=\"https://developers.google.com/maps/documentation/javascript/examples/default.css\" rel=\"stylesheet\">" + 
                        "<script src=\"https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=true&libraries=places,geometry\"></script>" + 
                        "<script>" + 
                        		"function idealRadius( northEast, southWest ) { " + 
                        			"var north = new google.maps.LatLng( northEast.lat(), 0 ); " + 
                        			"var south = new google.maps.LatLng( southWest.lat(), 0 ); " + 
                        			"var east = new google.maps.LatLng( 0, northEast.lng() ); " + 
                        			"var west = new google.maps.LatLng( 0, southWest.lng() ); " + 
                        			"return Math.min( google.maps.geometry.spherical.computeDistanceBetween( north, south )/2, google.maps.geometry.spherical.computeDistanceBetween( east, west )/2 ) } " +
                                "google.maps.visualRefresh = true; var map; var markers = []; " + 
                                "function clearMap(){ for ( var i = 0; i < markers.length; i++ ) markers[i].setMap( null ); markers = []; } var infowindow; " + 
                                "function initialize() { " + 
                                "       var loc = new google.maps.LatLng(" + this.currentCoords + "); map = new google.maps.Map(document.getElementById('map-canvas'), " + 
                                        "{ mapTypeId: \"" + this.currentMapType + "\", center: loc, zoom: " + this.currentZoom + "  }); google.maps.event.addListener(map, 'dragend', " + 
                                        "function () { " + 
                                                "var bounds = map.getBounds(); var center = bounds.getCenter(); var northEast = bounds.getNorthEast(); var southWest = bounds.getSouthWest(); " + 
                                                "var service = new google.maps.places.PlacesService(map); var r = idealRadius( northEast, southWest ); " + 
                                                "service.nearbySearch( { location: center, radius: r, " + 
                                                "types: ['" + type + "']   } , callback);  Android.mapDragged( center.toUrlValue(), r ); } ); google.maps.event.addListener(map, 'zoom_changed', " + 
                                        "function () { " + 
                                                "var bounds = map.getBounds(); var center = bounds.getCenter(); var northEast = bounds.getNorthEast(); var southWest = bounds.getSouthWest(); " + 
                                                "var service = new google.maps.places.PlacesService(map); var r = idealRadius( northEast, southWest ); " + 
                                                "service.nearbySearch( { location: center, radius: r, " + 
                                                "types: ['" + type + "']   } , callback);  Android.zoomChanged( r, map.getZoom() ); } ); " + 
                                        "google.maps.event.addListener(map, 'maptypeid_changed', function() { Android.maptypeidChanged( map.getMapTypeId() ); } ); var request = { location: loc, " + 
                                        "radius: " + this.currentRadius + ", types: ['" + type + "']  };  infowindow = new google.maps.InfoWindow(); " + 
                                        "var service = new google.maps.places.PlacesService(map);  service.nearbySearch(request, callback); " + 
                                "} " + 
                                "function callback(results, status) { " + 
                                        "clearMap(); if (status == google.maps.places.PlacesServiceStatus.OK) { " + 
                                                "for (var i = 0; i < results.length; i++) { createMarker(results[i]); } } } " + 
                                "function createMarker(place) {  " + 
                                        "var placeLoc = place.geometry.location;  var marker = new google.maps.Marker({ map: map, position: place.geometry.location  });  markers.push(marker); " + 
                                        "google.maps.event.addListener(marker, 'click', function() { infowindow.setContent(place.name); infowindow.open(map, this); });} " + 
                                        "google.maps.event.addDomListener(window, 'load', initialize); " + 
                        "</script>  </head>  <body>    <div id=\"map-canvas\" style=\"width: 100%;height: 100%; float:center\"></div>  </body></html>";
                
        return HTMLdata;
    }

    /*
     * ListView Task
     * Description: AsyncTask to get Places.
     */
    private class ListViewTask extends AsyncTask<Void, Void, ArrayList<Place>> {

        private int firstVisiblePosition;
        
        public ListViewTask() {

            this.firstVisiblePosition = 0;
        }
        
        public ListViewTask( int firstVisiblePosition ) {

            this.firstVisiblePosition = firstVisiblePosition;
        }
        
        @Override
        protected void onPreExecute() {
        	
        	listViewTasks.add( this );
        	
        	loadingCounter.addAndGet( 1 );
        	
        	if ( loadingCounter.get() == 1 )
        		loadingText.setVisibility( View.VISIBLE );
        		
        }
        
        @Override
        protected ArrayList<Place> doInBackground( Void... unused ) {

            return gpSearch.findPlaces();
        }

        @Override
        protected void onPostExecute( ArrayList<Place> result ) {
        	
        	if ( isCancelled() )
        		return;
        	
        	loadingCounter.addAndGet( -1 );
        	
        	if ( loadingCounter.get() == 0 )
        		loadingText.setVisibility( View.GONE );
        	
        	if ( result != null ) {
        		
        		adapter.clear();
        		
        		for ( int i = 0 ; i < result.size(); ++i )
                    adapter.add( result.get( i ) );
                
                adapter.notifyDataSetChanged();
                
                placesList.setSelection( this.firstVisiblePosition );
        	
        	} else {
        		
        		AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        		builder.setMessage( "Failed to find places. Check your internet connection" ).setTitle( "Error" )
        			.setPositiveButton( "Ok", new DialogInterface.OnClickListener() {
    					
    					@Override
    					public void onClick(DialogInterface dialog, int which) {}
    				} );
        		
        		builder.create().show();
        		
        		result = new ArrayList<Place>();
        		Place place = new Place();
        		place.name = "Refresh";
        		
        		result.add( place );
        		
        		adapter.clear();
        		
        		for ( int i = 0 ; i < result.size(); ++i )
                    adapter.add( result.get( i ) );
                
                adapter.notifyDataSetChanged();
                
                placesList.setSelection( this.firstVisiblePosition );
        	}
            
        }
    }

    /* 
     * Detail Task
     * Description: AsyncTask to get Google Places Detail.
     */
    private class DetailTask extends AsyncTask<Void, Void, PlaceDetail> {

        private Place place;

        public DetailTask( Place place ) {

            this.place = place;
        }

        @Override
        protected void onPreExecute() {
        	
        	detailTasks.add( this );
        	
        	loadingCounter.addAndGet( 1 );
        	
        	if ( loadingCounter.get() == 1 )
        		loadingText.setVisibility( View.VISIBLE );
        }
        
        @Override
        protected PlaceDetail doInBackground( Void... unused ) {

            return gpSearch.findPlaceDetail( this.place.placeReference );
        }

        @Override
        protected void onPostExecute( PlaceDetail placeDetail ) {
        	
        	if ( isCancelled() )
        		return;
        	
        	loadingCounter.addAndGet( -1 );
        	
        	if ( loadingCounter.get() == 0 )
        		loadingText.setVisibility( View.GONE );

        	if ( placeDetail != null ) {
        		
        		// Load placeDetail into its activity
                Intent placeDetailIntent = new Intent( context, PlaceDetailActivity.class );
                placeDetailIntent.putExtra( "name", placeDetail.siteName );
                placeDetailIntent.putExtra( "rating", place.rating );
                placeDetailIntent.putExtra( "price", place.price );
                placeDetailIntent.putExtra( "address", placeDetail.address );
                placeDetailIntent.putExtra( "phonenumber", placeDetail.phoneNumber );
                placeDetailIntent.putExtra( "website", placeDetail.website );
                placeDetailIntent.putExtra( "photoRef", placeDetail.photoRef );
                placeDetailIntent.putExtra( "gpSearchType", type );
                placeDetailIntent.putExtra( "gpSearchGeoLocation", currentCoords );
                placeDetailIntent.putExtra( "loadingCounter", loadingCounter );
                
                startActivity( placeDetailIntent );
                
        	} else {
        		
        		AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        		builder.setMessage( "Failed to get details. Check your internet connection" ).setTitle( "Error" )
        			.setPositiveButton( "Ok", new DialogInterface.OnClickListener() {
    					
    					@Override
    					public void onClick(DialogInterface dialog, int which) {}
    				} );
        		
        		builder.create().show();
        	}
            
        }
    }

    /*
     * New Location Selected
     * Description: Loads Map and ListView based off selected location.
     */
    private void newLocationSelected( String location ) {
        
        // "My Location" is one of the string items of the drop-down selector
        if ( location.equals( "My Location" ) ) {

            // update latitude and longitude coordinates for each
            if ( this.bestProvider != null ) {

                this.locationDetails = locationManager.getLastKnownLocation( bestProvider );

                if ( this.locationDetails != null ) {

                    this.currentCoords = this.getGoogleCoordinates( this.locationDetails.getLatitude(), this.locationDetails.getLongitude() );

                    // Update GP Search Parameters
                    this.gpSearch = new GooglePlacesSearch( this.type, this.currentCoords, this.defaultRadius );
                    new ListViewTask().execute();
                        
                    this.mapView.stopLoading();
                    this.mapView.loadData( getMapHTML(), "text/html", "UTF-8" );

                } else {

                    // Default to Panama City
                    this.spinner.setSelection( 1 );
                    this.currentSpinnerIndex = 1;
                    location = this.spinner.getItemAtPosition( this.currentSpinnerIndex ).toString();
                    Toast toast = Toast.makeText( this.context, "Failed to get current location. Defaulting to Panama City. Try again soon.",
                                    Toast.LENGTH_SHORT );
                    
                    toast.setGravity( Gravity.CENTER_HORIZONTAL, 0, 0 );
                    toast.show();
                }
            }
        }

        if ( location.equals( "Panama City" ) ) {

            this.currentCoords = this.getGoogleCoordinates( this.panamaLat, this.panamaLong );
    
            // Update GP Search Parameters
            this.gpSearch = new GooglePlacesSearch( type, this.currentCoords, this.defaultRadius );
            new ListViewTask().execute();

            this.mapView.stopLoading();
            this.mapView.loadData( getMapHTML(), "text/html", "UTF-8" );
        }
    }
    
    
    
    @Override
    /*
     * Spinner - On Item Selected
     * Description: Called when the location to be used in searching has
     *          been set.
     */
    public void onItemSelected( AdapterView<?> arg0, View arg1, int arg2, long arg3 ) {
                
        // Avoid first time call
        if ( this.currentSpinnerIndex == this.spinner.getSelectedItemPosition() )
            return;
        else
            this.currentSpinnerIndex = this.spinner.getSelectedItemPosition();
        
        this.newLocationSelected( this.spinner.getSelectedItem().toString() );
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {}
}
