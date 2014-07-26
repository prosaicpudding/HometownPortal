/* NewsActivity.java
 * Project E - Eric Daniels
 */

package com.android.projecte.townportal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.Bundle;
import android.widget.TextView;

/*
 * News Activity
 * Description: A Feed Activity that gets news from The News Herald.
 */
final public class NewsActivity extends FeedActivity {

    private String newsSource;
    
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        
        super.onCreate( savedInstanceState );
        
        // Get strings
        this.newsSource = getString( R.string.newsSource );
        this.title = getString( R.string.news_text );
        this.seeMoreUrl = getString( R.string.newsViewMore );
        
        // Set title and courtesy

        this.courtesyText.setText( getString( R.string.newsCourtesy ) );
        
        new FeedTask( this.context ).execute();
    }
    
    @Override
    protected List<Item> getItems() {
        
        List<Item> result = new Vector<Item>();
        
        try {
            
            // Download news items
            Document htmlDoc = Jsoup.connect( String.format( newsSource + "default.aspx?section=%s", 
                    getString( R.string.topNews ) ) ).get();
            
            Elements newsItems = htmlDoc.select( "li[data-icon]" );
            
            // Iterate through all items
            for ( Element element : newsItems ) {
                
                // Used http://jsoup.org/cookbook/ to figure out how to parse
                String title = element.select( "div[id=storySummary] h1, h2, h3, h4, h5, h6" ).get( 0 ).text();
                String description = element.select( "div[id=storySummary] p" ).get( 0 ).text();
                String link = null;
                
                // Get true link
                List<NameValuePair> uriPairs = URLEncodedUtils.parse( 
                        new URI( newsSource + element.select( "a" ).get( 0 ).attr( "href" ) ), "UTF-8" );
                
                // Find link within pairs
                for ( NameValuePair nvp : uriPairs ) {
                    
                    if ( nvp.getName().equalsIgnoreCase( "link" ) ) {
                        
                        link = nvp.getValue();
                        break;
                    }
                }

                // Add the item if applicable
                if ( title != null && link != null && description != null )
                    result.add( new Item( title, description, link ) );
            }
            
        } catch ( IOException e ) {
            
            e.printStackTrace();
            
        } catch ( URISyntaxException e ) {
            
            e.printStackTrace();
        }

        return result;
    }
    
    @Override
    protected String modifyUrl( String url ) {
        
        // Use mobile version of article for visibility purposes
        return url.replaceFirst( "www", "m" );
    }
}
