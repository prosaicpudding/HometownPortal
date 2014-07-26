package com.android.projecte.townportal;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;


/*
 * Employment Activity
 * Description: A Feed Activity that gets events from NPR
 */
final public class EventsActivity extends FeedActivity {
    
    private String eventsSource;

    
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate( Bundle savedInstanceState ) {

        super.onCreate( savedInstanceState );
        
     // JavaScript makes the Monster mobile site function better
        this.webView.getSettings().setJavaScriptEnabled( true );
        
        // Get strings        
        this.title = getString( R.string.events_text );
       
                
        // Set title and courtesy

        this.courtesyText.setText( getString( R.string.eventsCourtesy ) );
          
    	this.seeMoreUrl = getString( R.string.eventsViewMore );
    	this.eventsSource = getString( R.string.eventsRss );
		new FeedTask( this.context ).execute();	
       
    }
    

	/*
     * Get Web Contents
     * Descriptions: Retrieves a page from a given URL.
     */
    private InputStream getWebContents( String url ) {
        
        DefaultHttpClient client = new DefaultHttpClient();
        
        try {
            
            // Get HTML from URL
            HttpResponse response = client.execute( new HttpGet( url ) );
            HttpEntity message = response.getEntity();
            return message.getContent();
            
        } catch ( ClientProtocolException e ) {
            
            e.printStackTrace();
            
        } catch ( IOException e ) {
            
            e.printStackTrace();
        }
       
        return null;
    }

    @Override
    /*
     * Get Items
     * Description: Gets a list of items for display in the ListView.
     */
    protected List<Item> getItems() {
        
        List<Item> result = new Vector<Item>();

        // Create Document from RSS content
        try {
            
            // Used http://jsoup.org/cookbook/ to figure out how to parse
            Document rssDoc = Jsoup.parse( getWebContents( this.eventsSource ), "UTF-8", "", Parser.xmlParser() );
            
            Elements jobItems = rssDoc.select("item");
            
            for ( Element element : jobItems ) {
                
                String title = element.select( "title" ).get( 0 ).text();
                String description = element.select( "description" ).get( 0 ).text();
                String link = getString( R.string.eventsViewMore );

                if ( title != null && link != null && description != null )
                    result.add( new Item( title, description, link ) );
            }
            
        } catch ( IOException e ) {
            
            e.printStackTrace(); 
        }
        
        return result;
    }
    

}