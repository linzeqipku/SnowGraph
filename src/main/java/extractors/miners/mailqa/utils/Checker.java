package extractors.miners.mailqa.utils;

import org.apache.log4j.Logger;

import extractors.miners.mailqa.entity.Email;
import extractors.miners.mailqa.entity.Session;

public class Checker {

	private static Logger	logger	= Logger.getLogger(Checker.class.getName());

	private static void printErrorMessage(boolean isMessage, String info) {
		if (isMessage) {
			logger.error("the insert email 's " + info + " is null");
		}
		else {
			logger.error("the insert session's " + info + " is null");
		}
	}

	public static boolean isGoodMessage(Email email) {

		boolean isMessage = true;
		String info = "";
		if (email == null) {
			info = "email";
			logger.error("email is null");
			return false;
		}
		if (email.getMessageID() == null) {
			info = "message id";
			logger.error("message id is null");
			return false;
		}
		if (email.getProjectID() < 1 || email.getProjectID() > 2) {
			// info = "project id";
			// return false;
		}
		if (email.getSubject() == null || email.getSubject().trim().length() == 0) {
			info = "subject";
			logger.error("subject is null");
			return false;
		}
		if (email.getContent() == null || email.getContent().trim().length() == 0) {
			logger.error("content is null");
			return false;
		}
		if (email.getFromEmail() == null || email.getFromEmail().trim().length() == 0) {
			info = "from email address";
			logger.error("from email address is null");
			return false;
		}
		if (email.getToEmail() == null || email.getToEmail().trim().length() == 0) {
			info = "to email address";
			logger.error("to email address is null");
			// isGood = false;
		}
		// if(email.getKeyWords() == null) {
		// info = "keyword";
		// isGood = false;
		// }
		if (email.getSendDate() == null) {
			info = "send date";
			// isGood = false;
		}
		return true;
	}

	public static boolean isGoodSession(Session session) {
		boolean isMessage = false;
		boolean isGood = true;
		String info = "";
		if (session == null) {
			info = "session";
			isGood = false;
		}
		else if (session.getProjectID() < 1 || session.getProjectID() > 2) {
			info = "project id";
			isGood = false;
		}
		//else if (session.getSessionID() == null) {
		//	info = "session id";
		//	isGood = false;
		//}
		else if (session.getStartTime() == null) {
			info = "start time";
			isGood = false;
		}
		else if (session.getEndTime() == null) {
			info = "end time";
			isGood = false;
		}
		else if (session.getParticipants() == null) {
			info = "participants";
			isGood = false;
		}
		else if (session.getPromoterEmail() == null) {
			info = "promoter email address";
			isGood = false;
		}
		else if (session.getSubject() == null) {
			info = "subject";
			isGood = false;
		}
		else if (session.getMsgList() == null) {
			info = "msg list";
			isGood = false;
		}
		if (!isGood) {
			printErrorMessage(isMessage, info);
		}
		return isGood;
	}
}
