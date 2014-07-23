/* MainActivity.java
 * Project E - Eric Daniels
 */

package com.android.projecte.townportal;

import java.util.Vector;

import com.google.android.gms.ads.*;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

/*
 * Main Activity
 * Description: Main area for user to select different activities in this
 *              application.
 */
public class MainActivity extends Activity {

    // Used for constructing types of places to views
    private Vector<PlaceType> vFood = new Vector<PlaceType>(),
                              vEnt = new Vector<PlaceType>(),
                              vShop = new Vector<PlaceType>(),
                              vSchool = new Vector<PlaceType>(),
                              vTransit = new Vector<PlaceType>();
    
    private String foodTitle, entertainmentTitle, shoppingTitle, schoolsTitle, transportationTitle;
    AdView adView;
    
    @Override
    protected void onCreate( Bundle savedInstanceState ) {

        super.onCreate( savedInstanceState );

        // Use custom title bar
        requestWindowFeature( Window.FEATURE_CUSTOM_TITLE );
        setContentView( R.layout.activity_main );
        getWindow().setFeatureInt( Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title );
        
        // Get titles
        this.foodTitle = getString( R.string.food_text );
        this.entertainmentTitle = getString( R.string.entertainment_text );
        this.shoppingTitle = getString( R.string.shopping_text );
        this.schoolsTitle = getString( R.string.schools_text );
        this.transportationTitle = getString( R.string.transportation_text );

        // Setup food
        this.vFood.add( new PlaceType( "cafe", "Cafes" ) );
        this.vFood.add( new PlaceType( "restaurant", "Restaurants" ) );
        this.vFood.add( new PlaceType( "grocery_or_supermarket", "Markets" ) );

        // Setup Entertainment
        this.vEnt.add( new PlaceType( "movie_theater", "Movies" ) );
        this.vEnt.add( new PlaceType( "night_club", "Night Clubs" ) );
        this.vEnt.add( new PlaceType( "museum", "Museums" ) );

        // Setup Shopping
        this.vShop.add( new PlaceType( "shopping_mall", "Malls" ) );
        this.vShop.add( new PlaceType( "book_store", "Books" ) );
        this.vShop.add( new PlaceType( "electronics_store", "Electronics" ) );

        // Setup Schools
        this.vSchool.add( new PlaceType( "school", "Schools" ) );
        this.vSchool.add( new PlaceType( "university", "Universities" ) );
        
        // Setup Transportation
        this.vTransit.add( new PlaceType( "taxi_stand", "Taxi" ) );
        this.vTransit.add( new PlaceType( "car_rental", "Car Rental" ) );
        this.vTransit.add( new PlaceType( "airport", "Air" ) );
        
        
        //***
        //---https://developers.google.com/mobile-ads-sdk/docs/admob/fundamentals
        adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            .build();
        //Note: this presently produces an error because we have no ad unit ID
        //this can easily be registered for (from google) and added
        adView.loadAd(adRequest);
        //***
    }

    /*
     * Button On Click Listener
     * Description: Listens for any of the buttons being clicked
     *              and launches their respective activity
     */
    public void onClick( View v ) {

        switch ( v.getId() ) {
        
        case R.id.btnFood: {
            
            openPlaceList( this.foodTitle, this.vFood );
            
            break;
        }
            
        case R.id.btnEntertainment: {
            
            openPlaceList( this.entertainmentTitle, this.vEnt );
            
            break;
        }
        
        case R.id.btnShopping: {
            
            openPlaceList( this.shoppingTitle, this.vShop );
            
            break; 
        }

        case R.id.btnSchools: {
            
            openPlaceList( this.schoolsTitle, this.vSchool );
            
            break; 
        }
        
        case R.id.btnTransportation: {
            
            openPlaceList( this.transportationTitle, this.vTransit );
            
            break; 
        }
        
        case R.id.btnEmployment: {
            
            Intent employmentIntent = new Intent( this, EmploymentActivity.class );
            startActivity( employmentIntent );
            
            break; 
        }
        
        case R.id.btnNews:{
            
            Intent newsIntent = new Intent( this, NewsActivity.class );
            startActivity( newsIntent );
            
            break;
        }
        case R.id.btnWeather:{
        	
        	Intent weatherIntent = new Intent(this, WeatherActivity.class);
        	startActivity(weatherIntent);
        }
        
        default:
            break;
        }
    }

    /*
     * Open Place List
     * Description: Start a MapActivity based off certain place info.
     */
    private void openPlaceList( String title, Vector<PlaceType> places ) {

        Intent intent = new Intent( this, MapActivity.class );
        intent.putExtra( "title", title );
        
        for ( int i = 0; i < places.size(); i++ )
            intent.putExtra( "PlaceType" + Integer.toString( i + 1 ), places.get( i ) );
        
        startActivity( intent );
    }
    
  //***
    //---https://developers.google.com/mobile-ads-sdk/docs/admob/fundamentals
    @Override
    public void onResume() {
      super.onResume();
      if (adView != null) {
        adView.resume();
      }
    }

    @Override
    public void onPause() {
      if (adView != null) {
        adView.pause();
      }
      super.onPause();
    }

    @Override
    public void onDestroy() {
      // Destroy the AdView.
      if (adView != null) {
        adView.destroy();
      }
      super.onDestroy();
    }
    //***
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.option, menu);
		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.pcsite:
			String url = "http://www.pcgov.org/home";
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			startActivity(i);
		}
		return (super.onOptionsItemSelected(item));
	}
}
