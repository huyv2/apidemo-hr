package hr.dto.response.abc;

import hr.dto.response.ResponseBaseDto;
import hr.dto.response.abc.result.TestResult;

public class TestDto extends ResponseBaseDto {
	public TestDto() {
		setResult(new TestResult());
	}
}
