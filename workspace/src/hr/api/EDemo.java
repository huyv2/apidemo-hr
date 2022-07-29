package hr.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import hr.cache.Cache;
import hr.cache.CacheManager;
import hr.config.Config;
import hr.constant.CacheName;
import hr.constant.ErrorCode;
import hr.constant.ParamConstant;
import hr.dto.ProcessObject;
import hr.dto.request.RequestBaseDto;
import hr.dto.response.BaseResult;
import hr.dto.response.ResponseBaseDto;
import hr.factory.ExecutorFactory;
import hr.signature.RsaSignature;
import hr.util.HttpUtil;
import hr.util.JsonParserUtil;
import hr.util.MarkerUtil;

public abstract class EDemo extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public final Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public final void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType(ParamConstant.JSON_CONTENT_TYPE);
		java.io.PrintWriter out=resp.getWriter();
		ProcessObject processObject = ProcessObject.get();
		processObject.setCode(ErrorCode.UNSUPPORT_METHOD);
		processObject.setDescription("Unsupported Method: GET");
		String msg = processObject.getErrorResponseBody();
        out.println(msg);
        out.flush();
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		try {
			ExecutorService executor = ExecutorFactory.getThreadPoolExecutor();
			log.debug(req.isAsyncSupported());
			final AsyncContext asyncContext = req.startAsync();
			asyncContext.setTimeout(1200000);
			//asyncContext.start(new Runnable() {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					HttpServletRequest req = null;
					HttpServletResponse res = null;
					PrintWriter responseWriter = null;
					ProcessObject processObject = ProcessObject.getNewInstance();
					String requestBody = "";
					String responseBody = "";
					RequestBaseDto requestDto = null;
					try {
						log.info("Sent a request to threadpool normal thread");
						req = (HttpServletRequest) asyncContext.getRequest();
						res = (HttpServletResponse)asyncContext.getResponse();
						
						responseWriter = res.getWriter();
						
						HttpUtil httpUtil = HttpUtil.of(req.getReader());
						requestDto = preProcess(httpUtil, req, res);
						
						String clientIp = req.getHeader("X-FORWARDED-FOR");
						if (clientIp == null) {
							clientIp = req.getRemoteAddr();
						}
						
						ProcessObject.set(processObject);
						processObject.setUrl(req.getRequestURI());
						requestBody = httpUtil.toString();
						processObject.setRequestBody(requestBody);
						processObject.setIp(clientIp);
						
						log.info(String.format("Start | Request-body %s | Client-ip %s", MarkerUtil.markForRequestBody(requestBody), processObject.getIp()));
						
						boolean isValidation = RequestValidation.validateRequest();
						
						if (isValidation) {
							if (requestDto != null) {
								log.info(String.format("EDemo-name %s | Request-id %s | Begin service function logic", ProcessObject.get().geteDemoName(), ProcessObject.get().getRequestDto().getHeaderDto().getRequestId()));
								ResponseBaseDto responseDto = execute(requestDto);
								log.info(String.format("EDemo-name %s | Request-id %s | End service function logic", ProcessObject.get().geteDemoName(), ProcessObject.get().getRequestDto().getHeaderDto().getRequestId()));
								
								if (requestDto != null) {
									responseDto.setHeaderDto(requestDto.getHeaderDto());
								}
								SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
								String timestamp = sdf.format(new java.util.Date());
								responseDto.getHeaderDto().setTimestamp(timestamp);
								
								signResponse(processObject, responseDto);
								
								responseBody = JsonParserUtil.toJson(responseDto);
							} else {
								throw new Exception(String.format("Cannot parse request object in servlet | Request-body %s", MarkerUtil.markHRCardInJson(requestBody)));
							}
						} else {
							ResponseBaseDto responseDto = ProcessObject.get().getErrorResponseDto();
							signResponse(processObject, responseDto);
							responseBody = ProcessObject.get().getErrorResponseBody(responseDto);
						}
					} catch (Exception e) {
						log.error(e);
						try {
							processObject.setCode(ErrorCode.SYSTEM_ERROR);
							ResponseBaseDto responseDto = ProcessObject.get().getErrorResponseDto();
							signResponse(processObject, responseDto);
							responseBody = ProcessObject.get().getErrorResponseBody(responseDto);
						} catch (IllegalArgumentException | IllegalAccessException e1) {
							log.error(e1);
						}
					} finally {
						try {
							responseWriter.print(responseBody);
							responseWriter.flush();
							
							if (requestDto != null && requestDto.getHeaderDto() != null && requestDto.getData() != null && requestDto.getSignature() != null) {
								// Insert response to log system
								
								log.info(String.format("EDemo-name %s | Request-id %s | Finish | Response-body %s", ProcessObject.get().geteDemoName(), ProcessObject.get().getRequestDto().getHeaderDto().getRequestId(), responseBody));
							}
						} catch(Exception e) {
			            	log.error(e);
			            	log.info(String.format("Cannot parse request body %s", requestBody));
			            } finally {
							
			            }
						
						ProcessObject.unSet();
						asyncContext.complete();
					}
				}
			});
			
			asyncContext.addListener(new AsyncListener() {

				@Override
				public void onComplete(AsyncEvent arg0) throws IOException {
					log.debug(String.format("onComplete function %s%n", arg0.getAsyncContext()));
				}

				@Override
				public void onError(AsyncEvent arg0) throws IOException {
					log.debug("onError function");
				}

				@Override
				public void onStartAsync(AsyncEvent arg0) throws IOException {
					log.debug("onStartAsync function");
				}

				@Override
				public void onTimeout(AsyncEvent arg0) throws IOException {
					log.debug(String.format("onTimeout function %s%n", arg0.getAsyncContext()));
					/*try {
						AsyncContext asynContext = arg0.getAsyncContext();
						HttpServletRequest req = (HttpServletRequest) asynContext.getRequest();
						HttpServletResponse res = (HttpServletResponse)asynContext.getResponse();
						RequestBaseDto requestDto = preProcess(req, res);
						PrintWriter responseWriter = res.getWriter();
						ProcessObject processObject = ProcessObject.getNewInstance();
						ProcessObject.set(processObject);
						ProcessObject.get().setRequestDto(requestDto);
						ProcessObject.get().setCode(ErrorCode.TIMEOUT);
						String responseBody = ProcessObject.get().getErrorResponseBody();
						responseWriter.print(responseBody);
						responseWriter.flush();
						log.info(String.format("Response timeout %s", responseBody));
					} catch(Exception e) {
						log.error(e);
					}*/
				}
			});
		} catch(Exception e) {
			log.error(e);
		}
		
		log.info("Servlet thread released");
	}
	
	private void signResponse(ProcessObject processObject, ResponseBaseDto responseDto) throws IllegalArgumentException, IllegalAccessException {
		// Add client response signature
		if (Boolean.parseBoolean(Config.getValue(processObject.geteDemoName() + "_IsUsingSignatureResponse"))) {
			BaseResult result = responseDto.getResult();
			Class<?> classResult = result.getClass();
			StringBuilder sbSignatureData = responseDto.getDataForSignatureBase();
			Field[] superFields = classResult.getSuperclass().getDeclaredFields();
			for(Field field : superFields) {
				field.setAccessible(true);
				String value = (String)field.get(result);
				if (value == null) {
					value = "";
				}
				sbSignatureData.append(value);
			}
			Field[] fields = classResult.getDeclaredFields();
			for(Field field : fields) {
				field.setAccessible(true);
				String value = (String)field.get(result);
				if (value == null) {
					value = "";
				}
				sbSignatureData.append(value);
			}
			sbSignatureData.append(responseDto.getEndStringForSignature());
			String clearData = sbSignatureData.toString();
			log.debug(String.format("RequestId %s - Response - Valid clear text to sign: %s",
					responseDto.getHeaderDto().getRequestId(), clearData));
			PrivateKey privateKey = RsaSignature.getPrivateKey(processObject.geteDemoName());
			String serverCipherData = RsaSignature.sign(clearData, privateKey);
			responseDto.setSignature(serverCipherData);
		}
		// End
	}
	
	/*private RequestBaseDto preProcess(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType(ParamConstant.JSON_CONTENT_TYPE);
		
		RequestBaseDto requestDto = null;
		String requestUri = req.getRequestURI();
		Cache literalCache = (Cache) CacheManager.getInstance().getCache(CacheName.LITERAL_CLASS_CACHE_NAME);
		Object tClass = literalCache.get(requestUri);
		if (tClass != null) {
			Object tObject = (HttpUtil.of(req.getReader())).toObject((Class<?>) tClass);
			requestDto = (RequestBaseDto) tObject;
		}
		
		return requestDto;
	}*/
	
	private RequestBaseDto preProcess(HttpUtil httpUtil, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType(ParamConstant.JSON_CONTENT_TYPE);
		
		RequestBaseDto requestDto = null;
		String requestUri = req.getRequestURI();
		Cache literalCache = (Cache) CacheManager.getInstance().getCache(CacheName.LITERAL_CLASS_CACHE_NAME);
		Object tClass = literalCache.get(requestUri);
		if (tClass != null) {
			Object tObject = httpUtil.toObject((Class<?>) tClass);
			requestDto = (RequestBaseDto) tObject;
		}
		
		return requestDto;
	}
	
	protected abstract ResponseBaseDto execute(RequestBaseDto requestBaseDto);
}
