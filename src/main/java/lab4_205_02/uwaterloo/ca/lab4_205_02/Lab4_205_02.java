/*
Distance Between Two Points

To find out how far apart two fingers are, we first construct a vector (x, y) which is the difference between the two points. Then we use the formula for Euclidean distance to calculate the spacing:

private float spacing(MotionEvent event) {
float x = event.getX(0) - event.getX(1);
float y = event.getY(0) - event.getY(1);
return FloatMath.sqrt(x * x + y * y);
}
The order of the points doesn’t matter because any negative signs will be lost when we square them. Note that all math is done using Java’s float type. While some Android devices may not have floating point hardware, we’re not doing this often enough to worry about its performance.
Midpoint of Two Points

Calculating a point in the middle of two points is even easier:

private void midPoint(PointF point, MotionEvent event) {
float x = event.getX(0) + event.getX(1);
float y = event.getY(0) + event.getY(1);
point.set(x / 2, y / 2);
}
 */

package lab4_205_02.uwaterloo.ca.lab4_205_02;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.location.Location;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ca.uwaterloo.sensortoy.InterceptPoint;
import ca.uwaterloo.sensortoy.LabeledPoint;
import ca.uwaterloo.sensortoy.LineSegment;
import ca.uwaterloo.sensortoy.MapLoader;
import ca.uwaterloo.sensortoy.MapView;
import ca.uwaterloo.sensortoy.NavigationalMap;
import ca.uwaterloo.sensortoy.PositionListener;
import ca.uwaterloo.sensortoy.VectorUtils;

public class Lab4_205_02 extends AppCompatActivity implements SensorEventListener,PositionListener{
    double m_Azimuth;
    TextView Direction,GPSInstructor,Distance,OriginX,OriginY,DestinationX,DestinationY;
    SensorManager sensorManager;
    Button Reset,GetRoute;
    Sensor Orientation,Accelerometer;
    MapView mv;
    PositionListenerImplementation Pos;
    NavigationalMap Navigate=new NavigationalMap();
    VectorUtils Vectors;
    ImageView Pointer,GoThisWay;
    LineSegment LineSeg;
    boolean clicked=false;
    float currentdegree=0f;
    float meterin1step=0.762f; //source is http://www.convertunits.com/from/step/to/meter
    PointF Origin=new PointF(0,0);
    PointF Destination=new PointF(0,0);
    PointF UserCurrentPoint=new PointF(0,0);
    PointF nextpoint=new PointF(0,0);
    List<PointF> link= new ArrayList<PointF>();
    List<PointF> nextpoints= new ArrayList<PointF>();
    List<InterceptPoint> walls=new ArrayList<InterceptPoint>();
    PointF tempOrigin;
   int stepcounting=30;
    int actualstepcount=0;
    int truesteps=0;
    int tempsteps=0;
    int mypathsteps=0;
   // Canvas canvas=new Canvas();
   // Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    /*
    ScaleGestureDetector SGD;
    GestureDetector gestureDetector;
    public boolean onTouchEvent(MotionEvent ev) {
        SGD.onTouchEvent(ev);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = detector.getScaleFactor();
            return true;
        }
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:

                break;
        }
        return SGD.onTouchEvent(ev);
    }*/
    public void onSensorChanged(SensorEvent event) {
        //step counting algorithm.
     /* if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            if (event.values[0] > 4 || event.values[1] > 3 || event.values[2] > 3) {
                return;
            }
            double total = Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]);
            //double tofind = Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1]);
            //can also use tofind>1.8, stepcounting/40.
            if (total > 1) {
                int steps = stepcounting / 60;
                stepcounting++;
                Distance.setText(steps+"steps");
                nextpoint = new PointF(Origin.x + (float) Math.cos(m_Azimuth) * meterin1step, Origin.y + (float) Math.sin(m_Azimuth) * meterin1step);
                mv.setUserPoint(nextpoint);
                nextpoints.add(nextpoint);
                mv.setUserPoint(nextpoint);
                mv.setUserPath(nextpoints);

            }
        }*/
        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                tempOrigin=new PointF(Origin.x,Origin.y);
                double total = Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]);
                if (total > 1) {
                    actualstepcount++;
                    truesteps = actualstepcount / 60;
                    if (truesteps != tempsteps) {
                        Distance.setText(truesteps + "steps");
                        //UserCurrentPoint.set(Origin.x + (float) Math.cos(Math.toRadians(m_Azimuth)),Origin.y + (float) Math.sin(Math.toRadians(m_Azimuth)));
                        UserCurrentPoint = new PointF(Origin.x + (float) Math.cos(Math.toRadians(m_Azimuth)) * 0.8f, Origin.y + (float) Math.sin(Math.toRadians(m_Azimuth)) *0.8f);
                        //Origin=UserCurrentPoint;
                        tempsteps = truesteps;
                        nextpoints.add(UserCurrentPoint);
                        Origin.set(UserCurrentPoint.x, UserCurrentPoint.y);
                        PointF AJnextpoint= new PointF(Origin.x+0.1f,Origin.y+0.1f);
                      if(Navigate.calculateIntersections(Origin,AJnextpoint).size()>0)
                        {
                            Origin=mv.getOriginPoint();
                            //GPSInstructor.setText("Illegal");
                           // System.exit(-1);
                            Origin.set(0,0);
                        }
                        mv.setUserPoint(Origin);
                        // nextpoints.add(Destination);
                        //mv.setUserPoint(UserCurrentPoint);
                        //mv.setUserPath(nextpoints);
                        RouteDescribed();
                    }
                    /*if (Origin.y<2 || Origin.y>11.45  || Origin.x > 15 || Origin.x<2) {
                            if(Origin.x<2)
                                Origin.x=Origin.x+1;
                            else
                            Origin.x=Origin.x-1;
                            if(Origin.y<2)
                            Origin.y = Origin.y+1;
                            else
                            Origin.y=Origin.y-1;
                            GPSInstructor.setText("Invalid Point on Map");
                        }*/
                }
            }

            if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                // m_Azimuth = (event.values[0] + 360) % 360;
                m_Azimuth = event.values[0];
                //Azimuth (degrees of rotation around the z axis). This is the angle between magnetic north and the device's y axis.
                if (m_Azimuth >= 0 && m_Azimuth < 90) {
                    Direction.setText("North");
                }
                if (m_Azimuth >= 90 && m_Azimuth < 180) {
                    Direction.setText("East");
                }
                if (m_Azimuth >= 180 && m_Azimuth < 270) {
                    Direction.setText("South");
                }
                if (m_Azimuth >= 270 && m_Azimuth <= 360) {
                    Direction.setText("West");
                }
                RotateAnimation ra = new RotateAnimation(currentdegree, -event.values[0],
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f);
                // RotateAnimation ra = new RotateAnimation((float)m_Azimuth,currentdegree);
                ra.setDuration(250);

                ra.setFillAfter(true);//persist even after finishing
                Pointer.startAnimation(ra);
                //GoThisWay.startAnimation(ra);
                currentdegree = -event.values[0] - 180;
                mv.addListener(Pos);
                //first get x and y of starting point and destination. draw a line segment between them. if it has a wall(check from learn)
                //then figure out another way to go, else use the normal way.
                Origin = mv.getOriginPoint();
                Destination = mv.getDestinationPoint();
                LineSeg = new LineSegment(Origin, Destination);//construct a new line segment
                OriginX.setText(Double.toString(Origin.x));
                OriginY.setText(Double.toString(Origin.y));
                DestinationX.setText(Double.toString(Destination.x));
                DestinationY.setText(Double.toString(Destination.y));
                PointF nextpoint = new PointF(Origin.x + (float) Math.cos(Math.toRadians(m_Azimuth)), Origin.y + (float) Math.sin(Math.toRadians(m_Azimuth)));
                //last lab we calculated cos component multiplied by number of steps. here we convert each step to its equivalent distance.
                // link.add(Origin);
                //link.add(Destination);
                //  mv.setUserPath(link);
                walls = Navigate.calculateIntersections(Origin, Destination);
                if (walls.size() == 0) {
                    //if wall is null path is line.
                    mv.setUserPoint(Origin);
                    //Double distance= FindPathtoDestination(nextpoint,Destination);
                    //FindPathtoDestination();
                } else {
                    mv.setUserPoint(nextpoint);
                    //FindPathtoDestination();
                    //wall is there. take other route
                }
                mv.setUserPoint(Origin);
                // Origin=nextpoint;
                link.clear();
                walls.clear();
            }
        }
    }
    //whats the difference between linesegment.length and this function?Why does it return different values? ASK THE TA.
    void RouteDescribed() {
        /*
        float DeltaXSquared= (Destination.x-Origin.x)*(Destination.x-Origin.x);//(x2-x1)^2
        float DeltaYSquared=(Destination.y-Origin.y)*(Destination.y-Origin.y);//(y2-y1)^2
        double Distance=Math.sqrt((DeltaXSquared+DeltaYSquared));
        return Distance;*/
            PointF current_loc = mv.getUserPoint();
            List<PointF> Link = new ArrayList<>();
            PointF nextpoint, previouspoint;
            float deltaY = current_loc.y - Destination.y;//to avoid walking through walls

            previouspoint = new PointF(current_loc.x, current_loc.y);
            nextpoint = new PointF(Destination.x, Destination.y + deltaY);
            //this is the length of 1 wall slab. so if y is less than 8.34 that means you have a wall to your left or right..so walk vertically
            //also walk horizontally if your x values are within the horizontal limits of the room.
          /*if((Destination.x>22 || current_loc.x>22) && (Destination.y>3 || current_loc.y>3) && (Destination.y<20 || current_loc.y<20)) {
            while (!(Navigate.calculateIntersections(previouspoint, nextpoint).size() == 0)) {
                previouspoint.set(previouspoint.x-0.1f, previouspoint.y);//to avoid hanging and discrepancy.
                nextpoint.set(nextpoint.x-0.1f, nextpoint.y);
            }
        }*/
            if (current_loc.y < 22 || (current_loc.x < 24 && Destination.x < 24)) {
                if((current_loc.x>0 && current_loc.x<5 && current_loc.y>19 && current_loc.y<22)||(current_loc.x>18 && current_loc.x<24 && current_loc.y>19 && current_loc.y<22)){
                    while (!(Navigate.calculateIntersections(previouspoint, nextpoint).size() == 0)) {
                        previouspoint.set(previouspoint.x, previouspoint.y - 0.1f);//to avoid hanging and discrepancy.
                        nextpoint.set(nextpoint.x, nextpoint.y - 0.1f);
                       // myPath();
                    }
                }
                if(current_loc.y>3 && current_loc.y<20 && current_loc.x>23)
                {
                    while (!(Navigate.calculateIntersections(previouspoint, nextpoint).size() == 0)) {
                        previouspoint.set(previouspoint.x - 0.1f, previouspoint.y+1f);//to avoid hanging and discrepancy.
                        nextpoint.set(nextpoint.x - 0.1f, nextpoint.y+1f);
                    }
                }
                else {
                    while (!(Navigate.calculateIntersections(previouspoint, nextpoint).size() == 0)) {
                        previouspoint.set(previouspoint.x, previouspoint.y + 0.1f);//to avoid hanging and discrepancy.
                        nextpoint.set(nextpoint.x, nextpoint.y + 0.1f);
                    }
                }
            }
            //length of 1 wall slab is 8.34m. Vertical limit is 10.4m approx long. Thus we are setting limits.Here we check if point is between wall edge end and last point on map vertically, if so take horizontal route!
            //take care of edge conditions should not walk through corners.
            else if (Destination.y < 17.5 || Destination.y > 19 || current_loc.y > 19) {
                while (!(Navigate.calculateIntersections(previouspoint, nextpoint).size() == 0)) {
                    previouspoint.set(previouspoint.x, previouspoint.y - 0.1f);
                    nextpoint.set(nextpoint.x, nextpoint.y - 0.1f);
                }
            }
           /*else if(Origin.x>22 || Destination.x>22)
            {
                while (!(Navigate.calculateIntersections(previouspoint, nextpoint).size() == 0)) {
                previouspoint.set(previouspoint.x-0.1f, previouspoint.y-0.1f);
                nextpoint.set(nextpoint.x-0.1f, nextpoint.y);
            }

            }*/
//preserve this order.
            Link.add(current_loc);
            Link.add(previouspoint);//to avoid cutting through walls
            Link.add(nextpoint);//to avoid cutting through walls
            Link.add(Destination);
            mv.setUserPath(Link);
            myPath();
            Link.clear();
            //PathPredictor();
        }
    /*
void myPath()
{
    //delta x y
    float deltax=Math.abs(Destination.x-Origin.x);
    float deltay=Math.abs(Destination.y-Origin.y);
    //deal with vertical.
    float verticaldistcalc=0f;
    float horizontaldiscalc=0f;
    if(8.34>mv.getUserPoint().y){
        verticaldistcalc=(float)8.34-mv.getUserPoint().y;
    }
    if(17>mv.getUserPoint().x)
    {
        horizontaldiscalc=(float)17-mv.getUserPoint().x;
    }
    //convert distance into steps and check if those many steps have been taken or not.
    if(Origin.y>Destination.y && (verticaldistcalc>horizontaldiscalc)){
GPSInstructor.setText("Travel North by "+" "+verticaldistcalc+"m");
        RotateAnimation gothisway=new RotateAnimation(0,0,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        gothisway.setDuration(250);
        gothisway.setFillAfter(true);//persist even after finishing
        GoThisWay.startAnimation(gothisway);
    }
    if(Origin.y<Destination.y && (verticaldistcalc>horizontaldiscalc)){
        GPSInstructor.setText("Travel South"+" "+verticaldistcalc+"m");
        RotateAnimation gothisway=new RotateAnimation(180,0,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        gothisway.setDuration(250);
        gothisway.setFillAfter(true);//persist even after finishing
        GoThisWay.startAnimation(gothisway);
    }
    if(Origin.x>Destination.x && (horizontaldiscalc>verticaldistcalc)){
        GPSInstructor.setText("Travel West"+horizontaldiscalc+"m");
        RotateAnimation gothisway=new RotateAnimation(270,0,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        gothisway.setDuration(250);
        gothisway.setFillAfter(true);//persist even after finishing
        GoThisWay.startAnimation(gothisway);
    }
    if(Origin.x<Destination.x && (horizontaldiscalc>verticaldistcalc)){
        GPSInstructor.setText("Travel East"+horizontaldiscalc+"m");
        RotateAnimation gothisway=new RotateAnimation(90,0,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        gothisway.setDuration(250);
        gothisway.setFillAfter(true);//persist even after finishing
        GoThisWay.startAnimation(gothisway);
    }
    if(deltax<1.0 &&  deltay<1.0){
        GPSInstructor.setText("Destination Reached");
    }
}*/

    void myPath(){
        //1.4 is width between walls.
        PointF nextpoint=new PointF(0,0);
        nextpoint=Origin;
        if(mv.getUserPoint().y < 17 && Math.abs(Destination.x-Origin.x) > 1.4) {
            float verticaldistcalc = (float) (8.37 - mv.getUserPoint().y);
            stepcounting=(int)(verticaldistcalc/meterin1step);
            GPSInstructor.setText("Travel South"+Math.abs(stepcounting)+"steps");
            RotateAnimation gothisway=new RotateAnimation(180,0,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);
            gothisway.setDuration(250);
            gothisway.setFillAfter(true);//persist even after finishing
            GoThisWay.startAnimation(gothisway);
  /*
            nextpoint = new PointF(Origin.x, Origin.y + (float) Math.sin(Math.toRadians(m_Azimuth)) * meterin1step);
            nextpoints.add(nextpoint);
            mv.setUserPoint(nextpoint);
            nextpoints.add(Destination);
            mv.setUserPath(nextpoints);
            if(truesteps>=stepcounting-mypathsteps)
            {
                Origin.set(mv.getUserPoint().x,8.37f);
                mv.setUserPoint(Origin);
                mypathsteps=stepcounting;
                stepcounting=0;
                GPSInstructor.setText("Travel South"+Math.abs(stepcounting)+"steps");
                nextpoints.clear();
            }
*/
        } else {
            if(mv.getDestinationPoint().y > 17) {
                //can travel horizontally.
                float horizontaldistcalc = Destination.x-Origin.x;
                if(Destination.x-Origin.x > 0) {
                    stepcounting=(int)(horizontaldistcalc/meterin1step);
                    GPSInstructor.setText("Travel East" +Math.abs(stepcounting)+"steps");
                    RotateAnimation gothisway=new RotateAnimation(90,0,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f);
                    gothisway.setDuration(250);
                    gothisway.setFillAfter(true);//persist even after finishing
                    GoThisWay.startAnimation(gothisway);
              //      nextpoint = new PointF(Origin.x+(float) Math.cos(m_Azimuth) * meterin1step, Origin.y);
                //    mv.setUserPoint(nextpoint);
                } else if (Destination.x-Origin.x < 0) {
                    stepcounting=(int)(horizontaldistcalc/meterin1step);
                    GPSInstructor.setText("Travel West"+Math.abs(stepcounting)+"steps");
                    RotateAnimation gothisway=new RotateAnimation(270,0,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f);
                    gothisway.setDuration(250);
                    gothisway.setFillAfter(true);//persist even after finishing
                    GoThisWay.startAnimation(gothisway);
                  //  nextpoint = new PointF(Origin.x+(float) Math.cos(m_Azimuth) * meterin1step, Origin.y);
                    //mv.setUserPoint(nextpoint);
                    /*nextpoint = new PointF(Origin.x+ (float) Math.sin(Math.toRadians(m_Azimuth)) * meterin1step, Origin.y);
                    nextpoints.add(nextpoint);
                    mv.setUserPoint(nextpoint);
                    nextpoints.add(Destination);
                    mv.setUserPath(nextpoints);
                    if(truesteps>=stepcounting-mypathsteps)
                    {
                        Origin.set(mv.getUserPoint().x,8.37f);
                        mv.setUserPoint(Origin);
                        stepcounting=0;
                        GPSInstructor.setText("Travel West"+Math.abs(stepcounting)+"steps");
                        nextpoints.clear();
                    }*/
                }
            }
           else if(mv.getDestinationPoint().y < 17) {
                if(Math.abs(Destination.x-Origin.x) > 1.4) {
                    float horizontaldistcalc = Destination.x-Origin.x;
                    if(Destination.x-Origin.x > 0) {
                        stepcounting=(int)(horizontaldistcalc/meterin1step);
                        GPSInstructor.setText("Travel East"+Math.abs(stepcounting)+"steps");
                        RotateAnimation gothisway=new RotateAnimation(90,0,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF,
                                0.5f);
                        gothisway.setDuration(250);
                        gothisway.setFillAfter(true);//persist even after finishing
                        GoThisWay.startAnimation(gothisway);
                      //  nextpoint = new PointF(Origin.x+(float) Math.cos(m_Azimuth) * meterin1step, Origin.y);
                        //mv.setUserPoint(nextpoint);
                    } else if (Destination.x-Origin.x < 0) {
                        stepcounting=(int)(horizontaldistcalc/meterin1step);
                        GPSInstructor.setText("Travel West"+Math.abs(stepcounting)+"steps");
                        RotateAnimation gothisway=new RotateAnimation(270,0,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF,
                                0.5f);
                        gothisway.setDuration(250);
                        gothisway.setFillAfter(true);//persist even after finishing
                        GoThisWay.startAnimation(gothisway);
                       // nextpoint = new PointF(Origin.x+(float) Math.cos(m_Azimuth) * meterin1step, Origin.y);
                        //mv.setUserPoint(nextpoint);
                        /*nextpoint = new PointF(Origin.x+ (float) Math.sin(Math.toRadians(m_Azimuth)) * meterin1step, Origin.y);
                        nextpoints.add(nextpoint);
                        mv.setUserPoint(nextpoint);
                        nextpoints.add(Destination);
                        mv.setUserPath(nextpoints);
                        if(truesteps>=stepcounting-mypathsteps)
                        {
                            Origin.set(mv.getUserPoint().x,8.37f);
                            mv.setUserPoint(Origin);
                            stepcounting=0;
                            GPSInstructor.setText("Travel West"+Math.abs(stepcounting)+"steps");
                            nextpoints.clear();
                        }*/
                    }
                } else {
                    float verticaldistcalc = mv.getUserPoint().y - Destination.y;
                    stepcounting=(int)(verticaldistcalc/meterin1step);
                    GPSInstructor.setText("Travel North"+Math.abs(stepcounting)+"steps");
                    RotateAnimation gothisway=new RotateAnimation(0,0,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f);
                    gothisway.setDuration(250);
                    gothisway.setFillAfter(true);//persist even after finishing
                    GoThisWay.startAnimation(gothisway);
                    //nextpoint = new PointF(Origin.x, Origin.y + (float) Math.sin(m_Azimuth) * meterin1step);
                    //mv.setUserPoint(nextpoint);
                }
            }
            else {
                if(Destination.x-Origin.x > 0 && Destination.y-Origin.y > 0) {
                    GPSInstructor.setText("Travel"+ (Destination.x-Origin.x) + "East and"+(Destination.y-Origin.y)+"North.");
                } else if(Destination.x-Origin.x < 0 && Destination.y-Origin.y > 0) {
                    GPSInstructor.setText("Travel"+Math.abs(Destination.x-Origin.x)+"West and "+(Destination.y-Origin.y)+ "North.");
                } else if(Destination.x-Origin.x > 0 && Destination.y-Origin.y < 0) {
                    GPSInstructor.setText("Travel"+(Destination.x-Origin.x)+"East and "+Math.abs(Destination.y-Origin.y)+"South.");
                } else if(Destination.x-Origin.x < 0 && Destination.y-Origin.y < 0) {
                    GPSInstructor.setText("Travel"+ Math.abs(Destination.x-Origin.x)+"West and"+Math.abs(Destination.y-Origin.y)+"South.");
                }
            }
        }

        if(Math.abs(Destination.x-Origin.x) < 1.5 && Math.abs(Destination.y-Origin.y) < 1.5)
            GPSInstructor.setText("Destination Reached");
     }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Auto Generated
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab4_205_02);
            initializeViews();
            registerForContextMenu(mv);
            Pos=new PositionListenerImplementation();
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if(sensorManager!=null) {
                Orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
                sensorManager.registerListener(this, Orientation, SensorManager.SENSOR_DELAY_GAME);
                Accelerometer=sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
                sensorManager.registerListener(this,Accelerometer,SensorManager.SENSOR_DELAY_FASTEST);
            }
            else {
                return;
            }
        Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //click detected!
                clicked = true;
                GPSInstructor.setText(" ");
                stepcounting=0;
                Origin.x=0;Origin.y=0;
                Destination.x=0;Destination.y=-0;
                truesteps=0;
                actualstepcount=0;
                Distance.setText(truesteps+"steps");
                mypathsteps=0;
            }
        });
        GetRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //click detected!
                //clicked = true;
           RouteDescribed();
            }
        });
        }
    public void initializeViews() {
        try {
            LinearLayout layout = ((LinearLayout) findViewById(R.id.layout));
            Direction = (TextView) findViewById(R.id.Direction);
            Pointer = (ImageView) findViewById(R.id.Compass);
            GoThisWay = (ImageView) findViewById(R.id.GoThisWay);
            GPSInstructor=(TextView)findViewById(R.id.GPSInstructor);
            Distance=(TextView)findViewById(R.id.Distance);
            OriginX=(TextView)findViewById(R.id.OriginX);
            OriginY=(TextView)findViewById(R.id.OriginY);
            DestinationX=(TextView)findViewById(R.id.DestinationX);
            DestinationY=(TextView)findViewById(R.id.DestinationY);
            Reset = (Button) findViewById(R.id.ResetButton);
            GetRoute=(Button)findViewById(R.id.GetRouteButton);
            mv = new MapView(getApplicationContext(),1600, 1600, 50, 50);
            String output = "<Phone>/Android/data/lab3_205_02.uwaterloo.ca.lab3_205_02/files";
            //Navigate = MapLoader.loadMap(getExternalFilesDir(null), "Lab-room-peninsula.svg");
            Navigate = MapLoader.loadMap(getExternalFilesDir(null), "E2-3344.svg");
            mv.setMap(Navigate);
            layout.addView(mv);
            mv.setVisibility(View.VISIBLE);
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }
    protected void onResume() {
        super.onResume();
         sensorManager.registerListener(this,Orientation,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,Accelerometer,SensorManager.SENSOR_DELAY_FASTEST);
    }
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    @Override
    public  void onCreateContextMenu(ContextMenu menu , View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu , v, menuInfo);
        mv.onCreateContextMenu(menu , v, menuInfo);
    }
    @Override
    public  boolean  onContextItemSelected(MenuItem item)
    {
        return  super.onContextItemSelected(item) ||  mv.onContextItemSelected(item);
    }
    @Override
    public void originChanged(MapView source, PointF loc) {
        source.setOriginPoint(loc);
        source.setUserPoint(loc);
        RouteDescribed();
    }

    @Override
    public void destinationChanged(MapView source, PointF dest) {
        source.setDestinationPoint(dest);
        RouteDescribed();
    }
}