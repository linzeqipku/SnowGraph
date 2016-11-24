package pfr.plugins.parsers.mail.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;

public class MailUtil {
	
	private static final String MAIL_REGEX = "[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+";
	private static final Pattern MAIL_PATTERN = Pattern.compile(MAIL_REGEX);
	
	/*
	 * 从包含用户邮件地址和用户名称的字符串中，提取出用户的邮件地址和用户名称。
	 * 比如：
	 * 		输入： "Scott Ganyo <scott.ganyo@eTapestry.com>"
	 *  	输出： <"Scott Ganyo","scott.ganyo@eTapestry.com">  (用户名称和用户邮件地址的值对)
	 * 使用场景： Mbox归档中，From和To字段为包含用户邮件地址和用户名称的字符串。
	 */
	public static Pair<String,String> extractMailNameAndAddress(String mailAddressWithName){
		if(mailAddressWithName == null){
			return null;
		}
		
		mailAddressWithName = mailAddressWithName.replaceAll("<", "");
		mailAddressWithName = mailAddressWithName.replaceAll(">", "");
		
		Matcher matcher = MAIL_PATTERN.matcher(mailAddressWithName);
		if(!matcher.find()){
			System.err.println("Warning: Fail to match an email! Content is:" + mailAddressWithName);
			return null;
		}
		
		String mail = matcher.group();
		//remove mail
		String name = mailAddressWithName.replaceAll(mail, "");
		
		//replace possible " or '
		name = name.replaceAll("\"", "");
		name = name.replaceAll("'", "");
		
		name = name.trim();
		
		//remove possible beginning '(' and ending ')'
		if(name.startsWith("(") && name.endsWith(")")){
			name = name.substring(1,name.length()-1);
		}
		
		name = name.trim();
		
		if(name.isEmpty()){
			name = mail;
		}
		return Pair.of(name, mail);
	}

	/*
	 * 从包含多个用户邮件地址和用户名称的字符串中，不同用户以","分割，提取出所有用户的邮件地址和用户名称。
	 * 
	 * 比如：
	 * 		输入: "java-user@lucene.apache.org" <java-user@lucene.apache.org>, Ahmet Arslan <iorixxx@yahoo.com>
	 * 		输出：[<"java-user@lucene.apache.org","java-user@lucene.apache.org">,
	 *           <"Ahmet Arslan","iorixxx@yahoo.com">]
	 *           
	 */     
	public static List<Pair<String,String>> extractMultiMailNameAndAddress(String multiMailAddressWithName){
		if(multiMailAddressWithName == null){
			return Collections.emptyList();
		}
		
		List<Pair<String,String>> userMailList = new ArrayList<>();
		List<String> mailAddressWithNameList = new ArrayList<>();
		
		int beginIndex = 0;
		int endIndex = 0;
		boolean dotInQuotation = false;
		int len = multiMailAddressWithName.length();
		while(endIndex < len){
			char ch = multiMailAddressWithName.charAt(endIndex);
			if(ch == '"'){
				dotInQuotation = !dotInQuotation;
			}else if(ch == ','){
				if(!dotInQuotation){//引号外的逗号，用户切分处
					String oneMailInfo = multiMailAddressWithName.substring(beginIndex,endIndex);
					mailAddressWithNameList.add(oneMailInfo);
					beginIndex = endIndex + 1;
				}
			}
			endIndex++;
		}
		
		if(beginIndex < len){
			String oneMailInfo = multiMailAddressWithName.substring(beginIndex);
			mailAddressWithNameList.add(oneMailInfo);
		}
		
		for(String mailAddressWithName:mailAddressWithNameList){
			Pair<String,String> mailPair = extractMailNameAndAddress(mailAddressWithName);
			if(mailPair != null){
				userMailList.add(mailPair);
			}
		}
		
		return userMailList;
	}
	
	public static void main(String[] args) {

		extractMultiMailNameAndAddress("\"Lopresti, Alejandro Oscar\" <alejandro.lopresti@us.sema.com>,\"Jean-Marc Bertinchamps\" <jmbertinchamps@edpsa.com>");
		
//		String mailAddressWithName = "";
//		
//		mailAddressWithName = "Scott Ganyo <scott.ganyo@eTapestry.com>";
//		System.out.println(mailAddressWithName);
//		System.out.println(extractMailNameAndAddress(mailAddressWithName).toString());
//		System.out.println("****************************************************");
//		
//		mailAddressWithName = "jay ! <cyberjay10@yahoo.com>";
//		System.out.println(mailAddressWithName);
//		System.out.println(extractMailNameAndAddress(mailAddressWithName).toString());
//		System.out.println("****************************************************");
//		
//		mailAddressWithName = "\"Jean-Marc Bertinchamps\" <jmbertinchamps@edpsa.com>";
//		System.out.println(mailAddressWithName);
//		System.out.println(extractMailNameAndAddress(mailAddressWithName).toString());
//		System.out.println("****************************************************");
//		
//		mailAddressWithName = "\"Lopresti, Alejandro Oscar\" <alejandro.lopresti@us.sema.com>";
//		System.out.println(mailAddressWithName);
//		System.out.println(extractMailNameAndAddress(mailAddressWithName).toString());
//		System.out.println("****************************************************");
//		
//		mailAddressWithName = "\"Sunny Kapoor \\(SunKap\\)\" <sunkap@bizpiranha.com>";
//		System.out.println(mailAddressWithName);
//		System.out.println(extractMailNameAndAddress(mailAddressWithName).toString());
//		System.out.println("****************************************************");
//		
//		mailAddressWithName = "\"W. Eliot Kimber\" <eliot@isogen.com>";
//		System.out.println(mailAddressWithName);
//		System.out.println(extractMailNameAndAddress(mailAddressWithName).toString());
//		System.out.println("****************************************************");
//		
//		mailAddressWithName = "\"Bauer, Herbert S. (Scott)\" <Bauer.Scott@mayo.edu>";
//		System.out.println(mailAddressWithName);
//		System.out.println(extractMailNameAndAddress(mailAddressWithName).toString());
//		System.out.println("****************************************************");
//		
//		mailAddressWithName = "<bh22351@i-one.at>";
//		System.out.println(mailAddressWithName);
//		System.out.println(extractMailNameAndAddress(mailAddressWithName).toString());
//		System.out.println("****************************************************");
//		
//		mailAddressWithName = "nelson@monkey.org (Nelson Minar)";
//		System.out.println(mailAddressWithName);
//		System.out.println(extractMailNameAndAddress(mailAddressWithName).toString());
//		System.out.println("****************************************************");
//		
//		mailAddressWithName = "\"java-user@lucene.apache.org\" <java-user@lucene.apache.org>";
//		System.out.println(mailAddressWithName);
//		System.out.println(extractMailNameAndAddress(mailAddressWithName).toString());
//		System.out.println("****************************************************");
//		
//		mailAddressWithName = "\"'java-user@lucene.apache.org'\" <java-user@lucene.apache.org>";
//		System.out.println(mailAddressWithName);
//		System.out.println(extractMailNameAndAddress(mailAddressWithName).toString());
//		System.out.println("****************************************************");
//		
//		mailAddressWithName = "Sebastian Ho sebastianh@bii.a-star.edu.sg";
//		System.out.println(mailAddressWithName);
//		System.out.println(extractMailNameAndAddress(mailAddressWithName).toString());
//		System.out.println("****************************************************");
		
	}
}
