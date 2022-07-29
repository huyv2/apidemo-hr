package hr.dto;

import java.text.SimpleDateFormat;

import com.google.gson.annotations.SerializedName;

public abstract class BaseDto {
	
	@SerializedName("header")
	private HeaderDto header = null;
	@SerializedName("signature")
	private String signature = null;
	
	public HeaderDto getHeaderDto() {
		return header;
	}
	public void setHeaderDto(HeaderDto header) {
		this.header = header;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	
	public boolean isValidAllVariables() {
		return isValidParentClass() && isValidSubClass();
	}
	private boolean isValidParentClass() {
		boolean isValid = false;
		
		if (header != null &&
				header.getRequestId() != null && !header.getRequestId().isEmpty() &&
				header.getSource() != null && !header.getSource().isEmpty() &&
				isDateTimeValid(header.getTimestamp(), "yyyyMMddHHmmss") /* &&
				signature != null && !signature.isEmpty()*/) {
			isValid = true;
		}
		
		return isValid;
	}
	/**
	 * Validate request fields format
	 * @return true or false as boolean
	 */
	protected boolean isValidSubClass() {
		return true;
	}
	protected boolean isDateTimeValid(String dateTime, String format) {
		boolean isValid = false;
		
		if (dateTime != null && !dateTime.isEmpty()) {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			sdf.setLenient(false);
			
			try {
				sdf.parse(dateTime);
				isValid = true;
			} catch(Exception e) {
			}
		}
		
		return isValid;
	}
	
	public StringBuilder getDataForSignatureBase() {
		String requestId = header.getRequestId();
		if (requestId == null) {
			requestId = "";
		}
		String source = header.getSource();
		if (source == null) {
			source = "";
		}
		String timestamp = header.getTimestamp();
		if (timestamp == null) {
			timestamp = "";
		}
		StringBuilder sbAppendedHeader = new StringBuilder("Header")
				.append(requestId).append(source).append(timestamp);
		return sbAppendedHeader;
	}
	public String getEndStringForSignature() {
		return "End";
	}
}
