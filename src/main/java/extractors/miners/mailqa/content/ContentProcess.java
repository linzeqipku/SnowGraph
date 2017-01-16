package extractors.miners.mailqa.content;

import extractors.miners.mailqa.entity.Email;

/**
 * @ClassName: ContentProcess
 * @Description: process of the content
 * @author: left
 * @date: 2013.12.26 9:01:47
 */

public interface ContentProcess {

	public void process(Email e);
}
