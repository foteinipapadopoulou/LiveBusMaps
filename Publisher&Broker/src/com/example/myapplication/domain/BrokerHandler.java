package  com.example.myapplication.domain;
import java.io.*;
import java.net.*;

public class BrokerHandler extends Thread {

    Socket s;
    ObjectOutputStream out;
    ObjectInputStream in;
    Broker b;

    public BrokerHandler(Socket s, ObjectOutputStream out, ObjectInputStream in, Broker b) {
        this.s = s;
        this.out = out;
        this.in = in;
        this.b = b;
    }

    public synchronized void run() {
            try {
                    out.flush();
	                out.writeUTF("3");
	                out.flush();
	                out.writeInt(b.getPort());
	                out.flush();
	                System.out.println(in.readUTF());
	                //Wait until an error occurs
	                if(in.read()==-1) {
	                    b.connectionclose(s,in,out);
	                }
            } catch (IOException e ) {
                System.out.println("Server "+s.getInetAddress().getHostAddress()+" "+s.getPort()+" is down");
                b.removeBroker(s.getInetAddress().getHostAddress(), s.getPort());
            }finally {
                b.connectionclose(s, in, out);
            }
    }
}