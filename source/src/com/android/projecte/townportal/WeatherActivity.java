package com.android.projecte.townportal;



import org.json.JSONArray;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
/*
 * WeatherActivity
 *  
 * help from: https://github.com/survivingwithandroid/WeatherLib/blob/master/demo/
 */

public class WeatherActivity extends Activity implements AdapterView.OnItemSelectedListener {

	private final String cityID ="4167694";
	private String bestProvider, url, url2;
	private LocationManager locationManager;
	private Spinner spinner;
	private Location locationDetails;
	private static TextView cityText, condDescr, temp, press, windSpeed, unitTemp, hum, tempMin, tempMax, 
	sunset, sunrise, cloud;
	//ot , titleText;
	private static ImageView imgView;
	protected static ListView forecastList;
	private WeatherInfo info2;
	private final String searchUrl = "http://api.openweathermap.org/data/2.5/weather?", searchCoor ="lat=" ,
				searchCoor2 = "&lon=", searchId ="id=", searchForUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?",
				searchCount="&cnt=7";

	
	 protected void onCreate( Bundle savedInstanceState ) {

	        super.onCreate( savedInstanceState );
	       //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	        // Use custom title bar

	        setContentView( R.layout.activity_weather );



	        
	        info2 = new WeatherInfo();
	        url = searchUrl + searchId + cityID;
	        startSpinner();
	        new JSONParse().execute();
	        url2 = searchForUrl + searchId + cityID + searchCount;
	        getForecast();
	 }

	 private void getForecast() {
		 new WeatherForecast(url2, getApplication());	
	}

	private class JSONParse extends AsyncTask<String, String, JSONObject> {
	       
	      @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            startViews();
	            
	      }
	      
	      @Override
	        protected JSONObject doInBackground(String... args) {
	  		JSONObject json = info2.getJSONFromUrl(url);
	        return json;
	      }
	      
	       @Override
	         protected void onPostExecute(JSONObject json) {
	         	         try {
	     
	        	 JSONObject JSONObject_sys = json.getJSONObject("sys");
			     JSONArray JSONArray_weather = json.getJSONArray("weather");
			     JSONObject JSONObject_weather = JSONArray_weather.getJSONObject(0);
			     JSONObject JSONObject_main = json.getJSONObject("main"); 
			     JSONObject JSONObject_wind = json.getJSONObject("wind");
			     JSONObject JSONObject_clouds = json.getJSONObject("clouds");
	           
			        String result_icon = JSONObject_weather.getString("icon");
			        String result_main = JSONObject_weather.getString("main");
		            String result_description = JSONObject_weather.getString("description");
			        String result_country = JSONObject_sys.getString("country");
				    int result_sunrise = JSONObject_sys.getInt("sunrise");
				    int result_sunset = JSONObject_sys.getInt("sunset");
				    String result_name = json.getString("name");
			        Double result_temp = JSONObject_main.getDouble("temp");     
			        Double result_pressure = JSONObject_main.getDouble("pressure");		         
			        Double result_humidity = JSONObject_main.getDouble("humidity");	        
			        Double result_temp_min = JSONObject_main.getDouble("temp_min");		         
			        Double result_temp_max = JSONObject_main.getDouble("temp_max");
			        Double result_speed = JSONObject_wind.getDouble("speed");
			        int result_cloud = JSONObject_clouds.getInt("all");
			        
	            //Set JSON Data in TextView and ImageView
	            sunrise.setText(info2.convertDate(result_sunrise));
		        sunset.setText(info2.convertDate(result_sunset));
		        cityText.setText(result_name + "," + result_country);
		        condDescr.setText(result_main + "(" + result_description + ")");
		        temp.setText("" + Double.toString(Math.round(info2.convertTemperature(result_temp))) );
		        unitTemp.setText("°F");
		        press.setText(Double.toString(result_pressure)); 
		        hum.setText(result_humidity + "%");
		        tempMin.setText(Double.toString(Math.floor(info2.convertTemperature(result_temp_min))) + "°F");
		        tempMax.setText(Double.toString(Math.ceil(info2.convertTemperature(result_temp_max))) +"°F");
		        windSpeed.setText(info2.convertSpeed(result_speed) + "mph");
		        cloud.setText(Integer.toString(result_cloud) + "%");
		        imgView.setImageResource(info2.getWeatherResource(result_icon));
		         
	        } catch (Exception e) {
	          e.printStackTrace();
	        }
	       }
	    }
	
	 
//Initialize textViews and imageViews
	private void startViews() {
			
			cityText = (TextView) findViewById(R.id.location);
	        temp = (TextView) findViewById(R.id.tempBig);
	        condDescr = (TextView) findViewById(R.id.descrWeather);
	        imgView = (ImageView) findViewById(R.id.imgWeather);
	        hum = (TextView) findViewById(R.id.humidity);
	        press = (TextView) findViewById(R.id.pressure);
	        windSpeed = (TextView) findViewById(R.id.windSpeed);
	        tempMin = (TextView) findViewById(R.id.tempMin);
	        tempMax = (TextView) findViewById(R.id.tempMax);
	        unitTemp = (TextView) findViewById(R.id.tempUnit);
	        sunrise = (TextView) findViewById(R.id.sunrise);
	        sunset = (TextView) findViewById(R.id.sunset);
	        cloud = (TextView) findViewById(R.id.cloud);
	        forecastList = (ListView) findViewById(R.id.forecastDays);
		
	}
	
	//Initialize spinner
	private void startSpinner() {
		spinner = (Spinner) findViewById(R.id.spinner2);
		spinner.setOnItemSelectedListener(this);
		spinner.setSelection(1);
		
	}

 //Listener for spinner
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(0 == spinner.getSelectedItemPosition()){
			 locationManager = (LocationManager) getApplicationContext().getSystemService( Context.LOCATION_SERVICE );
			 if ( this.locationManager == null ) {
	                
		            Toast toast = Toast.makeText( getApplicationContext(), "error: Failed to use the Location Service.", Toast.LENGTH_SHORT );
		            toast.setGravity( Gravity.CENTER_HORIZONTAL, 0, 0 );
		            toast.show();
		            this.spinner.setSelection( 1 );
			 }
			 else{
				 getLocationDet();
				 
			 }
		}
		else {
			url = searchUrl + searchId + cityID;
			new JSONParse().execute();
			url2 = searchForUrl + searchId + cityID + searchCount;
	        getForecast();
		}
	}


	private void getLocationDet() {
		bestProvider = locationManager.getBestProvider( new Criteria(), true ); 
		if ( bestProvider != null ) {

            locationDetails = locationManager.getLastKnownLocation( bestProvider );

            if ( locationDetails != null ) {
            	url = searchUrl + searchCoor + locationDetails.getLatitude() + searchCoor2 + locationDetails.getLongitude();
    			new JSONParse().execute();
    			url2 = searchForUrl + searchCoor + locationDetails.getLatitude() + searchCoor2 + locationDetails.getLongitude() + searchCount;
    	        getForecast();
                
            } else {
            	spinner.setSelection( 1 );
                Toast toast = Toast.makeText( getApplicationContext(), "Failed to get current location. Defaulting to Panama City. Try again soon.",
                                Toast.LENGTH_SHORT );
                toast.setGravity( Gravity.CENTER_HORIZONTAL, 0, 0 );
                toast.show();
                url = searchUrl + searchId + cityID;
    			new JSONParse().execute();
    			url2 = searchForUrl + searchId + cityID + searchCount;
    	        getForecast();
            }
	} else{
		Toast toast = Toast.makeText( getApplicationContext(), "error: Please enable Location Services.", Toast.LENGTH_SHORT );
        toast.setGravity( Gravity.CENTER_HORIZONTAL, 0, 0 );
        toast.show();
        spinner.setSelection( 1 );
	}
		
	}


	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}

}
