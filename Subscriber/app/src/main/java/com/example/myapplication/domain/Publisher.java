package  com.example.myapplication.domain;
import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class Publisher implements Serializable {
    private static final long serialVersionUID = 1L;
	private int id;
	private int state;
	private List<Broker> brokers;
	private List<Topic> topics;
    private List<Value> values;
    private List<Bus> buses;
    private Hashtable<String,String> lines;



    //Constructor
    public Publisher(int id){
        this.id=id;
        this.state=0;
        buses=new ArrayList<>();
        lines=new Hashtable<>();
        values=new ArrayList<>();
        topics=new ArrayList<>();
        brokers=new ArrayList<>();
    }
	
    public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public List<Broker> getBrokers() {
		return brokers;
	}
	public void setBrokers(List<Broker> brokers) {
		this.brokers = brokers;
	}
	public List<Bus> getBuses() {
		return buses;
	}
	public void setBuses(List<Bus> buses) {
		this.buses = buses;
	}
	public List<Topic> getTopics() {
		return topics;
	}
	public List<Value> getValues() {
		return values;
	}
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
	//initializing the lists of values,topics and buses
    public void readTXT(int i,int Nbuses) throws IOException {
        String prefix="src/main/assets/";
        FileReader f = null;
        String[] elements;
        try {
            f = new FileReader(prefix+"DS_project_dataset/busLinesNew.txt");
        } catch (FileNotFoundException e) {
            System.out.println("File is not found.");
            System.exit(-1);
        }

        BufferedReader br = new BufferedReader(f);
        String line;
        //read the txt file of buslines and fills the table with keys:lineid and values:linecode
        int count=0;
        while ((line = br.readLine()) != null && !line.isEmpty()&& count<Nbuses) {
            if(count>=i) {
                elements = line.split(",");
                lines.put(elements[1], elements[0]);
            }
            count++;
        }

        //fills the list of topics with line ids from keys of the lines table
        for(String l:lines.keySet()){
            topics.add(new Topic(l));
        }

        br.close();
        f.close();
        Bus b;
        String id;
        f=new FileReader(prefix+"DS_project_dataset/busPositionsNew.txt");
        br=new BufferedReader(f);
        //read the text file of bus position and creates bus objects
        //and fills the list of values
        while ((line = br.readLine()) != null && !line.isEmpty()){
            elements = line.split(",");
            if(lines.containsValue(elements[0])){
                id= findId(elements[0]);
                b=new Bus(elements[0],id,elements[1],elements[2]);
                if(!hasBus(b)) buses.add(b);
                
                values.add(new Value(b,Double.parseDouble(elements[3]),Double.parseDouble(elements[4]),elements[5]));
            }
        }
        br.close();
        f.close();
        f=new FileReader(prefix+"DS_project_dataset/RouteCodesNew.txt");
        br=new BufferedReader(f);
        //after reading the routecodesnew txt we are adding the remaining variables of buses
        while ((line = br.readLine()) != null && !line.isEmpty()) {
            elements = line.split(",");
            if(lines.containsValue(elements[1]))  find(elements);
        }
        
        br.close();
        f.close();
    }

  //fills the remaining variables of List of buses and values
    private void find(String [] element) {
        String routecode=element[0];
        String linecode=element[1];
        String routetype=element[2];
        String description=element[3];
        for(Bus bus:buses){
            if(bus.getLineCode().equals(linecode) && bus.getRouteCode().equals(routecode)){
                bus.setDescriptionEnglish(description);
                bus.setRouteType(routetype);
            }
        }
        
        for(Value v:values){
            Bus bus=v.getBus();
            if(bus.getLineCode().equals(linecode) && bus.getRouteCode().equals(routecode)){
                bus.setDescriptionEnglish(description);
                bus.setRouteType(routetype);
            }

        }
    }
  //check if there is already a bus object with the same vehicleid
    public boolean hasBus(Bus b){
        for(Bus bus:buses){
            if(bus.equalsLineId(b)) return true;
        }
        return false;
    }
    
    private String findId(String value){
        for (Map.Entry entry:lines.entrySet()){
            if(value.equals(entry.getValue())){
                return (String) entry.getKey();
            }
        }
        return null;
    }
	
    @SuppressWarnings("unchecked")
	public void connectFirstTime(String ip, int port) throws IOException {
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            requestSocket = new Socket(InetAddress.getByName(ip), port);
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                in = new ObjectInputStream(requestSocket.getInputStream());
                out.flush();
                out.writeUTF("4");//Writes 4 so as to identify Client Handler that is the first connection
                out.flush();
                System.out.println(in.readUTF());
                setBrokers((List<Broker>)in.readObject());//Takes the broker  List
                System.out.println("Brokers received!");
                requestSocket.close();
                in.close();
                out.close();
                System.out.println(requestSocket.toString()+" is closed.");
        }catch(UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        }catch (IOException ioException) {
            ioException.printStackTrace();
            System.out.println("Server "+requestSocket.getLocalAddress()+" "+requestSocket.getPort()+" is down");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    
    public static void main(String args[]) throws IOException {
		String ip="127.0.0.1";
		int port =4321;
		int x1=0,x2=20;
		Publisher p= new Publisher(0);
		p.readTXT(x1,x2);
		System.out.println("Topics that I'm responsible for :");
        for(Topic t:p.getTopics()){
            System.out.print(t.getLineId()+" ");
        }
        System.out.println();
        p.connectFirstTime(ip,port);
        
        for(Broker b:p.getBrokers()){
        	System.out.println("Broker id: "+b.getId()+",ip: "+b.getIp()+",port: "+b.getPort());
        	System.out.print("Topics : ");
        	for(Topic t:b.getResponsibleTopics()){
        		System.out.print(t.getLineId()+ " ");
        	}
        	System.out.println();
        }
		
        for(Broker b:p.getBrokers()){
        	for(Topic t:b.getResponsibleTopics()){
        		 if(p.hasTopic(t)){
        			
        			p.connect(b.getIp(), b.getPort(),b);
        			break;
        		}
        	}
        } 
	}

    public void connect(String ip, int port,Broker b) throws IOException {
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            requestSocket = new Socket(InetAddress.getByName(ip), port);
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
            Thread t = new publisherhandler(requestSocket, out, in, this,b);
            t.start();
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        }catch(ConnectException e){
            System.err.println("Connection is refused because Server :"+ip+" "+port+ " is down.");
        }catch (IOException ioException) {
            ioException.printStackTrace();
            System.out.println("Server " + requestSocket.getLocalAddress() + " " + requestSocket.getPort() + " is down");
        }
    }

	private boolean hasTopic(Topic t) {
		for (Topic top:getTopics()){
			if(top.equals(t)) {
				System.out.println(top.getLineId());
				System.out.println(t.getLineId());
				return true;
			}
		}
		return false;
	}

	public boolean hasBrokerValue(Value v,Broker b) {
		for(Topic t:b.getResponsibleTopics()){
			
			if(v.getBus().getLineId().equals(t.getLineId())){
				
				
				return true;
			}
		}
		return false;
	}


    

}
