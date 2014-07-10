/* FeedActivity.java
 * Project E - Eric Daniels
 */

package com.android.projecte.townportal;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/*
 * Feed Activity
 * Description: An abstract Activity whose purpose is to generate a
 *              dynamic ListView of items from some feed source.
 */
public abstract class FeedActivity extends Activity {

    protected List<Item> items = new Vector<Item>();
    protected ArrayAdapter<Item> adapter;
    protected ListView list;
    protected WebView webView;
    protected TextView courtesyText, titleText, loadingText;
    protected View divider;
    protected Boolean viewingItem = false;
    protected String title, seeMoreUrl;
    
    private int savedFirstVisiblePosition = 0;
    
    final private Integer MAX_DESC_LENGTH = 200;
    
    protected Context context = this;
    private List<FeedTask> feedTasks = new Vector<FeedTask>();
    
    @Override
    protected void onCreate( Bundle savedInstanceState ) {

        super.onCreate( savedInstanceState );
        
        // Use custom title bar
        requestWindowFeature( Window.FEATURE_CUSTOM_TITLE );
        setContentView( R.layout.activity_feed );
        getWindow().setFeatureInt( Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title );
        
        // Get Views
        this.list = (ListView) findViewById( R.id.feedList );
        this.webView = (WebView) findViewById( R.id.feedWebView );
        this.courtesyText = (TextView) findViewById( R.id.feedCourtesy );
        this.titleText = (TextView) findViewById( R.id.title );
        this.loadingText = (TextView) findViewById( R.id.loading );
        this.divider = findViewById( R.id.feedDivider );
        
        // Create custom adapter
        this.adapter = new ArrayAdapter<Item>( this, android.R.layout.simple_list_item_2, this.items ) {
            
            @Override
            // Support shading and two text items
            public View getView( int position, View convertView, ViewGroup parent ) {
                
                // Got some help from http://stackoverflow.com/questions/11722885/what-is-difference-between-android-r-layout-simple-list-item-1-and-android-r-lay
                
                Item item = (Item) this.getItem( position );
                
                convertView =  ( (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE) )
                        .inflate( android.R.layout.simple_list_item_2, parent, false );
                
                TextView text1 = (TextView) convertView.findViewById( android.R.id.text1 ), 
                         text2 = (TextView) convertView.findViewById( android.R.id.text2 );
                
                text1.setText( item.title );
                
                // Center see more text
                if ( ( ( position + 1 ) == items.size() && item.title.equals( "See More" ) )
                        || ( position == 0 && item.title.equals( "Refresh" ) ) ) {
                    
                    text1.setGravity( Gravity.CENTER );
                    text1.setTextColor( getContext().getResources().getColor( R.color.darkBlue ) );
                    
                }
                
                // Shorten description
                String description = item.description;
                
                if ( description != null && description.length() > MAX_DESC_LENGTH )
                    description = description.substring( 0, MAX_DESC_LENGTH ) + "\u2026";
                
                text2.setText( description );
                
                // Gray out even items
                if( ( position % 2 ) != 0 )
                    convertView.setBackgroundResource( R.color.gray );
                
                return convertView;
            }
        };
        
        this.list.setAdapter( this.adapter );
        
        this.list.setOnItemClickListener( new OnItemClickListener() {

            @Override
            public void onItemClick( AdapterView<?> adapterView, View view, int position, long id ) {

                Item item = (Item) adapterView.getItemAtPosition( position );
                
                // See More and Refresh functionality
                if ( ( position + 1 ) == items.size() && item.title.equals( "See More" ) ) {
                    
                    // Load into outside browser
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData( Uri.parse( seeMoreUrl ) );
                    startActivity( intent );
                    
                    return;
                    
                } else if ( position == 0 && item.title.equals( "Refresh" ) ) {
                    
                    // Get items again
                    new FeedTask( context ).execute();
                    return;
                }
                
                // Show WebView only
                viewingItem = true;
                titleText.setText( R.string.returnText );
                
                list.setVisibility( View.GONE );
                divider.setVisibility( View.GONE );
                courtesyText.setVisibility( View.GONE );
                loadingText.setVisibility( View.VISIBLE );
                
                webView.loadUrl( modifyUrl( item.link ) );
                webView.setVisibility( View.VISIBLE );
            }    
            
        });
        
        // Set custom WebViewClient
        this.webView.setWebViewClient( new WebViewClient() {
            
            @Override
            public boolean shouldOverrideUrlLoading (WebView view, String url) {
                
                // We need to start a new browser otherwise we will keep loading pages in the
                // WebView. The override is necessary because adding a custom WebViewClient takes
                // away this standard behavior without one.
                // Found out how the standard way is done through:
                // http://stackoverflow.com/questions/14665671/android-webview-open-certain-urls-inside-webview-the-rest-externally
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData( Uri.parse( url ) );
                startActivity( intent );
                
                return true;
            }
            
            @Override
            public void onPageFinished( WebView webview, String url ){
                
                super.onPageFinished( webview, url );
                
                loadingText.setVisibility( View.INVISIBLE );
            }
        });
    }
    
    @Override
    protected void onDestroy() {
    	
    	for ( FeedTask task : this.feedTasks )
    		task.cancel( true );
    	
    	super.onDestroy();
    }
    
    @Override
    protected void onSaveInstanceState( Bundle outState ) {
        
        // Save WebView and viewing state
        this.webView.saveState( outState );
        outState.putBoolean( "viewingItem", this.viewingItem );
        outState.putInt( "firstVisiblePosition", this.list.getFirstVisiblePosition() );
        
        super.onSaveInstanceState(outState);     
    }

    @Override
    protected void onRestoreInstanceState( Bundle state ) {
        
        // Load WebView and viewing state
        this.webView.restoreState( state );
        this.viewingItem = state.getBoolean( "viewingItem" );
        this.savedFirstVisiblePosition = state.getInt( "firstVisiblePosition" );
        
        // Check to see if we should keep showing article
        if ( this.viewingItem ) {

            this.titleText.setText( R.string.returnText ); 
            
            this.list.setVisibility( View.GONE );
            this.divider.setVisibility( View.GONE );
            this.courtesyText.setVisibility( View.GONE );
            this.webView.setVisibility( View.VISIBLE );
            this.loadingText.setVisibility( View.VISIBLE );
        
        }
        
        super.onRestoreInstanceState( state );    
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        // Specially handle back button while viewingItem
        if ( keyCode == KeyEvent.KEYCODE_BACK && this.viewingItem ) {
           
            this.webView.setVisibility( View.GONE );
            this.list.setVisibility( View.VISIBLE );
            this.divider.setVisibility( View.VISIBLE );
            this.courtesyText.setVisibility( View.VISIBLE );
            
            this.viewingItem = false;
            this.titleText.setText( title );
            
            this.webView.loadUrl("about:blank");
            
            return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }

    /*
     * Feed Task
     * Description: Grabs items from a specified feed implemented by
     *              a child class.
     */
    protected class FeedTask extends AsyncTask<Void, Void, List<Item>> {

    	private Context context;
    	
    	public FeedTask( Context c ) {
    		
    		this.context = c;
    	}
    	
        @Override
        protected List<Item> doInBackground( Void... arg0 ) {
            
        	List<Item> result = null;
        	
        	try {
            
        		result = getItems();
        	
        	} catch ( UnknownHostException e ) {
        		
        		e.printStackTrace();
        		
        	} catch ( ConnectException e ) {
        		
        		e.printStackTrace();
        		
			} catch ( Exception e ) {
				
				e.printStackTrace();
			} 
        	
        	return result;
        }
        
        @Override
        protected void onPreExecute() {
            
        	feedTasks.add( this );
            loadingText.setVisibility( View.VISIBLE );
        }
        
        @Override
        protected void onPostExecute( List<Item> result ) {
            
        	if ( isCancelled() )
        		return;
        	
        	// Make sure we received something
        	if ( result != null && !result.isEmpty() ) {
        		
            	// Allow user to refresh
        		result.add( 0, new Item( "Refresh", null, null ) );
                
                // Let user see more jobs
        		result.add( new Item( "See More", null, null ) );
        		
        		// Reset and add new items
                adapter.clear();
                
                for ( int i = 0 ; i < result.size(); ++i )
                    adapter.add( result.get( i ) );
                
                adapter.notifyDataSetChanged();
                
                loadingText.setVisibility( View.INVISIBLE );
                
                list.setSelection( savedFirstVisiblePosition );
        	
        	} else {
        		
        		AlertDialog.Builder builder = new AlertDialog.Builder( context );
        		builder.setMessage( "Failed to retrieve feed. Check your internet connection" ).setTitle( "Error" )
        			.setPositiveButton( "Ok", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {}
					} );
        		
        		builder.create().show();
        		
        		// Add a special list with only refresh
        		result = new Vector<Item>();
        		
        		// Allow user to refresh
        		result.add( 0, new Item( "Refresh", null, null ) );
        		
        		// Reset and add new items
                adapter.clear();
                
                adapter.add( result.get( 0 ) );
                
                adapter.notifyDataSetChanged();
                
                loadingText.setVisibility( View.INVISIBLE );
        	}
        }
    }

    /*
     * Item
     * Description: Basic structure for storing feed data
     */
     protected class Item {

        protected String title, description, link;

        public Item( String title, String description, String link ) {

            this.title = title;
            this.description = description;
            this.link = link;
        }
    }

    /*
     * Modify URL
     * Description: Modifies a URL before loading it into the WebView.
     */
    protected String modifyUrl( String url ) {
        
        return url;
    }
    
    abstract protected List<Item> getItems() throws UnknownHostException, ConnectException, Exception;
}
