package  com.example.myapplication.domain;
import java.io.Serializable;

public class Topic implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String LineId;

	public Topic(String LineId) {
		super();
		this.LineId = LineId;
	}

	public String getLineId() {
		return LineId;
	}

	public void setLineId(String LineId) {
		this.LineId = LineId;
	}
	
	public boolean equals(Topic t){
		if(this.getLineId().equals(t.getLineId())) return true;
		return false;
	}
}
