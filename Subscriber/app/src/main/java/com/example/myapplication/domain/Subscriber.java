package  com.example.myapplication.domain;

import java.io.*;
import java.net.*;
import java.util.*;

public class Subscriber implements Serializable{

	private static final long serialVersionUID = 1L;
	private transient  int id;
	private List<Broker> brokers;
	private List<Value> values;
	private Topic t;
 	public transient  Socket requestSocket = null;
	public transient ObjectOutputStream out = null;
	public transient ObjectInputStream in = null;

	//Constructors

	public Subscriber(int id, Topic t) {
		super();
		this.id = id;
		this.t=t;
		brokers=new ArrayList<>();
		values=new ArrayList<>();
	}

    public Subscriber(int i) {
        this.id=i;
    }

    //Getters and Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {this.id = id;}

	public List<Broker> getBrokers() {
		return brokers;
	}

	public void setBrokers(List<Broker> brokers) {
		this.brokers = brokers;
	}

	public List<Value> getValues() {
		return values;
	}

	public void setValues(List<Value> values) {
		this.values = values;
	}

    public void setTopic(Topic t) {this.t = t;}
	public Topic getTopic(){return t;}

	public Socket getRequestSocket() {return requestSocket;}

	public ObjectOutputStream getOut() {return out;}

	public ObjectInputStream getIn() {return in;}

	public void setRequestSocket(Socket requestSocket) {this.requestSocket = requestSocket;}

	public void setOut(ObjectOutputStream out) {this.out = out;}

	public void setIn(ObjectInputStream in) {this.in = in;}

	/******************/

	/**
	 * Handling first connection with Broker.
	 * Subscriber receives the brokerList and their responsible topics
	 * and gives his ID.
	 * @throws IOException
	 */
	public void connect() throws IOException {
        try {
        	out.flush();
			out.writeUTF("1");//Writes 1 so as to be  identified by Broker
		    out.flush();

			System.out.println(in.readUTF());

			setBrokers((List<Broker>) in.readObject());//Takes the broker  List
			System.out.println("Brokers received!");

			out.writeInt(getId());//Sends ID
			out.flush();
		}catch(UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		   	requestSocket.close();
		   	in.close();
		   	out.close();
		}catch(InterruptedIOException e){
			System.out.println("Interrupted!");
			return;
		}catch (IOException ioException) {
			ioException.printStackTrace();
			System.out.println("Server "+ requestSocket.getLocalAddress()+" "+ requestSocket.getPort()+" is down");
		    requestSocket.close();
			in.close();
			out.close();
		} catch (ClassNotFoundException e) {
        	e.printStackTrace();
			requestSocket.close();
			in.close();
			out.close();
		}
	}

	/*
	*Find who broker is responsible for this Topic t
	* by searching into brokersList.
	 */
	public Broker findBroker(Topic t) {
		for(Broker b:getBrokers()){
			for(Topic top:b.getResponsibleTopics()){
				if(top.equals(t)) return b;
			}
		}
		return null;
	}

}
