package hr.function.abc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import hr.api.EDemo;
import hr.config.Config;
import hr.constant.ErrorCode;
import hr.dto.ProcessObject;
import hr.dto.request.RequestBaseDto;
import hr.dto.request.abc.TestDto;
import hr.dto.response.ResponseBaseDto;
import hr.util.JsonParserUtil;

public class TestFunction extends EDemo {
	private static final long serialVersionUID = 1L;

	@Override
	protected ResponseBaseDto execute(RequestBaseDto requestBaseDto) {
		TestDto requestDto = (TestDto) requestBaseDto;
		hr.dto.response.abc.TestDto responseDto = new hr.dto.response.abc.TestDto();
		
		// Logic here
		
		return responseDto;
	}
}
