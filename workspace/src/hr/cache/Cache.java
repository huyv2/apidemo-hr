package hr.cache;

import java.util.Map;

import hr.cache.object.ExpireObject;

public class Cache extends CacheBase {
	public static Cache getNewInstance() {
		return new Cache();
	}
	
	private Cache() {}
	
	/**
	 * Only accept first value with same key
	 * @param key
	 * @param value
	 * @return true: Put successfully or exists key with another value - false: Put failed
	 */
	public boolean put(String key, Object value){
		boolean isSuccess = false;
		Object result = null;
		
		if (value != null) {
			try {
				ExpireObject expireObject = ExpireObject.getNewInstance();
				expireObject.setValueObject(value);
				result = hashMap.putIfAbsent(key, expireObject);
				isSuccess = true;
			} catch(Exception e) {
				isSuccess = false;
			}
		}
		
		if (isSuccess && result == null) {
			log.info(String.format("Put key %s in cache %s successfully", key, getName()));
		} else if (isSuccess) {
			log.info(String.format("Exists key %s in cache %s !!!", key, getName()));
		}
		return isSuccess && (result == null);
	}
	// Accept update latest value with same key
	/**
	 * Accept update latest value with same key
	 * @param key
	 * @param value
	 * @return true: Put or update successfully with key and value - false: Put or update failed
	 */
	public boolean putOrUpdate(String key, Object value){
		boolean isSuccess = false;
		Object result = null;
		
		if (value != null) {
			try {
				ExpireObject expireObject = ExpireObject.getNewInstance();
				expireObject.setValueObject(value);
				result = hashMap.put(key, expireObject);
				isSuccess = true;
			} catch(Exception e) {
				isSuccess = false;
			}
		}
		
		if (isSuccess && result == null) {
			log.info(String.format("Put key %s in cache %s successfully", key, getName()));
		} else if (isSuccess) {
			log.info(String.format("Updated key %s in cache %s successfully", key, getName()));
		}
		return isSuccess;
	}
	public Object get(String key) {
		Object value = null;
		try {
			ExpireObject expireObject = (ExpireObject) hashMap.get(key);
			if (expireObject != null) {
				value = expireObject.getValueObject();
			}
		} catch(Exception e) {
			value = null;
		}
		return value;
	}
	public void putAllStr(Map<String, String> data) {
		try {
			for(Map.Entry<String, String> entry : data.entrySet()) {
				put(entry.getKey(), entry.getValue());
			}
		} catch(Exception e) {
			log.error(e);
		}
	}
}
