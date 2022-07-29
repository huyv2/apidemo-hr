package hr.dto;

import com.google.gson.annotations.SerializedName;

public class HeaderDto /*implements Serializable*/ {
	
	@SerializedName("request_id")
	private String requestId;
	@SerializedName("source")
	private String source;
	@SerializedName("timestamp")
	private String timestamp;
	
	public HeaderDto(String requestId, String source, String timestamp) {
		setRequestId(requestId);
		setSource(source);
		setTimestamp(timestamp);
	}
	
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
}
