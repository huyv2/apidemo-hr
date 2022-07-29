package hr.cache;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public abstract class CacheBase {
	protected final Logger log = Logger.getLogger(this.getClass());
	
	private String name = "";
	private boolean isEternal = true;
	private boolean isSync = false;
	private long duration = 0; // MiliSecond
	protected ConcurrentHashMap<String, Object> hashMap = new ConcurrentHashMap<String, Object>();
	
	public CacheBase() {
	}
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean getIsEternal() {
		return isEternal;
	}
	public void setIsEternal(boolean isEternal) {
		this.isEternal = isEternal;
	}
	public boolean getIsSync() {
		return isSync;
	}
	public void setIsSync(boolean isSync) {
		this.isSync = isSync;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}


	public boolean remove(String key) {
		boolean isSuccess = true;
		Object result = null;
		
		try {
			result = hashMap.remove(key);
		} catch(Exception e) {
			isSuccess = false;
		}
		
		if (isSuccess && result != null) {
			log.info(String.format("Remove key %s in cache %s successfully", key, getName()));
		} else {
			log.info(String.format("Remove key %s in cache %s failed!!!", key, getName()));
		}
		
		return isSuccess && (result != null);
	}
	public boolean loadAll() {
		// Code here
		return false;
	}
	public ConcurrentHashMap<String, Object> getAll() {
		return hashMap;
	}
	public int getSize() {
		return hashMap.size();
	}
	public boolean isEmpty() {
		return hashMap.isEmpty();
	}
	public void removeAll() {
		hashMap.clear();
	}
}
