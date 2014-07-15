/* Place.java
 * Project E - Eric Daniels
 */

package com.android.projecte.townportal;

import org.json.JSONException;
import org.json.JSONObject;

/*
 * Place
 * Description: Data structure that holds Google Place data.
 */
public class Place {

    public String id, icon, name, vicinity, placeReference;
    public Double latitude, longitude, rating;
    public Integer price;
    
    static Place jsonToPlace( JSONObject toPlace ) {

        Place result = null;
        
        try {
            
            JSONObject location = (JSONObject) ( (JSONObject) toPlace.get( "geometry" ) ).get( "location" );
            
            // Create and set place
            result = new Place();
            result.latitude = (Double) location.get( "lat" );
            result.longitude = (Double) location.get( "lng" );
            result.icon = toPlace.getString( "icon" );
            result.name = toPlace.getString( "name" );
            result.vicinity = toPlace.getString( "vicinity" );
            result.id = toPlace.getString( "id" );
            result.placeReference = toPlace.getString( "reference" );
            result.rating = toPlace.has( "rating" ) ? toPlace.getDouble( "rating" ) : 0;
            result.price = toPlace.has( "price_level" ) ? toPlace.getInt( "price_level" ) : 0;

        } catch ( JSONException e ) {
            
            e.printStackTrace();
        }
        
        return result;
    }

    @Override
    public String toString() {
        
        return this.name;
    }
}