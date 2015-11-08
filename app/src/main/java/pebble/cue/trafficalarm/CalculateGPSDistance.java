package pebble.cue.trafficalarm;

/**
 * Created by cue on 11/8/2015.
 */
public class CalculateGPSDistance {
    final static int Earth_Radius = 6371000;// meters


    public static DistanceVector Distance(LocationCoord firstLoc, LocationCoord secondLoc){ // ‘haversine’ formula
        DistanceVector dv = new DistanceVector();
        double dLat = Math.toRadians(secondLoc.Lat-firstLoc.Lat);
        double dLng = Math.toRadians(secondLoc.Longt-firstLoc.Longt);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(firstLoc.Lat)) * Math.cos(Math.toRadians(secondLoc.Lat)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        dv.Distance = (float) (Earth_Radius * c);
        double longDiff= secondLoc.Longt-firstLoc.Longt;
        double y = Math.sin(longDiff)*Math.cos(secondLoc.Lat);
        double x = Math.cos(firstLoc.Lat)*Math.sin(secondLoc.Lat)-Math.sin(firstLoc.Lat)*Math.cos(secondLoc.Lat)*Math.cos(longDiff);

        dv.Bearing = (Math.toDegrees(Math.atan2(y, x))+360)%360;
        return dv;
    }
}
