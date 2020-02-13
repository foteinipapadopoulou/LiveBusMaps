package com.example.myapplication.view;
import com.example.myapplication.domain.*;
import com.example.myapplication.R;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private TextView finalResult;
    private Button button;
    private EditText lineId;
    private ListView myList;
    private ArrayAdapter arrayAdapter;
    Subscriber s;
    Broker lastbroker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button_enter);
        finalResult=(TextView) findViewById(R.id.textView);
        lineId=(EditText) findViewById(R.id.lineId);
        myList=(ListView) findViewById(R.id.theList);
        //List with busLines
        final ArrayList<String> buslines=new ArrayList<>();
        //Reading the text file with busLines
        try {
            readTXT(buslines);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Set buslines arraylist on ListView
        arrayAdapter=new ArrayAdapter(this,R.layout.list_item_layout,buslines);
        myList.setAdapter(arrayAdapter);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
             @Override
              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 lineId.setText(buslines.get(position));
              }
         });

        //When typing show suggestions from ListView
        lineId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                (MainActivity.this).arrayAdapter.getFilter().filter(s);
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        //Initializing button " Enter "'.
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            String busline = lineId.getText().toString();
            if(!buslines.contains(busline)){
                Log.e("WRONG LINEID",busline);
                Toast.makeText(MainActivity.this,"Cannot find this busline.",Toast.LENGTH_SHORT).show();
            }else if(s.getBrokers()==null) {
                //First Connection was unsuccessful.
                Toast.makeText(MainActivity.this,"Unsuccessful connection. Restart App!",Toast.LENGTH_SHORT).show();
            }
            else {
                s.setTopic(new Topic(busline));
                //Find who is responsible for this busline by searching brokersList.
                Broker b=s.findBroker(s.getTopic());
                if(b.equals(lastbroker)){
                    if(s.requestSocket.isClosed()){
                        //Subscriber was connected with lastbroker
                        //but an exception occured
                        //so we should connect again.
                        AsyncTaskRunner runner = new AsyncTaskRunner(v);
                        runner.execute(b.getIp(),String.valueOf(b.getPort()),"0");
                    }else {
                        //Subscriber is already connected with lastbroker
                        AsyncTaskRunner runner = new AsyncTaskRunner(v);
                        runner.execute(b.getIp(), String.valueOf(b.getPort()), "1");// with 1->not the first connection.
                    }

                }else {
                    //Subscriber is not connected with broker
                    //that is responsible for this busline
                    try {
                        if(!s.requestSocket.isClosed()) {
                            s.requestSocket.close();
                            s.in.close();
                            s.out.close();
                        }
                        lastbroker=b;
                        //New Connection with the " right" broker
                        AsyncTaskRunner runner = new AsyncTaskRunner(v);
                        runner.execute(b.getIp(),String.valueOf(b.getPort()),"0");
                    }catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            }

        });

        //Initializing Subscriber with an ID
        s=new Subscriber(1);
        s.setTopic(null);

        //First Connection with Broker
        String ip="192.168.1.4";
        int port=4321;

        lastbroker=new Broker(0,ip,port); //Save the lastbroker that Subscriber has been connected with
        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(ip, String.valueOf(port),"0");//with 0 -> its the first time you connect

    }


    private class AsyncTaskRunner extends AsyncTask<String,String,String>{
        private String resp;
        private View v;
        ProgressDialog progressDialog;

        public AsyncTaskRunner(View v){
            this.v=v;
            progressDialog=new ProgressDialog(v.getContext());
        }
        public AsyncTaskRunner(){

            progressDialog=new ProgressDialog(MainActivity.this);
        }

        //Parameters is ip,port for the broker that we want to be connected with
        //and the 3rd parameter is to identify if it is the first conenction
        //with this broker or it is already connected.

        protected String doInBackground (String... params){
            publishProgress("Connecting...");
            try{
                String ip=params[0];
                int port=Integer.parseInt(params[1]);

                if(params[2].equals("0")) {
                    //Create the connection
                    s.requestSocket = new Socket(InetAddress.getByName(ip), port);
                    s.out = new ObjectOutputStream(s.requestSocket.getOutputStream());
                    s.in = new ObjectInputStream(s.requestSocket.getInputStream());
                    s.connect();
                }
                resp="Successful connection.";


                //We have a topic to search for.
                if(s.getTopic()!=null){

                    progressDialog.dismiss();

                    //We use GlobalState class to pass Socket,InputStream,OutputStream
                    //,that have been created, in MapsActivity

                    GlobalState state=((GlobalState) getApplicationContext());
                    state.setS(s.getRequestSocket());
                    state.setIn(s.in);
                    state.setOut(s.out);

                    Intent mySecondActivity = new Intent(MainActivity.this, MapsActivity.class);
                    mySecondActivity.putExtra("Subscriber",s);//Also,send the subscriber to MapsActivity
                    startActivity(mySecondActivity);
                }
            }catch(Exception e){
                e.printStackTrace();
                Log.e("DEBUG",e.getMessage());
                resp="Unsuccessful connection.";
            }
            return resp;
        }

        protected void onPostExecute(String result){
            progressDialog.dismiss();
            finalResult.setText(result);
        }

        protected void onPreExecute(){

            progressDialog.setTitle("ProgressDialog");
            progressDialog.setMessage("Wait for connection");
            //Setting a " Cancel" Button
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener(){
                // Set a click listener for progress dialog cancel button
                @Override
                public void onClick(DialogInterface dialog, int which){
                    // dismiss the progress dialog
                    progressDialog.dismiss();
                    cancel(true);
                }
            });
            progressDialog.show();
        }

        protected void onProgressUpdate(String... text ){
            //Things to be done while execution of long running operation is in progress.
            finalResult.setText(text[0]);
        }
    }

    /**
     * Reading text file busLinesNew.txt to save busLines
     * @param busline(An arrayList with busLines)
     * @return
     * @throws IOException
     */
    public ArrayList<String> readTXT(ArrayList<String> busline) throws IOException {
        InputStream in = null;
        String[] elements;

        try {
           in= getAssets().open("DS_project_dataset/busLinesNew.txt");
        } catch (FileNotFoundException e) {
            System.out.println("File is not found.");
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        int count=0;
        while ((line = br.readLine()) != null && !line.isEmpty()) {
                elements = line.split(",");
                if(!busline.contains(elements[1]))
                    busline.add(elements[1]);
                count++;
        }

        br.close();
        in.close();
        return busline;
    }


}


