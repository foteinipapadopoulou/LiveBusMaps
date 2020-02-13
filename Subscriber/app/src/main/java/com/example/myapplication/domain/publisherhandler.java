package  com.example.myapplication.domain;
import java.io.*;
import java.net.*;

public class publisherhandler extends Thread {

	private Socket s;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Publisher p;
	private Broker b;
	public publisherhandler(Socket s, ObjectOutputStream out, ObjectInputStream in, Publisher p,Broker b) {
		super();
		this.s = s;
		this.out = out;
		this.in = in;
		this.p = p;
		this.b=b;
	}
	public void push() throws IOException{
		
		for(Value v:p.getValues()){
			if(p.hasBrokerValue(v,b)){
				System.out.println("Sending..");
				out.writeObject(v);
				out.flush();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	 @Override
	 public void run(){
        try {
            out.writeUTF("2");
            out.flush();
            System.out.println(in.readUTF());
            out.writeObject(p);//Gives ID of Publisher
            out.flush();
            push();
            out.writeObject(null);
            if(in.read()==-1) {
            	in.close();
            	out.close();
                s.close();
            }
            System.out.println("Values are sent successfully!");
            out.writeObject(null);//Sends null in order to stop Broker reading Values
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Server "+s.getInetAddress().getHostAddress()+" "+s.getPort()+" is down");
        }
        finally {
            System.out.println(s.toString()+" is closed.");
        }
    }
    

	

}
