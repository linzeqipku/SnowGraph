package pfr.plugins.parsers.issuetracker;

import java.util.List;
import java.util.Map;

import pfr.plugins.parsers.issuetracker.entity.IssueInfo;
import pfr.plugins.parsers.issuetracker.entity.IssueUserInfo;

public interface IssueTrackerExtractor
{
	public List<IssueInfo> extract();

	public Map<String, IssueUserInfo> getUserMap();
}
