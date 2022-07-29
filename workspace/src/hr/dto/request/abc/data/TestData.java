package hr.dto.request.abc.data;

import com.google.gson.annotations.SerializedName;

public class TestData {
	
	@SerializedName("name")
	private String name;
	@SerializedName("occupation")
	private String occupation;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOccupation() {
		return occupation;
	}
	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}
}
