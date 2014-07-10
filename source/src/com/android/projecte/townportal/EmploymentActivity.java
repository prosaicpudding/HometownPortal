/* EmploymentActivity.java
 * Project E - Eric Daniels
 */

package com.android.projecte.townportal;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * Description: A Feed Activity that gets job listings from Monster.
 */
final public class EmploymentActivity extends FeedActivity {
    
    private String jobsSource;
    
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate( Bundle savedInstanceState ) {

        super.onCreate( savedInstanceState );
        
        // JavaScript makes the Monster mobile site function better
        this.webView.getSettings().setJavaScriptEnabled( true );
        
        // Get strings
        this.jobsSource = getString( R.string.jobsRss );
        this.title = getString( R.string.empl_text );
        this.seeMoreUrl = getString( R.string.jobsViewMore );
        
        // Set title and courtesy
        ((TextView) findViewById( R.id.title ) ).setText( this.title );
        this.courtesyText.setText( getString( R.string.emplCourtesy ) );
        
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
            Document rssDoc = Jsoup.parse( getWebContents( this.jobsSource ), "UTF-8", "", Parser.xmlParser() );
            
            Elements jobItems = rssDoc.select("item");
            
            for ( Element element : jobItems ) {
                
                String title = element.select( "title" ).get( 0 ).text();
                String description = element.select( "description" ).get( 0 ).text();
                String link = element.select( "link" ).get( 0 ).text();

                if ( title != null && link != null && description != null )
                    result.add( new Item( title, description, link ) );
            }
            
        } catch ( IOException e ) {
            
            e.printStackTrace(); 
        }
        
        return result;
    }
    
    @Override
    protected String modifyUrl( String url ) {
        
        // Transform link to mobile version for visibility purposes
        url = url.replace( "jobview", "m" );
        
        Matcher matcher = Pattern.compile(".com/.*-([0-9]+)\\.aspx").matcher( url );
        
        // Should always return true
        if ( matcher.find() )
            url = matcher.replaceFirst( ".com/" + matcher.group( 1 ) );
        
        return url;
    }
}
