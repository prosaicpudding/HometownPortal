/* SplashActivity.java
 * Project E - Eric Daniels
 */

package com.android.projecte.townportal;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/*
 * Splash Activity
 * Description: This splash page runs before getting into the MainActivity.
 */
public class SplashActivity extends Activity {

    final private long splashDelay = 5000; // How long to display the our splash for

    @Override
    protected void onCreate( Bundle savedInstanceState ) {

        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_splash );

        new Timer().schedule( new TimerTask() {

            @Override
            public void run() {

                finish();
                Intent mainIntent = new Intent( SplashActivity.this, MainActivity.class );
                startActivity( mainIntent );
            }
            
        }, this.splashDelay );
    }

}
