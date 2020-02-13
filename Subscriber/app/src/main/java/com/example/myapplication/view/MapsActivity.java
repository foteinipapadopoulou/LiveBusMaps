package com.example.myapplication.view;
import com.example.myapplication.domain.*;
import com.example.myapplication.R;
import com.example.myapplication.domain.Value;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public Subscriber s;
    boolean isBackButtonPressed=false;//We use this variable to identify if backButton is pressed.
    ArrayList<LatLng> latLngs = new ArrayList<>();
    ArrayList<Marker> markers=new ArrayList<>();//Save markers
    List<Value> values=new ArrayList<>();//Save the values that received

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent i =getIntent();
        //Receive the subscriber object
        s=(Subscriber) i.getSerializableExtra("Subscriber");

        //Receive the Socket,Inputstream,Outputstream that have been initializing
        GlobalState state = ((GlobalState) getApplicationContext());
        s.setRequestSocket(state.getS());
        s.setIn(state.getIn());
        s.setOut(state.getOut());

        //Buttons
        final Button button = findViewById(R.id.button);//RouteType=1
        final Button button2 = findViewById(R.id.button2);//RouteType=2

        /**
         * Add the buttons that hide the markers
         */
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = -1;
                boolean state=true;
                //Searching into values and markers
                for (Value val : values) {
                    for (Marker m : markers) {
                        if (val.getBus().getRouteType().equals("1")) {
                            if (m.getTag().equals(val.getBus().getVehicleId())) {
                                if(i==-1){
                                    state=!m.isVisible();
                                }
                                m.setVisible(state);
                                i = markers.indexOf(m);
                            }
                        }
                    }
                }
                if (i != -1) {
                    if (markers.get(i).isVisible()) {
                        button.setText("Hide " + markers.get(i).getSnippet());
                    } else {
                        button.setText("Hidden " + markers.get(i).getSnippet());
                    }
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i=-1;
                boolean state=true;
                //Searching into values and markers
                for(Value val : values) {
                    for (Marker m : markers) {
                        if(val.getBus().getRouteType().equals("2")){
                            if (m.getTag().equals(val.getBus().getVehicleId())){
                                if(i==-1){
                                    state=!m.isVisible();
                                }
                                m.setVisible(state);
                                i = markers.indexOf(m);
                            }
                        }

                    }
                }
                if (i != -1) {
                    if (markers.get(i).isVisible()) { //Hides all the markers
                        button2.setText("Hide " + markers.get(i).getSnippet());
                    } else {
                        button2.setText("Hidden " + markers.get(i).getSnippet());
                    }
                }
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Zoom camera to athens
        LatLng athens = new LatLng(37.99, 23.73);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(athens));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setTrafficEnabled(true);

        MapsActivity.AsyncTaskRunner runner = new MapsActivity.AsyncTaskRunner(s);
        runner.execute();
    }


    private class AsyncTaskRunner extends AsyncTask<Value,Value,Value>{

        MarkerOptions markerOptions = new MarkerOptions();
        Subscriber sub ;

        public AsyncTaskRunner(Subscriber s){
            this.sub=s;
        }

        @Override
        protected void onProgressUpdate(Value... value) {
            if(value[0].getTime()==null){
                //If an error occured ,go back to MainActivity
                Toast.makeText(MapsActivity.this,value[0].getBus().getDescriptionEnglish(),Toast.LENGTH_SHORT).show();
                onBackPressed();
            }else {
                drawMarkers(value);
            }
        }

        private void drawMarkers(Value[] val){
            Value v=val[0];
            LatLng l=new LatLng(v.getLatitude(),v.getLongitude());
            latLngs.add(l);

            //If it is the first marker that is being drawn
            //move camera to this region.
            if(latLngs.size()==1) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(l));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
            }

            //Find if there is already marker with the same VehicleID
            boolean found=false;
            for(Marker marker:markers){
                if(marker.getTag().toString().equals(v.getBus().getVehicleId())){
                    marker.setPosition(l);
                    found=true;
                }
            }
            //If there is not already a marker,then make one.
            if(!found){
                markerOptions.position(l);
                markerOptions.title(v.getBus().getVehicleId());
                markerOptions.snippet(v.getBus().getDescriptionEnglish());
                Marker  m =mMap.addMarker(markerOptions);
                markers.add(m);
                m.setTag(v.getBus().getVehicleId());
            }
        }

        @Override
        protected Value doInBackground(Value... values) {
           receivingValues(s.getRequestSocket(),s.getIn(),s.getOut());
            return null;
        }

        public Value receivingValues(Socket requestSocket, ObjectInputStream in, ObjectOutputStream out) {
            try {
                out.flush();
                out.writeObject(s.getTopic());//Sending the topic that we want values.
                out.flush();
                Value v = new Value(new Bus(" ", "", "", ""), 0, 0, "0");
                do {
                    System.out.println("Waiting for values.. ");
                    //Read the value that broker sent.
                    v = (Value) in.readObject();
                    //Check if backButton is pressed
                    if (isBackButtonPressed) {
                        //We send to Broker an identifier integer
                        //1->If we don't want the same topic
                        out.writeInt(1);
                        out.flush();
                        return null;
                    } else {
                        //0->continue sending the same topic
                        out.writeInt(0);
                        out.flush();
                    }

                    //If time is null then an error occured with
                    //publisher of this busline
                    //or waiting time has elapsed.
                    if (v.getTime() == null) {

                        System.out.println(v.getBus().getDescriptionEnglish());
                        out.writeInt(1);
                        out.flush();
                        publishProgress(v);
                        try {
                            s.requestSocket.close();
                            s.in.close();
                            s.out.close();
                        }catch(IOException ex){

                        }
                        return v;
                    } else {
                        //Value is correct and we make the markers.
                        System.out.println(v.toString());
                        values.add(v);
                        publishProgress(v);
                    }

                } while (v != null || v.getTime() != null);

            }catch(Exception e){
                e.printStackTrace();
                Value v = new Value(new Bus(" ", " ", " ", " "," ","Cannot find buses for this line."), 0, 0, null);
                publishProgress(v);
                try {
                    s.requestSocket.close();
                    s.in.close();
                    s.out.close();
                }catch(IOException ex){

                }
            }
            return null;
        }
    }

    //If backButton is pressed we finish MapsActivity
    //and set isBackButtonPressed variable to true.
    @Override
    public void onBackPressed(){
        isBackButtonPressed=true;
        this.finish();
    }
}
