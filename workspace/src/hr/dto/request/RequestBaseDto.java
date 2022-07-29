package hr.dto.request;

import hr.dto.BaseDto;

public abstract class RequestBaseDto extends BaseDto {
	public abstract Object getData();
	
	@Override
	public StringBuilder getDataForSignatureBase() {
		return super.getDataForSignatureBase().append("Data");
	}
}
