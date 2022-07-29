package hr.dto.response.defaultdto;

import hr.dto.response.ResponseBaseDto;
import hr.dto.response.defaultdto.result.DefaultResult;

public class DefaultDto extends ResponseBaseDto {
	public DefaultDto() {
		setResult(new DefaultResult());
	}
}
