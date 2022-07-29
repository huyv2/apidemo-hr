package hr.util;

import hr.cache.Cache;
import hr.cache.CacheManager;
import hr.constant.CacheName;

public class ErrorCodeUtil {
	public static String getErrorCodeMessage(String code) {
		Cache cache = (Cache) CacheManager.getInstance().getCache(CacheName.ERROR_CODE_CACHE_NAME);
		String message = (String) cache.get(code);
		return message;
	}
}
