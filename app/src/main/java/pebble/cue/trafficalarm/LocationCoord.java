package pebble.cue.trafficalarm;

/**
 * Created by cue on 11/8/2015.
 */
public class LocationCoord {
    double Lat;
    double Longt;
    public LocationCoord(double la,double lo){
        this.Lat = la;
        this.Longt = lo;
    }
    public LocationCoord(String la,String lo){
        this.Lat =  Double.parseDouble(la);
        this.Longt = Double .parseDouble(lo);
    }
}
