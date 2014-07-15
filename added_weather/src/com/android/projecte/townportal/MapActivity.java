/* MapActivity.java
 * Project E - Eric Daniels
 */

package com.android.projecte.townportal;

import java.util.concurrent.atomic.AtomicInteger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.Window;
import android.widget.TextView;

/*
 * Map Activity
 * Description: Used with Google Maps activity page to display tabs which are
 *              sub-categories of a user selected category.
 * Note: Uses fragments now to avoid deprecation
 *       http://developer.android.com/guide/components/fragments.html
 */
public class MapActivity extends FragmentActivity {

	// Multiple fragments can make loadingText appear and disappear
    // so we need to make sure that if anyone is loading that it stays
    // there.
    private AtomicInteger loadingCounter = new AtomicInteger( 0 );
	
    @Override
    protected void onCreate( Bundle savedInstanceState ) {

        super.onCreate( savedInstanceState );
        
        // http://stackoverflow.com/questions/2736389/how-to-pass-object-from-one-activity-to-another-in-android
        Intent intent = getIntent();
        String title = ( String ) intent.getSerializableExtra( "title" );
        PlaceType pt1 = (PlaceType) intent.getSerializableExtra( "PlaceType1" );
        PlaceType pt2 = (PlaceType) intent.getSerializableExtra( "PlaceType2" );
        PlaceType pt3 = (PlaceType) intent.getSerializableExtra( "PlaceType3" );

        // Use custom title bar
        requestWindowFeature( Window.FEATURE_CUSTOM_TITLE );
        setContentView( R.layout.activity_map_tabs );
        getWindow().setFeatureInt( Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title );
        ((TextView) findViewById( R.id.title ) ).setText( title );

        // Set up TabHost
        FragmentTabHost tabHost = (FragmentTabHost) findViewById( android.R.id.tabhost );
        tabHost.setup( this, this.getSupportFragmentManager(), R.id.realtabcontent );

        Bundle args = new Bundle();
        
        // Tab 1 - first tab is required, others may be null
        args.putString( "type", pt1.googleName );
        args.putSerializable( "loadingCounter" , loadingCounter );
        tabHost.addTab( tabHost.newTabSpec( pt1.displayName ).setIndicator( pt1.displayName ), GooglePlacesMap.class, args );

        // Tab 2
        if ( pt2 != null ) {
            
            args = new Bundle();
            args.putString( "type", pt2.googleName );
            args.putSerializable( "loadingCounter" , loadingCounter );
            tabHost.addTab( tabHost.newTabSpec( pt2.displayName ).setIndicator( pt2.displayName ), GooglePlacesMap.class, args );
        }

        // Tab 3
        if ( pt3 != null ) {
            
            args = new Bundle();
            args.putString( "type", pt3.googleName );
            args.putSerializable( "loadingCounter" , loadingCounter );
            tabHost.addTab( tabHost.newTabSpec( pt3.displayName ).setIndicator( pt3.displayName ), GooglePlacesMap.class, args );
        }

        tabHost.setCurrentTab( 0 );

        // loop through all tab views and set height value
        // http://www.speakingcode.com/2011/10/17/adjust-height-of-android-tabwidget/
        int heightValue = 30;
        for ( int i = 0; i < tabHost.getTabWidget().getTabCount(); i++ )
            tabHost.getTabWidget().getChildAt( i ).getLayoutParams().height = 
                (int) ( heightValue * this.getResources().getDisplayMetrics().density );

    }
}
