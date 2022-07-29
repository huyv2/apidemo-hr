package hr.dto.response;

import com.google.gson.annotations.SerializedName;

import hr.dto.BaseDto;

public abstract class ResponseBaseDto extends BaseDto {
	@SerializedName("result")
	private BaseResult result;
	
	public BaseResult getResult() {
		return result;
	}
	protected void setResult(BaseResult result) {
		this.result = result;
	}
	
	@Override
	public StringBuilder getDataForSignatureBase() {
		return super.getDataForSignatureBase().append("Result");
	}
	
	public void addResult(String code, String message, String description) {
		result.setCode(code);
		result.setMessage(message);
		result.setDescription(description);
	}
}
