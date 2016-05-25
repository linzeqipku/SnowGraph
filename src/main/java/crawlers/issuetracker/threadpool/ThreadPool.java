package crawlers.issuetracker.threadpool;

import utils.Config;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/*
 * A thread pool to execute A KIND OF tasks concurrently.
 * 
 * Note:
 * 		In essence, the thread pool can run any tasks concurrently, 
 * 		but for statistic, the thread pool is designed to run a same kind of tasks.
 * 		For different kinds of tasks, we can run them sequentially. 
 */
public class ThreadPool {
	private static int threadPoolSize = Config.getIntValue("threadpoolsize",20);
	private static ExecutorService executors = null;
	
	/*fields for statistics*/
	private static int totalTaskNum = 0;
	private static int succeededTaskNum = 0;
	private static long usageTimeInMs = 0;
	
	/*
	 * Initialize a fixed size thread pool
	 */
	static{
		executors = Executors.newFixedThreadPool(threadPoolSize);
	}
	
	/*
	 * Set thread pool size
	 */
	public static void setThreadPoolSize(int _threadPoolSize){
		threadPoolSize = _threadPoolSize;
	}

	/*
	 * Get the number of all tasks
	 */
	public static int getTotalTaskNum(){
		return totalTaskNum;
	}
	
	/*
	 * Get the number of succeeded tasks
	 */
	public static int getSucceededTaskNum(){
		return succeededTaskNum;
	}
	
	/*
	 * Get the usage time of executing all tasks in ms
	 */
	public static long getUsageTimeInMs(){
		return usageTimeInMs;
	}
	
	/*
	 * Run given tasks concurrently, 
	 * then return the results of those succeeded tasks.
	 * 
	 * @param tasks: concurrent tasks to be executed
	 * @return: the list of results of succeeded tasks
	 */
	public static <T> List<T> execute(Collection<? extends Callable<T>> tasks){
		if(tasks == null){
			return Collections.emptyList();
		}
		
		totalTaskNum = tasks.size();
		succeededTaskNum = 0;
		long beginTime = System.currentTimeMillis();
		
		List<Future<T>> futures = null;
		try {
			futures = executors.invokeAll(tasks);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if(futures == null){
			return Collections.emptyList();
		}
		
		//collect results of succeeded tasks
		List<T> resultList = new ArrayList<T>();
		for(Future<T> future: futures){
			try {
				T result = future.get();
				resultList.add(result);//add succeeded task result
				succeededTaskNum++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		usageTimeInMs = System.currentTimeMillis() - beginTime;		
		return resultList;
	}	
}