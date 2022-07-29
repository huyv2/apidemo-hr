package hr.factory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import hr.config.Config;
import hr.constant.ParamConstant;

public class ExecutorFactory {
	private static final Logger log = Logger.getLogger(ExecutorFactory.class);
	
	private static int corePoolSize;
	private static int maxPoolSize;
	private static byte boundQueue;
	private static ThreadPoolExecutor executor = null;
	
	public static void init() {
		try {
			corePoolSize = Integer.parseInt(Config.getValue("corePoolSize"));
			maxPoolSize = Integer.parseInt(Config.getValue("maxPoolSize"));
			boundQueue = Byte.parseByte(Config.getValue("boundQueue"));
		} catch(Exception e) {
			corePoolSize = 1;
			maxPoolSize = 1;
			boundQueue = ParamConstant.IS_MAX_TASK_QUEUE;
		}
		
		try {
			if (boundQueue == ParamConstant.IS_MAX_TASK_QUEUE) {
				executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.SECONDS,
						new LinkedBlockingQueue<Runnable>());
			} else {
				executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.SECONDS,
						new LinkedBlockingQueue<Runnable>(boundQueue));
			}
			log.info("Init Factory ExecutorThreadPool successfully");
		} catch(Exception e) {
			log.error(e);
		}
	}
	
	public static ExecutorService getThreadPoolExecutor() {
		return executor;
	}
	
	public static void shutdown() {
		try {
			log.info("Prepare sutting down ThreadPool");
			
			executor.shutdown();
			
			log.info("Please wait for all taks complete");
			while(!executor.awaitTermination(10, TimeUnit.SECONDS)) {
				log.info("Awaiting completation of tasks!");
			}
			log.info("All tasks complete!");
			
			log.info("Shutdown ThreadPool successfully");
		} catch(Exception e) {
			log.error("Shutdown error ", e);
		}
	}
}
