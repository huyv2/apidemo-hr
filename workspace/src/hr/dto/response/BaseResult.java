package hr.dto.response;

import com.google.gson.annotations.SerializedName;

public abstract class BaseResult {
	@SerializedName("code")
	private String code;
	@SerializedName("message")
	private String message;
	@SerializedName("description")
	private String description;
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
