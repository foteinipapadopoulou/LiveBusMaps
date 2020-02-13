package  com.example.myapplication.domain;
import java.io.Serializable;

public class Bus implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String LineCode,LineId,RouteCode,VehicleId,RouteType,DescriptionEnglish;

	public Bus(String lineCode, String lineId, String routeCode,String vehicleid, String routeType, String descriptionEnglish) {
		LineCode = lineCode;
		LineId = lineId;
		RouteCode = routeCode;
		RouteType = routeType;
		VehicleId=vehicleid;
		DescriptionEnglish = descriptionEnglish;
	}

	public Bus(String lineCode, String lineId, String routeCode, String vehicleId) {
		LineCode = lineCode;
		LineId = lineId;
		RouteCode = routeCode;
		VehicleId = vehicleId;
	}

	public String getLineCode() {
		return LineCode;
	}

	public void setLineCode(String lineCode) {
		LineCode = lineCode;
	}

	public String getLineId() {
		return LineId;
	}

	public void setLineId(String lineId) {
		LineId = lineId;
	}

	public String getRouteCode() {
		return RouteCode;
	}

	public void setRouteCode(String routeCode) {
		RouteCode = routeCode;
	}

	public String getVehicleId() {
		return VehicleId;
	}

	public void setVehicleId(String vehicleId) {
		VehicleId = vehicleId;
	}

	public String getRouteType() {
		return RouteType;
	}

	public void setRouteType(String routeType) {
		RouteType = routeType;
	}

	public String getDescriptionEnglish() {
		return DescriptionEnglish;
	}

	public void setDescriptionEnglish(String descriptionEnglish) {
		DescriptionEnglish = descriptionEnglish;
	}

	@Override
	public String toString() {
		return "Bus{" +
				"LineCode='" + LineCode + '\'' +
				", LineId='" + LineId + '\'' +
				", RouteCode='" + RouteCode + '\'' +
				", VehicleId='" + VehicleId + '\'' +
				", RouteType='" + RouteType + '\'' +
				", DescriptionEnglish='" + DescriptionEnglish + '\'' +
				'}';
	}
	
	public boolean equalsLineId(Bus b){
		if(this.getLineId().equals(b.getLineId())) return true;
		else return false;
	}
}

