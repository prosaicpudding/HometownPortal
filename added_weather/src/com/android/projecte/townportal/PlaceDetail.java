/* PlaceDetail.java
 * Project E - Eric Daniels
 */

package com.android.projecte.townportal;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;

/*
 * Place Detail
 * Description: Data structure used to store details of a place
 *              in a Place Detail Activity.
 */
public class PlaceDetail {

    public String phoneNumber = null, address = null, website = null, photoRef = null, siteName = null;
    public Bitmap sitePhoto = null;
    
    // Create PlaceDetail from JSON
    static PlaceDetail jsonToPlaceDetail( JSONObject result ) {

        PlaceDetail placeDetail = null;

        try {
            
            // Create and set place detail
            placeDetail = new PlaceDetail();
        
            if ( !result.isNull( "photos" ) ) {
                
                JSONObject photo = result.getJSONArray( "photos" ).getJSONObject( 0 );
                placeDetail.photoRef = photo.getString( "photo_reference" );
            }
        
            if ( !result.isNull( "formatted_phone_number" ) )
                placeDetail.phoneNumber =  result.getString( "formatted_phone_number" );;
        
            if ( !result.isNull( "formatted_address" ) )
                placeDetail.address = result.getString( "formatted_address" );;
        
            if ( !result.isNull( "website" ) )
                placeDetail.website = result.getString( "website" );;
        
            if ( !result.isNull( "name" ) )
                placeDetail.siteName = result.getString( "name" );
            
        }
        
        catch ( JSONException e ) {

            e.printStackTrace();
        }
        
        return placeDetail;
   }
    
}
