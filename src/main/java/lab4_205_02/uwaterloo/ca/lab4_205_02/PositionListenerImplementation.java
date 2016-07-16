package lab4_205_02.uwaterloo.ca.lab4_205_02;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

import ca.uwaterloo.sensortoy.MapView;
import ca.uwaterloo.sensortoy.PositionListener;

/**
 * Created by Anurag Joshi on 7/2/2016.
 */
public class PositionListenerImplementation extends Lab4_205_02 implements PositionListener {

    List<PointF> myListLoc=new ArrayList<PointF>();
    List<PointF> myListDest=new ArrayList<PointF>();
    @Override
    public void originChanged(MapView source, PointF loc) {
        source.setUserPoint(loc);
        myListLoc.add(loc);
        source.setUserPath(myListLoc);
        return;
    }

    @Override
    public void destinationChanged(MapView source, PointF dest) {
       // source.setUserPoint(dest);
        myListDest.add(dest);
        source.setUserPath(myListDest);
        return;

    }
}
