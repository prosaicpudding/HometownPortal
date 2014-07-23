package com.android.projecte.townportal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/*
 * WeatherForecast
 * Description: class that parses JSON object and includes inner class Adapter to update ListView
 * help from: http://www.youtube.com/watch?v=0TulTqQM0Cc
 */
public class WeatherForecast {
	private List<dayForecast> dayForecastList;
	private String url;
	private Context ctx;
	private WeatherInfo info2;
	
	//constructor
	WeatherForecast(String link, Context context){
		url = link;
		ctx = context;
		dayForecastList = new ArrayList<dayForecast>();
		
		new JSONParse().execute();
	}
	
	private class JSONParse extends AsyncTask<String, String, JSONObject> {
	        
	      @Override
	        protected JSONObject doInBackground(String... args) {
	    	 info2 = new WeatherInfo();
	  		JSONObject json = info2.getJSONFromUrl(url);
	        return json;
	      }
	       @Override
	         protected void onPostExecute(JSONObject json) {
	         	         try {
	     
	        	 JSONArray jArray = json.getJSONArray("list");
	        	 
	        	 for(int i=1; i<jArray.length(); i++){
	        		 JSONObject data = jArray.getJSONObject(i);
	        		 dayForecast result = new dayForecast();
	        		 
	        		 JSONArray JSONArray_weather = data.getJSONArray("weather");
	        		 JSONObject json_weather = JSONArray_weather.getJSONObject(0);
	        		 JSONObject json_temp = data.getJSONObject("temp");
	        		 
	        		 result.date = data.getLong("dt");
	        		 result.description = json_weather.getString("description");
	        		 result.icon = json_weather.getString("icon");
	        		 result.temp_min = json_temp.getDouble("min");
	        		 result.temp_max = json_temp.getDouble("max");
	        		 
	        		 dayForecastList.add(result);
	        	 }
	           
	        	 WeatherAdapter adapter = new WeatherAdapter();
	        	 WeatherActivity.forecastList.setAdapter(adapter);
	           
	        } catch (Exception e) {
	          e.printStackTrace();
	        }
	       }
	    }
	
	
	 class WeatherAdapter extends ArrayAdapter<dayForecast>{

		WeatherAdapter(){
			super(ctx,R.layout.row_weather, dayForecastList);
		}
		
		
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.row_weather, parent, false);
			}
			
			TextView dayText = (TextView) convertView.findViewById(R.id.dayName);
	        TextView dayDate = (TextView) convertView.findViewById(R.id.dayDate);
			ImageView icon = (ImageView) convertView.findViewById(R.id.dayIcon);
			TextView minTempText = (TextView) convertView.findViewById(R.id.dayTempMin);
	        TextView maxTempText = (TextView) convertView.findViewById(R.id.dayTempMax);
	        TextView dayDescr = (TextView) convertView.findViewById(R.id.dayDescr);
			
	                
	        dayText.setText(new SimpleDateFormat("EEEE", Locale.US).format(new Date(dayForecastList.get(position).date * 1000)) + ", ");
	        dayDate.setText( new SimpleDateFormat("MMM d", Locale.US).format(new Date(dayForecastList.get(position).date * 1000)));
	        minTempText.setText( Double.toString(Math.floor(info2.convertTemperature(dayForecastList.get(position).temp_min))) + "°F");
	        maxTempText.setText( Double.toString(Math.ceil(info2.convertTemperature(dayForecastList.get(position).temp_max))) + "°F");
	        dayDescr.setText( dayForecastList.get(position).description);
	        icon.setImageResource(info2.getWeatherResource(dayForecastList.get(position).icon));
	        
	        return convertView;
		}
	}

	
}

class dayForecast{

	public double temp_max;
	public double temp_min;
	public String icon;
	public String description;
	public long date;
	
}