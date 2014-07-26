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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Employment Activity
 * Description: A Feed Activity that gets job listings from Monster.
 */
final public class EmploymentActivity extends FeedActivity {
    
    private String jobsSource;
    private EditText job, keyword, location; 
    private AlertDialog dialog;
    
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate( Bundle savedInstanceState ) {

        super.onCreate( savedInstanceState );
        
     // JavaScript makes the Monster mobile site function better
        this.webView.getSettings().setJavaScriptEnabled( true );
        
        // Get strings        
        this.title = getString( R.string.empl_text );
       
                
        // Set title and courtesy

        this.courtesyText.setText( getString( R.string.emplCourtesy ) );
        
        //add job search dialog
        addDialog();  
        
       
    }
    
    /*
     * Add dialog
     * Description: initializes job search dialog
     */
    private void addDialog() {
    	//Build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        
        LayoutInflater inflater =  ((Activity) this.context).getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.search_jobs, null));
        
        		
       builder
        .setCancelable(false)
        .setTitle(getString(R.string.job_search_title))
        .setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
								
				getTextFromDialog();
				
			}
		})
		
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				defaultToPanamaCity();
							
			}
		});
        
        dialog = builder.create();
        
        dialog.show();
        job = (EditText) dialog.findViewById(R.id.jobTitle);
		keyword = (EditText) dialog.findViewById(R.id.keyword);
		location = (EditText) dialog.findViewById(R.id.location);
		
	}
/*
 * Default To Panama City
 * Description: shows Panama City job listings if dialog is canceled 
 */
	protected void defaultToPanamaCity() {
		Toast toast =Toast.makeText( getApplicationContext(), "Defaulting to Panama City Job Listing", Toast.LENGTH_SHORT );
		toast.setGravity( Gravity.CENTER_HORIZONTAL, 0, 0 );
        toast.show();
        
    	this.seeMoreUrl = getString( R.string.jobsViewMore );
    	this.jobsSource = getString( R.string.jobsRss );
		new FeedTask( this.context ).execute();	
	}

	/*
	 * Get Text Dialog
	 * Description: get text from editTexts in dialog. set urls and start task
	 */
	protected void getTextFromDialog() {
		String[] editTexts = new String[3];
		editTexts[0]= job.getText().toString();
		editTexts[1] = keyword.getText().toString();
		editTexts[2] = location.getText().toString();
		
		for(int i=0; i<editTexts.length; i++){
			if(!editTexts[i].matches("")) {
				editTexts[i]= editTexts[i].replaceAll(" ", "+");
				editTexts[i]= editTexts[i].replaceAll(",", "");
			}
		}
		
		this.seeMoreUrl = "http://m.monster.com/JobSearch/Search?jobtitle=" + editTexts[0] + 
				"&keywords=" + editTexts[1] + "&where=" + editTexts[2];
		
		this.jobsSource = "http://rss.jobsearch.monster.com/rssquery.ashx?jobtitle=" + editTexts[0] + 
				"&keywords=" + editTexts[1] + "&where=" + editTexts[2];
		
		
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
