package  com.example.myapplication.domain;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.*;

public class Broker implements Serializable {

    private static final long serialVersionUID = 1L;
    private int id,port;
    private String  ip;
    public List<Subscriber> registeredSubscribers;
    public List<Publisher> registeredPublisher;
    private List<Broker> brokers;
    private List<Topic> responsibleTopics;
    //Saves sha1 number that has each broker and the list of topics that is responsible for
    private HashMap<BigInteger,List<Topic>> hashMap;
    private ServerSocket ss;
    private Value value=null;
    final Lock lock = new ReentrantLock();
    final Condition notEmpty = lock.newCondition();

    /***Constructors***/

    public Broker(int id,  String ip,int port) {
        this.id = id;
        this.port = port;
        this.ip = ip;
        registeredSubscribers=new ArrayList<>();
        registeredPublisher=new ArrayList<>();
        brokers=new ArrayList<>();
        responsibleTopics=new ArrayList<>();
        hashMap=new HashMap<>();
    }

    public Broker(int id, String ip, int port, List<Topic> topics) {
        this.id = id;
        this.port = port;
        this.ip = ip;
        registeredSubscribers=new ArrayList<>();
        registeredPublisher=new ArrayList<>();
        brokers=new ArrayList<>();
        this.responsibleTopics=topics;
        hashMap=new HashMap<>();
    }

    /**Getters and Setters**/

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    public List<Broker> getBrokers() {
        return brokers;
    }

    public void setBrokers(List<Broker> brokers) { this.brokers = brokers;}

    public List<Topic> getResponsibleTopics() {
        return responsibleTopics;
    }

    public Value getValue() {return value;}

    public void setValue(Value value) { this.value = value;}

    /************************/

    //Checking among two brokers if they have the same ip and port
    public boolean equals(Broker b){
        if(b.getPort()==this.getPort() && b.getIp().equals(this.getIp())) return true;
        return false;
    }

    public static void main(String args[]) throws IOException{
        String prefix="src/";
        //Check arguments.
        if (args.length != 3) {
            System.out.println("Wrong arguments!!");
            System.out.println("Need IP,Port,ID.");
            System.exit(-1);
        }

        //Create the broker.
        Broker b =null;
        try {
            b = new Broker(Integer.parseInt(args[2]),args[0],Integer.parseInt(args[1]));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        //Read Brokers File
        b.readBrokers(prefix+ "com/example/myapplication/domain/BrokersInfo.txt");

        //Read linesID in order to  hash them
        b.readLines(prefix+ "com/example/myapplication/domain/DS_project_dataset/busLinesNew.txt");
        b.setTopics();

        System.out.println("I am responsible for :");
        for(Topic t:b.getResponsibleTopics()){
            System.out.print(t.getLineId()+" ");
        }
        b.openBroker();

        /*Connecting with other Brokers via ID
         *ex. Broker with ID:0 waiting for connections
         *Broker with ID:1 is connected with Broker with ID:0
         * Broker with ID:2 is connected with Broker with ID:0 and ID:1
         * ..etc
         */
        for(Broker br:b.getBrokers()){
            if(br.equals(b))
                break;
            else
                b.connect(br.getIp(),br.getPort());
        }
        System.out.println();
        //accept Connections with Publishers and Subscribers
        b.acceptConnection();
        b.closeBroker();


    }


    /**Server's function**/
    public void openBroker(){
        try {
            ss = new ServerSocket(this.port);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void connectionclose(Socket s,ObjectInputStream in ,ObjectOutputStream out){
        try{
            in.close();
            out.close();
            s.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void closeBroker(){
        try {
            ss.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    //Connect with the other Brokers
    private void connect(String ip, int port) {
        Socket requestSocket=null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            requestSocket = new Socket(InetAddress.getByName(ip), port);
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
            Thread t=new BrokerHandler(requestSocket,out,in,this);
            t.start();
        }catch(UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        }catch(IOException ioException) {
            System.err.println("Server "+ip +" port: "+port+" that you try to connect is down.");
        }
    }

    public  void acceptConnection(){
        Socket connection = null;
        ObjectOutputStream out=null;
        ObjectInputStream in=null;
        try{
            while(true) {
                System.out.println("Waiting for connections!");
                connection = ss.accept();
                System.out.println("A new client is connected : " + connection);
                out = new ObjectOutputStream(connection.getOutputStream());
                in = new ObjectInputStream((connection.getInputStream()));
                Thread t= new ClientHandler(connection,out,in,this);
                t.start();
            }
        }catch (Exception e) {
            connectionclose(connection,in,out);
            e.printStackTrace();
        }
    }
    /*******************/

    //Read IPs and ports for other Brokers
    public void readBrokers(String file) throws IOException {
        FileReader f = null;
        try {
            f = new FileReader(file);
        } catch (FileNotFoundException e) {
            System.out.println("File " + file + " is not found.");
            System.exit(-1);
        }

        BufferedReader br = new BufferedReader(f);
        String line = null;
        Broker b;
        List<BigInteger> x=new ArrayList<>();//saves Big Integer numbers that are produced from sha1function
        int i=0; //Gives id to each Broker
        while ((line = br.readLine()) != null && !line.isEmpty()) {
            String[] elements = line.split(",");
            b=new Broker(i,elements[0], Integer.parseInt(elements[1]));

            if(this.equals(b)) brokers.add(this);
            else brokers.add(b);
            //Setting List of brokers
            b.setBrokers(brokers);

            x.add(sha1function(b.getIp()+b.getPort()));
            i++;
        }
        if(brokers.size()==0) System.out.println("No Brokers Found in txt file.");
        Collections.sort(x);//Sort according to list of big integer number
        //Initializing the HashMap
        for(BigInteger n:x){
            hashMap.put(n,new ArrayList<Topic>());
        }
    }

    //Read buslines and hash them to brokers
    public void readLines(String s) throws IOException {
        FileReader f = null;
        String[] elements;
        try {
            f = new FileReader(s);
        } catch (FileNotFoundException e) {
            System.out.println("File is not found.");
            System.exit(-1);
        }

        BufferedReader br=new BufferedReader(f);
        String line;
        List<String> b=new ArrayList<>();

        while ((line = br.readLine()) != null && !line.isEmpty()) {
            elements = line.split(",");

            if(!b.contains(elements[1])){
                b.add(elements[1]);
                calculateKeys(elements[1]);
            }
        }
    }

    private void setTopics() {
        BigInteger key;
        List<Topic> t;
        for(Broker b:brokers){
            //find each broker from hashmap
            //and initializing their topics into
            //the arraylist responsibletopics.
            key=sha1function(b.getIp()+b.getPort());
            t= hashMap.get(key);

            for(Topic top:t){
                b.addResponsibleTopics(top);
                if(this.equals(b)){
                    this.addResponsibleTopics(top);
                }
            }
        }
    }

    public boolean addResponsibleTopics(Topic t) {
        boolean exist=false;
        //Find if topic already exists
        for(Topic top:getResponsibleTopics()){
            if(top.equals(t)) exist=true;
        }
        if(!exist) {
            responsibleTopics.add(t);
            return true;
        }
        return false;
    }

    public void calculateKeys(String s){
        BigInteger topic=sha1function(s);
        boolean found=false;
        BigInteger key = null;
        for(BigInteger  r:hashMap.keySet()){
            //if r < topic
            if(r.compareTo(topic)==1) {
                found=true;
                key=r;
                break;
            }
        }
        if(!found){
            BigInteger hashedTopic= topic.mod( BigInteger.valueOf(brokers.size()));
            Broker b1=search(hashedTopic);
            key= sha1function(b1.getIp()+b1.getPort());
        }

        updateTopic(new Topic(s),key);
    }

    //Add a new Topic in the HashMap given the key that is calculated
    public void updateTopic(Topic t,BigInteger key){
        //Ψάχνουμε ποιος broker εχει τα περισσοτερα και τα λιγοτερα κλειδιά
        //ώστε να μοιραστούν ομοιόμορφα τα κλειδιά.
        BigInteger max=computeMax();
        BigInteger min=computeMin();
        if (max==key) key=min;//change broker that will be responsible for this topic
        List<Topic> topics= hashMap.get(key);
        boolean exist=false;
        for(Topic top:topics){
            if(top.equals(t)) exist=true;
        }
        if(!exist)
            topics.add(t);
    }

    private BigInteger computeMin() {
        int min=0;
        BigInteger minKey=null;
        int i=0;
        for(BigInteger k:hashMap.keySet()){
            if(i==0){
                min=hashMap.get(k).size();
                minKey=k;
            }
            if(min>hashMap.get(k).size()){
                min=hashMap.get(k).size();
                minKey=k;
            }
            i++;
        }
        return minKey;
    }

    private BigInteger computeMax() {
        int max=-1;
        BigInteger maxKey=null;
        for(BigInteger k:hashMap.keySet()){
            if(max<hashMap.get(k).size()){
                max=hashMap.get(k).size();
                maxKey=k;
            }
        }
        return maxKey;
    }

    //Search by id of Broker
    private Broker search(BigInteger h){
        for (Broker b:getBrokers()){
            if(h.compareTo(BigInteger.valueOf(b.getId()))==0)  return b;
        }
        return null;
    }
    /*
     *Implementation of sha1 function
     *It takes a string and returns a bigInteger from SHA-1 function
     */
    public static BigInteger sha1function(String s){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(s.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            return no;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void removeBroker(String ip,int port){
        for(Broker b:getBrokers()){
            if(b.equals(new Broker(0,ip,port))) {
                getBrokers().remove(b);
                System.out.println("Success removing "+ip+" "+port);
                break;
            }
        }
    }

    public boolean isActivePublisher(Topic t){
        boolean flag=false;
        for(Publisher p :registeredPublisher){
            boolean contains=false;
            //Search who publisher is responsible for this topic.
            for(Topic topic :p.getTopics()) {
                if(topic.equals(t)) contains=true;
            }
            if(contains) {
                //If state ==1 then publisher is not active
                if(p.getState()==1) flag=false;
                else flag=true;
            }
        }
        return flag;
    }

    public boolean sending(Topic busid, ObjectInputStream in,ObjectOutputStream out,Subscriber s){
        int waiting=-1;
        try{
            do{
                lock.lock();

                if(!isActivePublisher(busid)) {
                    //If publisher is down then immediately sends.
                    System.out.println("This publisher is down ");
                    out.writeObject(new Value(new Bus(null, null, null, null, null, "Sensor for this publisher is down"), 0, 0, null));
                }

                while(value==null || !value.getBus().getLineId().equals(busid.getLineId())){
                    long start=System.currentTimeMillis();
                    notEmpty.await(40, TimeUnit.SECONDS);// Wait for 40 seconds
                    long estimatedTime=System.currentTimeMillis()-start;//When he wakes up we compute the time he waited.

                    if(!isActivePublisher(busid)|| estimatedTime>=40000){
                        out.writeObject(new Value(new Bus(null,null,null,null,null,"Sensor for this publisher is down"),0,0,null));
                        out.flush();
                        waiting=in.readInt();
                        System.err.println(waiting);
                        s.setTopic(null);
                        lock.unlock();
                        return true;
                    }
                }

                if(value.getBus().getLineId().equals(busid.getLineId())){
                    out.writeObject(value);
                    out.flush();
                    waiting=in.readInt();//Wait for reply if he wants the same topic again.
                    if(waiting==1){//1->he doesn't want the same topic.else continue
                        lock.unlock();
                        return true;
                    }
                }
                lock.unlock();
                Thread.sleep(1000);
            }while(value!=null);
        }catch(InterruptedException e){
            e.printStackTrace();
            registeredSubscribers.remove(s);
            lock.unlock();
        } catch (IOException e) {
            e.printStackTrace();
            registeredSubscribers.remove(s);
            lock.unlock();
        }
        return false;
    }

    /**Find if there is subscriber that wants this topic**/
    public boolean existSubWithTopic(String lineId) {
        for(Subscriber s:registeredSubscribers){
            if (s.getTopic() != null) {
                if(s.getTopic().getLineId().equals(lineId)) return true;
            }
        }
        return false;
    }

    /**Receiving values from Publishers**/
    public void pull( ObjectInputStream in,ObjectOutputStream out,Publisher p){
        try {
            do {
                lock.lock();
                value = (Value) in.readObject();

                System.out.println("I received a value");
                System.out.println(value.toString());
                //Find if there are subscribers that want this topic
                if (this.existSubWithTopic(value.getBus().LineId)) {
                    notEmpty.signalAll();
                    System.out.println("Trying to send");
                }

                lock.unlock();
                Thread.sleep(1000);
            } while (value != null);
        }catch( SocketException e){
            //If a publisher is down then change its state
            p.setState(1);
            System.err.println("A publisher is down");
            notEmpty.signalAll();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally{
            lock.unlock();
        }
    }
}
