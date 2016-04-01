package com.cmti.app;

/*
 * This is a sample app to demo POMI's capability to launch third party
 * apps from "app drawer".
 *  
 * The app allows users to view weather conditions at an airport based on 
 * the ICAO code. The app persists the last airport a user viewed prior 
 * to closing the app and display information for that airport by default 
 * on the next launch.
  */

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.SyncStateContract.Helpers;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AirportWeather extends Activity implements View.OnClickListener
{
    private static final String TAG = "AirportWeather";
    private static final String GEONAMES_WEATHER_URL = 
            "http://ws.geonames.org/weatherIcaoJSON?ICAO=";
    private static final String GEONAMES_WEATHER_APP = "&username=gtech";
    private SharedPreferences myPref;
    private String key;
    private String defaultAirportCode;
    
    private Button mButton;
    private TextView mTextTemp;
    private TextView mTextClouds;
    private TextView mTextAirport;
    private TextView mTextHumidity;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mTextAirport = (TextView) findViewById(R.id.AirportName);
        mTextTemp = (TextView) findViewById(R.id.Temperature);
        mTextClouds = (TextView) findViewById(R.id.Clouds);
        mTextHumidity = (TextView) findViewById(R.id.Humidity);
        
        // Create button and set up listener
        mButton = (Button)findViewById(R.id.GetWeather);
        mButton.setOnClickListener(this);
        
        // retrieve last airport code
        myPref = getPreferences(MODE_PRIVATE);
        key = getString(R.string.key_airportcode);
        defaultAirportCode = getString(R.string.default_airportcode);
        String apCode = myPref.getString(key,defaultAirportCode);
        if ( Character.toUpperCase(apCode.charAt(0)) != 'K') {
            apCode = "K" + apCode;
        }        
        
        showWeather(apCode);
    }
    
    public void onClick(View view) 
    {       
        EditText airportCode = (EditText)findViewById(R.id.AirportCode);
        String apCode = airportCode.getText().toString().trim();
        if ( Character.toUpperCase(apCode.charAt(0)) != 'K') {
            apCode = "K" + apCode;
        }
            
        if (TextUtils.isEmpty(apCode)) {
            displayMessage(getString(R.string.invalid_airport_code));
        } else {
            showWeather(apCode);
        }
    }
    
    private void displayMessage(String msg) {
        Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        toast.show();
    }
    
    /**
     * Display weather info based on the airport code
     * @param airportCode the airport code
     */
    public void showWeather(String airportCode)
    {
        try {
            URL geonamesURL = new URL(GEONAMES_WEATHER_URL
                    + airportCode + GEONAMES_WEATHER_APP);
             
            JsonParser parser = new JsonParser();
            String str = IOUtils.toString(geonamesURL);
            JsonObject obj = (JsonObject) parser.parse(str).getAsJsonObject()
                    .get(getString(R.string.json_weather_observation));
            if (obj == null) { 
                displayMessage(getString(R.string.invalid_airport_code));
                return; 
            }
            String cd = obj.get(getString(R.string.json_clouds)).toString();
            String temp = obj.get(getString(R.string.json_temperature)).toString();
            String hu = obj.get(getString(R.string.json_humidity)).toString();
            String apt = obj.get(getString(R.string.json_airport_name)).toString();
            
            mTextAirport.setText(apt);
            mTextTemp.setText(temp + " c");
            mTextClouds.setText(cd);
            mTextHumidity.setText(hu +"%");
            myPref.edit().putString(key, airportCode);
        } catch (MalformedURLException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }
    
  
}