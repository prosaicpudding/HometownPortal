/* GooglePlacesSearch.java
 * Project E - Eric Daniels
 */

package com.android.projecte.townportal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/*
 * Google Places Search
 * Description: Uses JSON query to retrieve Google Places information 
 *              about places with a type selected by user.
 */
public class GooglePlacesSearch {

    public String location,
                  radius = "16100", // in meters - about 10 miles
                  types,
                  sensor = "false",
                  APIKey = "AIzaSyBz7p2E8oDDBYJYvL3RM3cFjHCJDkpuqwU",
                  reference = null;
    
    BitmapFactory.Options bmOptions;
    
    public GooglePlacesSearch( String placeType, String geoLocation, int radius ) {

        this.types = placeType;
        this.location = geoLocation;
        this.radius = Integer.toString( radius );
    }

    /*
     * Form Google Search URL
     */
    private String formGoogleSearchURL() {

        String returnVal = new String();

        returnVal = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                    + this.location + "&radius=" + this.radius + "&types=" + this.types
                    + "&sensor=" + this.sensor + "&key=" + this.APIKey;

        return returnVal;
    }

    /*
     * Find Places
     * Descriptions: Finds and returns a list of places of a certain type in a
     *               selected location
     */
    public ArrayList<Place> findPlaces() {

        ArrayList<Place> arrayList = null;
        
        String urlString = formGoogleSearchURL();

        try {
            
            String json = getURLContent( urlString );
            JSONObject object = new JSONObject( json );
            JSONArray array = object.getJSONArray( "results" );

            arrayList = new ArrayList<Place>();
            
            // Add all results as Places
            for ( int i = 0; i < array.length(); i++ )
                arrayList.add( Place.jsonToPlace( (JSONObject) array.get( i ) ) );
            
            return arrayList;
            
        } catch ( JSONException e ) {
            
            e.printStackTrace();
            
        } catch ( NullPointerException e ) {
        	
        	e.printStackTrace();
        }
        
        return arrayList;
    }

    /*
     * Get URL Content
     * Description: Gets content from a specified URL
     */
    private String getURLContent( String url ) {

        String result = null;
        
        DefaultHttpClient client = new DefaultHttpClient();
        
        try {
            
            // Get HTML from URL
            HttpResponse response = client.execute( new HttpGet( url ) );
            InputStream messageContent = response.getEntity().getContent();
            
            StringBuilder content = new StringBuilder( messageContent.available() );
            BufferedReader reader = new BufferedReader( new InputStreamReader( messageContent ) );
            String readLine;
            
            while ( ( readLine = reader.readLine() ) != null )
                content.append( readLine + "\n" );
            
            result = content.toString();
            
        } catch ( ClientProtocolException e ) {
        	
        	e.printStackTrace();
        	
        } catch ( UnknownHostException e ) {
        	
        	e.printStackTrace();
        	
    	} catch (ConnectException e) {
    		
    		e.printStackTrace();
    		
    	} catch ( IOException e ) {
    		
    		e.printStackTrace();
    		
        } catch ( Exception e ) {
        	
        	e.printStackTrace();
        } 
        
        return result;
    }

    /*
     * Get Place Detail URL
     * Description: Generates URL from a place reference
     */
    private String getPlaceDetailUrl( String placeRef ) {

        String returnVal = new String();

        returnVal = "https://maps.googleapis.com/maps/api/place/details/json?reference=";
        returnVal += placeRef + "&sensor=" + this.sensor + "&key=" + this.APIKey;

        return returnVal;
    }

    /*
     * Get Place Detail from Photo URL
     */
    private String GetPlaceDetailPhotoUrl( String photoRef ) {

        String returnVal = new String();
        returnVal = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400";
        returnVal += "&photoreference=" + photoRef + "&sensor=" + this.sensor + "&key=" + this.APIKey;

        return returnVal;
    }

    /*
     * Find Place Detail
     * Description: Gets PlaceDetails from Google Places passing Places reference.
     */
    public PlaceDetail findPlaceDetail( String placeRef ) {

        PlaceDetail placeDetail = null;
        String urlString = getPlaceDetailUrl( placeRef );

        try {
            
            String json = getURLContent( urlString );
            JSONObject object = new JSONObject( json );
            JSONObject result = object.getJSONObject( "result" );

            placeDetail = PlaceDetail.jsonToPlaceDetail( result );

        } catch ( JSONException e ) {
        	
            e.printStackTrace();
            
        } catch ( NullPointerException e ) {
        
        	e.printStackTrace();
        }
        
        return placeDetail;
    }

    /*
     * Find Place Photo
     * Description: Gets Places Photo from Google Photos passing photo reference.
     */
    public PlacePhoto findPlacePhoto( String photoReference ) {

        PlacePhoto placePhoto = new PlacePhoto();
        String urlString = GetPlaceDetailPhotoUrl( photoReference );
        
        try {
            
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI( new URI( urlString ) );
                
            Bitmap photo = BitmapFactory.decodeStream( client.execute( request ).getEntity().getContent() );

            placePhoto.photo = photo;
    
        } catch ( URISyntaxException e ) {
        
            e.printStackTrace();
        
        } catch ( ClientProtocolException e ) {
            
            e.printStackTrace();
        
        } catch ( IOException e ) {
            
            e.printStackTrace();
        }
        
        return placePhoto;
    }

}
