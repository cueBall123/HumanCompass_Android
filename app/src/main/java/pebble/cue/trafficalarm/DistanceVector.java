package pebble.cue.trafficalarm;

/**
 * Created by cue on 11/8/2015.
 */
public class DistanceVector {
    public double Distance;
    public double Bearing;
    DistanceVector(){}
    DistanceVector(String dist,String Bearing){
        this.Distance = Double.parseDouble(dist);
        this.Bearing = Double.parseDouble(Bearing);
    }
}
