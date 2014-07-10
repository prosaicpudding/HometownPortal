/* PlaceDetailActivity.java
 * Project E - Eric Daniels
 */

package com.android.projecte.townportal;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

/*
 * Place Detail Activity
 * Description: Used with Place Detail Activity page to display detailed place 
 *              information when user clicks on a place in Map Activity.
 */
public class PlaceDetailActivity extends Activity {

    private TextView nameTextView, ratingTextView, priceTextView, 
                addressTextView, phoneNumberTextView, websiteTextView,
                loadingText;
    private ImageView photoImageView;
    
    private GooglePlacesSearch gpSearch;
    private AtomicInteger loadingCounter;
    private List<PhotoTask> photoTasks = new Vector<PhotoTask>();

    @Override
    protected void onCreate( Bundle savedInstanceState ) {

        super.onCreate( savedInstanceState );

        this.gpSearch = new GooglePlacesSearch( getIntent().getExtras().getString( "gpSearchType" ), 
                getIntent().getExtras().getString( "gpSearchGeoLocation" ), 1 );
        
        // Use custom title bar
        requestWindowFeature( Window.FEATURE_CUSTOM_TITLE );
        setContentView( R.layout.activity_place_detail );
        getWindow().setFeatureInt( Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title );
        ((TextView) findViewById( R.id.title ) ).setText( R.string.returnText );

        // Set Place detail TextViews
        this.nameTextView = (TextView) findViewById( R.id.nameText );
        this.ratingTextView = (TextView) findViewById( R.id.ratingText );
        this.priceTextView = (TextView) findViewById( R.id.priceText );
        this.addressTextView = (TextView) findViewById( R.id.addressText );
        this.phoneNumberTextView = (TextView) findViewById( R.id.phoneNumberText );
        this.websiteTextView = (TextView) findViewById( R.id.websiteText );
        this.photoImageView = (ImageView) findViewById( R.id.photoImage );
        this.loadingText = (TextView) findViewById( R.id.loading );
        
        // Set TextViews
        this.nameTextView.setText( getIntent().getExtras().getString( "name" ) );
        this.ratingTextView.setText( ratingToStar( (int) getIntent().getExtras().getDouble( "rating" ) ) );
        this.addressTextView.setText( getIntent().getExtras().getString( "address" ) );
        this.phoneNumberTextView.setText( getIntent().getExtras().getString( "phonenumber" ) );
        this.websiteTextView.setClickable( true );
        this.websiteTextView.setMovementMethod( LinkMovementMethod.getInstance() );
        this.loadingCounter = (AtomicInteger) getIntent().getExtras().getSerializable( "loadingCounter" );
        
        String dollars = priceToDollar( getIntent().getExtras().getInt( "price" ) );
        
        // Don't display dollar if it isn't there so we save space
        if ( dollars.isEmpty() )
            this.priceTextView.setVisibility( View.GONE );
        else
            this.priceTextView.setText( dollars );

        String website = getIntent().getExtras().getString( "website" );
        
        if ( website != null )
            this.websiteTextView.setText( Html.fromHtml( "<a href=" + website + ">" + website ) );
        
        new PhotoTask( getIntent().getExtras().getString( "photoRef" ) ).execute();
    }
    
    @Override
    protected void onDestroy() {
    	
    	for ( PhotoTask task : this.photoTasks )
    		task.cancel( true );
    	
    	super.onDestroy();
    }
    
    // Async Task to get Google Places Photo
    class PhotoTask extends AsyncTask<Void, Void, PlacePhoto> {

        private String photoReference;

        public PhotoTask( String photoRef ) {

            this.photoReference = photoRef;
        }
        
        @Override
        protected void onPreExecute() {
        	
        	loadingCounter.addAndGet( 1 );
        	
        	if ( loadingCounter.get() == 1 )
        		loadingText.setVisibility( View.VISIBLE );
        }

        @Override
        protected PlacePhoto doInBackground( Void... unused ) {

            return gpSearch.findPlacePhoto( photoReference );
        }

        @Override
        protected void onPostExecute( PlacePhoto placePhoto ) {
            
        	if ( isCancelled() )
        		return;
        	
        	loadingCounter.addAndGet( -1 );
        	
        	if ( loadingCounter.get() == 0 )
        		loadingText.setVisibility( View.GONE );
        	
            // Set Photo ImageView
            if ( placePhoto.photo != null )
                photoImageView.setImageBitmap( placePhoto.photo );
            else
                photoImageView.setVisibility( View.GONE );
        }

    }
    
    /*
     * Rating to Star
     * Description: Creates a star rating based off
     *              some integer out of 5.
     */
    private String ratingToStar( int rating ) {
        
        String result = "";
        
        for ( int i = 0; i < rating; i++ )
            result += "\u2605";
        
        if ( !result.isEmpty() )
            for ( int i = 0; i < 5-rating; i++ )
                result += "\u2606";
        else
            result = "No Rating";
        
        return result;
    }
    
    /*
     * Price to Dollar
     * Description: Creates a dollar representation
     *              of a price level.
     */
    private String priceToDollar( int price ) {
        
        String result = "";
        
        for ( int i = 0; i < price; i++ )
            result += "\u0024";
            
        return result;
    }

}
