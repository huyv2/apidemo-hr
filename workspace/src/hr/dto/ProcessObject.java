package hr.dto;

import java.text.SimpleDateFormat;

import hr.dto.request.RequestBaseDto;
import hr.dto.response.ResponseBaseDto;
import hr.dto.response.defaultdto.DefaultDto;
import hr.util.ErrorCodeUtil;
import hr.util.JsonParserUtil;

public class ProcessObject {
	private static ThreadLocal<ProcessObject> context = new ThreadLocal<ProcessObject>();
	private String url;
	private String ip;
	private String requestBody;
	private RequestBaseDto requestDto;
	private ResponseBaseDto responseDto;
	private String code;
	private String description = "";
	private String message = "";
	private String eDemoName;
	
	public static void set(ProcessObject processObject) {
		context.set(processObject);
	}
	public static ProcessObject get() {
		return context.get();
	}
	public static void unSet() {
		context.remove();
	}
	public static ProcessObject getNewInstance() {
		return new ProcessObject();
	}
	
	private ProcessObject() {}
	public String getRequestBody() {
		return requestBody;
	}
	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public RequestBaseDto getRequestDto() {
		return requestDto;
	}
	public void setRequestDto(RequestBaseDto requestDto) {
		this.requestDto = requestDto;
	}
	public ResponseBaseDto getResponseDto() {
		return responseDto;
	}
	public void setResponseDto(ResponseBaseDto responseDto) {
		this.responseDto = responseDto;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMessage() {
		String message;
		if (this.message == null || this.message.isEmpty()) {
			message = ErrorCodeUtil.getErrorCodeMessage(code);
		} else {
			message = this.message;
		}
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
	public String geteDemoName() {
		return eDemoName;
	}
	public void seteDemoName(String eDemoName) {
		this.eDemoName = eDemoName;
	}
	
	public String getErrorResponseBody() {
		return JsonParserUtil.toJson(getErrorResponseDto());
	}
	
	public String getErrorResponseBody(ResponseBaseDto responseDto) {
		return JsonParserUtil.toJson(responseDto);
	}
	
	public ResponseBaseDto getErrorResponseDto() {
		
		DefaultDto responseDto = new DefaultDto();
		responseDto.setHeaderDto(getRequestDto() != null ? getRequestDto().getHeaderDto() : null);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String timestamp = sdf.format(new java.util.Date());
		responseDto.addResult(getCode(), ErrorCodeUtil.getErrorCodeMessage(code), getDescription());
		if (responseDto.getHeaderDto() != null) {
			responseDto.getHeaderDto().setTimestamp(timestamp);
		}
		
		setResponseDto(responseDto);
		
		return responseDto;
	}
}
