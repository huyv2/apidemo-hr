package hr.cache;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import hr.cache.constant.TimeUnit;
import hr.cache.object.ExpireObject;
import hr.config.Config;

public class CacheManager extends CacheBase {
	private static CacheManager cacheManager = null;
	private Timer timer = null;
	
	public static CacheManager getInstance() {
		if (cacheManager == null) {
			cacheManager = new CacheManager();
		}
		return cacheManager;
	}
	
	private CacheManager() {
		setName("CacheManager");
		long periodChecking;
		try {
			periodChecking = Long.parseLong(Config.getValue("periodChecking"));
			if (periodChecking > 60000) {
				periodChecking = 60000;
			}
		} catch(Exception e) {
			periodChecking = 59000;
		}
		final int secondCondition = (int) periodChecking/1000;
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				// Timer remove expiring caches
				try {
					for(Map.Entry<String, Object> cacheObjectMap : hashMap.entrySet()) {
						CacheBase cache = (CacheBase)cacheObjectMap.getValue();
						if (!cache.getIsEternal()) {
							ConcurrentHashMap<String, Object> cacheMaps = (ConcurrentHashMap<String, Object>) cache.getAll();
							long now = new Date().getTime();
							long duration = cache.getDuration();
							for(Map.Entry<String, Object> cacheMap : cacheMaps.entrySet()) {
								String key = cacheMap.getKey();
								ExpireObject expireObject = (ExpireObject) cacheMap.getValue();
								if ((now - expireObject.getCreatedDateTime()) > duration) {
									cache.remove(key);
								}
							}
							/*if (cache.isEmpty()) {
								hashMap.remove(cache.getName());
							}*/
						}
						Calendar cNow = Calendar.getInstance();
						if (cNow.get(Calendar.HOUR_OF_DAY) == 1 && cNow.get(Calendar.MINUTE) == 0 &&
								cNow.get(Calendar.SECOND) < secondCondition) {
							if (cache.getIsSync()) {
								if (cache.loadAll()) {
									log.info(String.format("Reload daily %s: %s successfully", cache.getName(), cache.getSize()));
								}
							}
						}
					}
				} catch(Exception e) {
					log.error(e);
				}
			}
			
		}, 0, periodChecking);
	}
	
	public void init() {
		try {
			for(Map.Entry<String, Object> cacheObjectMap : hashMap.entrySet()) {
				CacheBase cache = (CacheBase)cacheObjectMap.getValue();
				if (cache.getIsSync()) {
					if (cache.loadAll()) {
						log.info(String.format("Reload %s: %s successfully", cache.getName(), cache.getSize()));
					}
				}
			}
		} catch(Exception e) {
			log.error(e);
		}
	}
	/**
	 * Create a new eternal cache
	 * @param name: cache's name and it's as key in cache manager
	 * @return Instance cache object which had just been created and has been managed by cache manager
	 */
	public CacheBase createCache(String name) {
		CacheBase cache = null;
		try {
			cache = Cache.getNewInstance();
			cache.setName(name);
			cache.setIsEternal(true);
			cache.setDuration(0);
			putToCacheManager(name, cache);
			log.info(String.format("Created eternal cache with name %s successfully!!!", name));
		} catch(Exception e) {
			log.error(String.format("Cannot create eternal cache with name %s", name));
			cache = null;
		}
		return cache;
		
	}
	/**
	 * Create a new expiring cache
	 * @param name: cache's name and it's as key in cache manager
	 * @param timeUnit: as living time unit
	 * @param duration: as living duration
	 * @return Instance cache object which had just been created and has been managed by cache manager
	 */
	public CacheBase createCache(String name, TimeUnit timeUnit, long duration) {
		CacheBase cache = null;
		try {
			long realDuration = 0;
			switch (timeUnit) {
			case SECOND:
				realDuration = duration * 1000;
				break;
			case MINUTE:
				realDuration = duration * 60000; // duration * 60 * 1000
				break;
			case HOUR:
				realDuration = duration * 3600000; // duration * 60 * 60 * 1000
				break;
			case DAY:
				realDuration = duration * 86400000; // duration * 24 * 60 * 60 * 1000
				break;
			default:
				realDuration = duration;
				break;
			}
			cache = Cache.getNewInstance();
			cache.setName(name);
			cache.setIsEternal(false);
			cache.setDuration(realDuration);
			putToCacheManager(name, cache);
			log.info(String.format("Created expiring cache with name %s in duration %s %ss", name, duration, timeUnit.toString().toLowerCase()));
		} catch(Exception e) {
			log.error(String.format("Cannot create expiring cache with name %s in duration %ss", name, duration));
			cache = null;
		}
		return cache;
	}
	/**
	 * Put a new customization eternal cache to cache manager
	 * @param name: cache's name and it's as key in cache manager
	 * @param cache: Putting customization cache object
	 * @return Instance cache object which had just been created and has been managed by cache manager
	 */
	public CacheBase putCache(String name, Cache cache) {
		try {
			cache.setName(name);
			cache.setIsEternal(true);
			cache.setIsSync(true);
			cache.setDuration(0);
			putToCacheManager(name, cache);
			log.info(String.format("Put eternal cache with name %s successfully!!!", name));
		} catch(Exception e) {
			log.error(String.format("Cannot put eternal cache with name %s", name));
			cache = null;
		}
		return cache;
		
	}
	/**
	 * Put a new customization expiring cache to cache manager
	 * @param name: cache's name and it's as key in cache manager
	 * @param timeUnit: as living time unit
	 * @param duration: as living duration
	 * @param cache: Putting customization cache object
	 * @return Instance cache object which had just been created and has been managed by cache manager
	 */
	public CacheBase putCache(String name, TimeUnit timeUnit, long duration, Cache cache) {
		try {
			long realDuration = 0;
			switch (timeUnit) {
			case SECOND:
				realDuration = duration * 1000;
				break;
			case MINUTE:
				realDuration = duration * 60000; // duration * 60 * 1000
				break;
			case HOUR:
				realDuration = duration * 3600000; // duration * 60 * 60 * 1000
				break;
			case DAY:
				realDuration = duration * 86400000; // duration * 24 * 60 * 60 * 1000
				break;
			default:
				realDuration = duration;
				break;
			}
			cache.setName(name);
			cache.setIsEternal(false);
			cache.setIsSync(false);
			cache.setDuration(realDuration);
			putToCacheManager(name, cache);
			log.info(String.format("Put expiring cache with name %s in duration %s %ss", name, duration, timeUnit.toString().toLowerCase()));
		} catch(Exception e) {
			log.error(String.format("Cannot put expiring cache with name %s in duration %ss", name, duration));
			cache = null;
		}
		return cache;
	}
	private void putToCacheManager(String name, CacheBase cache) {
		hashMap.putIfAbsent(name, cache);
	}
	/**
	 * Get a instance cache object which is being managed by cache manager
	 * @param name: key
	 * @return a instance cache object with key
	 */
	public CacheBase getCache(String name) {
		CacheBase cache = null;
		try {
			cache = (CacheBase) hashMap.get(name);
		} catch(Exception e) {
			log.error(e);
			cache = null;
		}
		return cache;
	}
	
	public void destroy() {
		try {
			if (timer != null) {
				timer.cancel();
				timer.purge();
			}
			for(Map.Entry<String, Object> cacheObjectMap : hashMap.entrySet()) {
				CacheBase cache = (CacheBase)cacheObjectMap.getValue();
				ConcurrentHashMap<String, Object> cacheMaps = (ConcurrentHashMap<String, Object>) cache.getAll();
				for(Map.Entry<String, Object> cacheMap : cacheMaps.entrySet()) {
					String key = cacheMap.getKey();
					cache.remove(key);
				}
				hashMap.remove(cache.getName());
			}
			log.info("Destroyed CacheManager successfully");
		} catch(Exception e) {
			log.error(e);
		}
	}
}
