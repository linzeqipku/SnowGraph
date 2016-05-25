package crawlers.issuetracker.util;

import crawlers.issuetracker.threadpool.ThreadPool;

/*
 * Report thread pool's status after a collections of tasks are executed.
 */
public class ThreadPoolStatusReporter {
	public static void report(){
		String statisticStatus = getStatisticStatus();
		System.out.println(statisticStatus);
	}
	
	private static String getStatisticStatus(){
		StringBuilder statisticStatusBuilder = new StringBuilder();
		
		int totalTaskNum = ThreadPool.getTotalTaskNum();
		int succeededTaskNum = ThreadPool.getSucceededTaskNum();
		int failedTaskNum = totalTaskNum - succeededTaskNum;
		
		long usageTimeInMs = ThreadPool.getUsageTimeInMs();
		long mins = usageTimeInMs/1000/60;
		long seconds =(usageTimeInMs%(1000*60))/1000;
		
		statisticStatusBuilder.append("Task Report\n")
							  .append("Total Task num:").append(totalTaskNum).append("\n")
							  .append("Succeeded Task Num").append(succeededTaskNum).append("\n")
							  .append("Failed Task Num:").append(failedTaskNum).append("\n")
							  .append("\n\n")
							  .append("Usage Time:").append(mins).append(" minutes ").append(seconds).append(" seconds.\n");
		
		return statisticStatusBuilder.toString();
	}
	
	public static void main(String[] args) {
		report();
	}
	
}
