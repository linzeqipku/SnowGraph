package extractors.miners.mailqa.answer;

import java.io.IOException;
import java.util.ArrayList;

import javassist.bytecode.analysis.Analyzer;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.Version;

import extractors.miners.mailqa.entity.Email;

public class AnswerSelector {

	public Email getAnswerMail(Email qMail, String question, ArrayList<Email> rMailList) {
		// TODO Auto-generated method stub
		
		String queryStr = qMail.getSubject() + "　" + question;

		
		return rMailList.get(rMailList.size() - 1);
//		return null;
	}

}
