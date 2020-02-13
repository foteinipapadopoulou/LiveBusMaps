package  com.example.myapplication.domain;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler extends Thread {
    Socket s;
    ObjectOutputStream out;
    ObjectInputStream in;
    Broker b;

    public ClientHandler(Socket s ,ObjectOutputStream out,ObjectInputStream in,Broker b ){
        this.s=s;
        this.out=out;
        this.in=in;
        this.b=b;
    }

    @Override
    public synchronized void run(){
       int port=-1;
       Value value=null;
       String received;
       while(true){
            try {
                received = in.readUTF();
                if (received.equals("1")){
                    /*****connection with subscriber*****/
                    out.writeUTF("You 're connected at " + s + " and You're a subscriber.");
                    out.flush();
                    List<Broker> br = new ArrayList<>();

                    for (Broker brok : b.getBrokers()) {
                        List<Topic> topics=new ArrayList<>();
                        for(Topic t:brok.getResponsibleTopics()){
                            topics.add(t);
                        }
                        br.add(new Broker(brok.getId(), brok.getIp(), brok.getPort(),topics));
                    }
                    out.writeObject(br);//Sends the list of brokers
                    out.flush();

                    int id = in.readInt();//Read id
                    Subscriber s1 = new Subscriber(id);
                    b.registeredSubscribers.add(s1);

                    boolean waiting=true;

                    while(waiting) {
                        Topic busid = (Topic) in.readObject();//Read Topic
                        out.flush();
                        s1.setTopic(busid);
                        waiting=b.sending(busid, in, out, s1);//Checks if sub wants the same topic
                    }
                    out.writeObject(null);
                    out.flush();
                    b.registeredSubscribers.remove(s1);
                }
                else if(received.equals("2")){
	            	 /*****connection with Publisher*****/
                    out.writeUTF("You 're connected at " + s + " and You're a publisher.");
                    out.flush();
                    Publisher p =(Publisher) in.readObject();

                    b.registeredPublisher.add(p);
                    b.pull(in,out,p);
                    
	            }else if (received.equals("4")){
                    /*****First connection with Publisher *****/
                    out.writeUTF("Broker>You 're connected first time at " + s );
                    out.flush();
                    
                    List<Broker> br = new ArrayList<>();
                    
                    for (Broker brok : b.getBrokers()) {
                    	List<Topic> topics=new ArrayList<>();
                    	for(Topic t:brok.getResponsibleTopics()){
                    		topics.add(t);
                    	}
                    	br.add(new Broker(brok.getId(), brok.getIp(), brok.getPort(),topics));                   
                    }
                    out.writeObject(br);
                    out.flush();
                    
                }else if (received.equals("3")) {
                    /*****connection with Broker*****/
                	port=in.readInt();
                    out.writeUTF("You 're connected at " + s + " and You're a broker.");
                    out.flush();
                
                }else{
                    out.writeUTF("Invalid input");
                    out.flush();
                    Thread.sleep(500);
                    break;
                }
            }catch(IOException e ){
                b.connectionclose(s,in,out);
                System.out.println("Exit:"+s);
                b.removeBroker(s.getInetAddress().getHostAddress(), port);
                break;
            
            }catch(InterruptedException e) {
                e.printStackTrace();
                b.connectionclose(s, in, out);
                System.out.println("Exit"+s);
                b.removeBroker(s.getInetAddress().getHostAddress(), port);
                break;
            } catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
        }
    }
}

