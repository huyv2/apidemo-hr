package hr.dto.request.abc;

import com.google.gson.annotations.SerializedName;

import hr.dto.request.RequestBaseDto;
import hr.dto.request.abc.data.TestData;

public class TestDto extends RequestBaseDto {
	
	@SerializedName("data")
	private TestData data;
	
	@Override
	public TestData getData() {
		return data;
	}
	public void setData(TestData data) {
		this.data = data;
	}
	
	
	public void addData(String enrollmentId, String cardNo, String idNo, String customerName, String mobileNo) {
		data = new TestData();
		data.setEnrollmentId(enrollmentId);
		data.setCardNo(cardNo);
		data.setIdNo(idNo);
		data.setCustomerName(customerName);
		data.setMobileNo(mobileNo);
	}
	
	
	
	@Override
	protected boolean isValidSubClass() {
		boolean isValid = false;
		
		if (data != null &&
				data.getEnrollmentId() != null && !data.getEnrollmentId().isEmpty() &&
				data.getCardNo() != null && !data.getCardNo().isEmpty() &&
				data.getIdNo() != null && !data.getIdNo().isEmpty() &&
				data.getCustomerName() != null && !data.getCustomerName().isEmpty() &&
				data.getMobileNo() != null && !data.getMobileNo().isEmpty()) {
			isValid = true;
		}
		
		return isValid;
	}
}
