/* PlaceType.java
 * Project E - Eric Daniels
 */

package com.android.projecte.townportal;

import java.io.Serializable;

/*
 * Place Type
 * Description: Used for creation of tabs and using that tab
 * 				to search Google Places API.
 */
public class PlaceType implements Serializable {

    private static final long serialVersionUID = 1L;
    public String googleName, displayName;

    public PlaceType( String googleName, String displayName ) {

        this.googleName = googleName;
        this.displayName = displayName;
    }
}
