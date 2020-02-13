package  com.example.myapplication.domain;
import java.io.Serializable;

public class Value implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Bus bus;
	double latitude;
	double longitude;
	String time;
	public Value(Bus bus, double latitude, double longitude,String time) {
		super();
		this.bus = bus;
		this.latitude = latitude;
		this.longitude = longitude;
		this.time=time;
	}
	public Bus getBus() {
		return bus;
	}
	
	public void setBus(Bus bus) {
		this.bus = bus;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public boolean equalsLineId(Value v){
		if(this.getBus().equalsLineId(v.getBus())) return true;
		else return false;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	@Override
	public String toString() {
		return "Value [bus=" + bus + ", latitude=" + latitude + ", longitude=" + longitude + ", time=" + time + "]";
	}
}
