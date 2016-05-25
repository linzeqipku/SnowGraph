package crawlers.issuetracker.exception;

/*
 * Thrown when crawling task fails.
 */
public class CrawlFailedException extends Exception{
	private static final long serialVersionUID = 1L;

	/*
	 * Constructs a {@Code CrawlFailedException} with no detail message.
	 */
	public CrawlFailedException(){
		super();
	}
	
	/*
	 * Constructs a {@Code CrawlFailedException} with the specific detail message
	 * 
	 * @param message: the detail message
	 */
	public CrawlFailedException(String message){
		super(message);
	}

}
