package com.android.projecte.townportal;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * WeatherInfo
 * Description: class used to get JSON object with weather information, and functions to convert information
 */

public class WeatherInfo {

		  private static InputStream content = null;
		  private static JSONObject jObj = null;
		  private static String json = "";
		 
		  //Empty constructor
		  WeatherInfo() {
		  }
		  
		  /*
		   * getJSONfromUrl
		   * Description: Get JSON object from openweathermap
		   */
		  public JSONObject getJSONFromUrl(String url) {
		   
		    try {
		      
		      DefaultHttpClient httpClient = new DefaultHttpClient();
		      HttpPost httpPost = new HttpPost(url);
		      HttpResponse httpResponse = httpClient.execute(httpPost);
		      HttpEntity httpEntity = httpResponse.getEntity();
		      content = httpEntity.getContent();
		    } catch (UnsupportedEncodingException e) {
		      e.printStackTrace();
		    } catch (ClientProtocolException e) {
		      e.printStackTrace();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		    try {
		      BufferedReader reader = new BufferedReader(new InputStreamReader(
		          content, "iso-8859-1"), 8);
		      StringBuilder sb = new StringBuilder();
		      String line = null;
		      while ((line = reader.readLine()) != null) {
		        sb.append(line);
		      }
		      content.close();
		      json = sb.toString();
		    } catch (Exception e) {
		      e.printStackTrace();
		    }
		    
		    try {
		      jObj = new JSONObject(json);
		    } catch (JSONException e) {
		    	e.printStackTrace();
		    }
		    
		    return jObj;
		  }
		  
		/*
		 * convert temperature
		 * Description: Convert temperature from Kelvin to Farenheit
		 */
		public Double convertTemperature(double double1) {
			Double result;
			result = 9 * (double1 - 273.15) / 5 + 32;
			return result;
		}

		/*
		 * convert Date
		 * Description: Convert date from unix format to HH:mm
		 */
		public String convertDate(long unixTime) {
	        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
	        Calendar cal = Calendar.getInstance();
	        cal.setTimeInMillis(unixTime * 1000);
	        sdf.setTimeZone(cal.getTimeZone());
	        return sdf.format(cal.getTime());
	    }
	   
		/*
		 * convert Speed
		 * Description: Convert speed from mps to mph
		 */
		public String convertSpeed(Double mps){
		Double res;
		String mph;
			res = mps * 60 * 60 * 100 / 2.54 / 12 / 5280;
			mph = Double.toString(Math.round(res));
			return mph;
		}
	/*
	 * Convert openweather icon to drawable
	 * Icons from: //https://www.iconfinder.com/search/?q=weather+icons&license=2&maximum=256&price=free
	 */
	public int getWeatherResource(String id) {
	       	if (id.equals("01d"))
				return R.drawable.w01d;
			else if (id.equals("01n")) 
				return R.drawable.w01n;
			else if (id.equals("02d") || id.equals("02n"))
				return R.drawable.w02d;
			else if (id.equals("03d") || id.equals("03n"))		
				return R.drawable.cloud2;
	        else if (id.equals("04d") || id.equals("04n"))
	            return R.drawable.w04d;
			else if (id.equals("09d") || id.equals("09n"))		
				return R.drawable.w500d;
			else if (id.equals("10d") || id.equals("10n"))		
				return R.drawable.w501d;
			else if (id.equals("11d") || id.equals("11n"))		
				return R.drawable.w11d;
			else if (id.equals("13d") || id.equals("13n"))		
				return R.drawable.w13d;
			else if (id.equals("50d") || id.equals("50n"))		
				return R.drawable.w50d;

			return R.drawable.w01d;

		} 
	
}
