/* NewsActivity.java
 * Project E - Eric Daniels
 */

package com.android.projecte.townportal;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.Bundle;

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
            
            Elements newsItems = htmlDoc.select( "a[class=list-group-item]" );
            
            // Iterate through all items
            for ( Element element : newsItems ) {
                
                // http://jsoup.org/apidocs/org/jsoup/select/Selector.html
                String title       = element.select( ".articleTitle" ).get( 0 ).text();
                String description = element.select(".articleSum").get(0).text();
                String link        = null;
                String lousy_link  = element.select( "a" ).get( 0 ).attr( "href" );

                if(lousy_link != null)
                {
                	// lousy_link is sometimes only partially encoded. Making it uniform.
                	link = URLDecoder.decode(newsSource + lousy_link, "UTF-8");
                }
                
                // Add the item if applicable
                if ( title != null && link != null && description != null )
                {
                    result.add( new Item( title, description, link ) );
                }
            }
            
        } catch ( IOException e ) {
            
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
