package hr.api;

import java.lang.reflect.Field;
import java.security.PublicKey;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import hr.cache.Cache;
import hr.cache.CacheManager;
import hr.config.Config;
import hr.constant.CacheName;
import hr.constant.ErrorCode;
import hr.constant.ParamConstant;
import hr.constant.RuleConstant;
import hr.dto.HeaderDto;
import hr.dto.ProcessObject;
import hr.dto.request.RequestBaseDto;
import hr.signature.RsaSignature;
import hr.util.JsonParserUtil;
import hr.util.MarkerUtil;

public class RequestValidation {
	private static final Logger log = Logger.getLogger(RequestValidation.class);
	
	public static boolean validateRequest() {
		ProcessObject processObject = ProcessObject.get();
		String url = processObject.getUrl();
		String requestBody = processObject.getRequestBody();
		RequestBaseDto requestDto = null;
		String eDemoName = "";
		String logInsertCodeError = "00";
		
		try {
			Cache literalCache = (Cache) CacheManager.getInstance().getCache(CacheName.LITERAL_CLASS_CACHE_NAME);
			Object tClass = literalCache.get(url);
			
			String urlArr[] = url.split("/");
			if(urlArr.length < 3) {
				processObject.setCode(ErrorCode.CANNOT_GET_EDEMo_NAME);
				processObject.setDescription(String.format("Not found schema:  %s", url));
				processObject.setRequestDto(requestDto);
				log.debug(String.format("Request-id unknown | Cannot get edemo name | Request | Request-body %s", MarkerUtil.markForRequestBody(requestBody)));
				insertLog(requestDto, requestBody, processObject, "unknown", url, "");
				return false;
			} else {
				eDemoName = urlArr[2];
				processObject.seteDemoName(eDemoName);
			}
			
			if (tClass != null) {
				Object tObject = JsonParserUtil.toObject(requestBody, (Class<?>) tClass);
				requestDto = (RequestBaseDto) tObject;
				if (requestDto != null && requestDto.getHeaderDto() != null && requestDto.getData() != null && requestDto.getSignature() != null) {
					log.debug(String.format("EDemo-name %s | Request-id %s | Request | Parsed successfully to object | %s!!!", processObject.geteDemoName(), requestDto.getHeaderDto().getRequestId(), tClass));
				} else {
					processObject.setCode(ErrorCode.CANNOT_PARSE);
					processObject.setRequestDto(requestDto);
					log.debug(String.format("EDemo-name %s | Request-body %s | Request | Cannot parse to object %s!!!", processObject.geteDemoName(), MarkerUtil.markForRequestBody(requestBody), tClass));
					insertLog(requestDto, requestBody, processObject, eDemoName, url, ((Class<?>)tClass).getSimpleName());
					return false;
				}
			} else {
				processObject.setCode(ErrorCode.INVALID_URL);
				processObject.setDescription("Url not found");
				processObject.setRequestDto(requestDto);
				log.debug(String.format("EDemo-name %s | Request | Invalid url %s | Request-body %s", processObject.geteDemoName(), url, MarkerUtil.markForRequestBody(requestBody)));
				insertLog(requestDto, requestBody, processObject, eDemoName, url, "");
				return false;
			}
			
			processObject.setRequestDto(requestDto);
			logInsertCodeError = insertLog(requestDto, requestBody, processObject, eDemoName, url, ((Class<?>)tClass).getSimpleName());
			
			if (!logInsertCodeError.equals("00")) {
				processObject.setCode(ErrorCode.REQUESTID_DUPLICATED);
				processObject.setDescription(String.format("Request id is duplicated: %s", requestDto.getHeaderDto().getRequestId()));
				processObject.setRequestDto(requestDto);
				return false;
			}
			
			Object dataObject = requestDto.getData();
			Class<?> classData = dataObject.getClass();
			Field[] dataFields = classData.getDeclaredFields();
			
			boolean isValidFormat = false;
			Cache cache = (Cache) CacheManager.getInstance().getCache(CacheName.RULE_VALIDATION_CACHE_NAME);
			HeaderDto headerObject = requestDto.getHeaderDto();
			Class<?> classHeader = headerObject.getClass();
			String headerName = classHeader.getSimpleName();
			String dataName = classData.getSimpleName();
			HeaderDto headerRuleObject = (HeaderDto) cache.get(headerName);
			Object dataRuleObject = cache.get(dataName);
			if (headerRuleObject != null && dataRuleObject != null) {
				isValidFormat = isValidFieldRule(headerRuleObject, headerObject, dataRuleObject, dataObject, dataFields);
			} else {
				isValidFormat = requestDto.isValidAllVariables();
			}
			if (isValidFormat) {
			} else {
				processObject.setCode(ErrorCode.WRONG_FORMAT);
				processObject.setRequestDto(requestDto);
				log.debug(String.format("EDemo-name %s | Request-id %s | Request | Wrong data format fields", processObject.geteDemoName(), requestDto.getHeaderDto().getRequestId()));
				return false;
			}
			
			// Handle request signature here
			if (Boolean.parseBoolean(Config.getValue(processObject.geteDemoName() + "_IsUsingSignatureRequest"))) {
				StringBuilder sbValidationData = requestDto.getDataForSignatureBase();
				for(Field dataField : dataFields) {
					dataField.setAccessible(true);
					String value = (String)dataField.get(dataObject);
					if (value == null) {
						value = "";
					}
					sbValidationData.append(value);
				}
				sbValidationData.append(requestDto.getEndStringForSignature());
				String validationClearData = sbValidationData.toString();
				log.debug(String.format("EDemo-name %s | Request-id %s | Request | Valid clear text for validating signature: %s",
						processObject.geteDemoName(), requestDto.getHeaderDto().getRequestId(), validationClearData));
				PublicKey publicKey = RsaSignature.getPublicKey(eDemoName);
				String clientCipherData = requestDto.getSignature();
				if (clientCipherData == null) {
					clientCipherData = "";
				}
				boolean isVerifySuccess = RsaSignature.verify(validationClearData, clientCipherData, publicKey);
				if (isVerifySuccess) {
				} else {
					processObject.setCode(ErrorCode.INVALID_SIGNATURE);
					processObject.setDescription("Invalid signature");
					processObject.setRequestDto(requestDto);
					log.debug(String.format("EDemo-name %s | Request-id %s | Request | Invalid signature", processObject.geteDemoName(), requestDto.getHeaderDto().getRequestId()));
					return false;
				}
			}
			// End
			return true;		
		} catch(Exception e) {
			log.error(e);
			processObject.setCode(ErrorCode.SYSTEM_ERROR);
			processObject.setRequestDto(requestDto);
			return false;
		}
	}
	
	private static String insertLog(RequestBaseDto requestDto, String requestBody, ProcessObject processObject, String eDemoName, String url, String className) {
		String msgErr = "00";

		//Insert to log system
		
		return msgErr;
	}
	
	private static boolean isValidFieldRule(HeaderDto headerRuleObject, HeaderDto headerObject, Object dataRuleObject, Object dataObject, Field[] dataFields) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		boolean isSuccess = true;
		// Check header
		for(String rule : headerRuleObject.getRequestId().split(",")) {
			isSuccess = isCheck(rule, headerObject.getRequestId());
			if (!isSuccess) {
				break;
			}
		}
		if (isSuccess) {
			for(String rule : headerRuleObject.getSource().split(",")) {
				isSuccess = isCheck(rule, headerObject.getSource());
				if (!isSuccess) {
					break;
				}
			}
		}
		if (isSuccess) {
			for(String rule : headerRuleObject.getTimestamp().split(",")) {
				isSuccess = isCheck(rule, headerObject.getTimestamp());
				if (!isSuccess) {
					break;
				}
			}
		}
		// End
		//Check data
		if (isSuccess) {
			Class<?> classDataRule = dataRuleObject.getClass();
			for(Field dataField : dataFields) {
				dataField.setAccessible(true);
				Field dataRuleField = classDataRule.getDeclaredField(dataField.getName());
				dataRuleField.setAccessible(true);
				String ruleList = (String) dataRuleField.get(dataRuleObject);
				if (ruleList != null && !ruleList.isEmpty()) {
					String value = (String)dataField.get(dataObject);
					for(String rule : ruleList.split(",")) {
						isSuccess = isCheck(rule, value);
						if (!isSuccess) {
							break;
						}
					}
				}
				if (!isSuccess) {
					break;
				}
			}
		}
		// End
		return isSuccess;
	}
	private static boolean isCheck(String rule, String value) {
		boolean isSuccess = true;
		if (rule.equals(RuleConstant.NOT_NULL)) {
			if (value == null) {
				isSuccess = false;
			}
		} else if (rule.equals(RuleConstant.NOT_EMPTY)) {
			if (value.isEmpty()) {
				isSuccess = false;
			}
		} else if (rule.equals(RuleConstant.DATETIME)) {
			isSuccess = isDateTimeValid(value, RuleConstant.DATETIME);
		} else if (rule.startsWith(RuleConstant.MAX_LENGTH)) {
			int maxLen = Integer.parseInt(rule.substring(rule.lastIndexOf("-") + 1, rule.length()));
			if (value.length() > maxLen) {
				isSuccess = false;
			}
		} else if (rule.startsWith(RuleConstant.FIX_LENGTH)) {
			int len = Integer.parseInt(rule.substring(rule.lastIndexOf("-") + 1, rule.length()));
			if (value.length() != len) {
				isSuccess = false;
			}
		}
		return isSuccess;
	}
	private static boolean isDateTimeValid(String dateTime, String format) {
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
}
