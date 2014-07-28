/* MainActivity.java
 * Hometown Portal
 * Team Sharp Cookie
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ViewSwitcher.ViewFactory;

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
    
    //
    // ImageSwitcher help from:
    //
    // http://www.learn-android-easily.com/2013/06/android-imageswitcher.html
    // http://android-er.blogspot.com/2012/02/animate-fade-infade-out-by-changing.html
    //
    private int						image_stack_index	= 0;
    private ImageSwitcher	image_switcher;
    private Thread				image_swap_thread;
    private int						image_stack[]	= {
    		R.drawable.banner1,
    		R.drawable.banner2,
    		R.drawable.banner3,
    		R.drawable.banner4,
    		R.drawable.banner5,
    		R.drawable.banner6 };

  	
  	
    
    @Override
    protected void onCreate( Bundle savedInstanceState ) {

        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        
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
        
        
        image_switcher = (ImageSwitcher)findViewById(R.id.imageSwitcher);
        image_switcher.setFactory(new ViewFactory()
        {
        	public View makeView()
        	{
        		// Create a new ImageView set it's properties
        		ImageView imageView = new ImageView(getApplicationContext());
        		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        		imageView.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        		return imageView;
        	}
        });

        // Declare the animations and initialize them
        Animation in = AnimationUtils.loadAnimation(this, R.anim.fadein);
        Animation out = AnimationUtils.loadAnimation(this, R.anim.fadeout);

        // set the animation type to imageSwitcher
        image_switcher.setInAnimation(in);
        image_switcher.setOutAnimation(out);

        image_swap_thread = new Thread()
        {
        	@Override
        	public void run()
        	{
        		try
        		{
        			while (true)
        			{
        				sleep(4000);
        				runOnUiThread(new Runnable()
        				{
        					@Override
        					public void run()
        					{
        						next_image();
        					}
        				});
        			}
        		}
        		catch (InterruptedException e)
        		{
        			// do nothing
        		}
        	}
        };

        next_image(); // load the first image
        image_swap_thread.start();
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
        	
        	break;
        }
        case R.id.btnEvents:{
        	
        	Intent eventsIntent = new Intent(this, EventsActivity.class);
        	startActivity(eventsIntent);
        	
        	break;
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
    
    private void next_image()
    {
    	image_stack_index++;
    	image_stack_index %= image_stack.length;
    	image_switcher.setImageResource(image_stack[image_stack_index]);
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
			break;
		case R.id.about:
			Toast
			.makeText(this,
			"Panama City Portal v3.0",
			Toast.LENGTH_LONG)
			.show();
			return(true);
		}
		return (super.onOptionsItemSelected(item));
	}
}
