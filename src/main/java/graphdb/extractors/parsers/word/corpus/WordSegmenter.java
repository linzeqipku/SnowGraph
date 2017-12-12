package graphdb.extractors.parsers.word.corpus;

import graphdb.extractors.parsers.word.document.DocumentParser;
import graphdb.extractors.parsers.word.entity.utils.DocumentInfo;
import graphdb.extractors.parsers.word.entity.word.WordDocumentInfo;
import org.ansj.domain.Result;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by maxkibble on 2017/7/13.
 */

public class WordSegmenter {
    private static String[] stopTokens = {"，","。","“","”","","【","】","\n", "\t","_","；","、","：","[","]","-","."," ", "(",")",";",","};
    private static HashSet<String> wordsCN = new HashSet<>();

    public static ArrayList<String> demo(String strToParse) {
        String str = strToParse;
                //"我年纪还轻，阅历不深的时候，我父亲教导过我一句话，我至今还念念不忘。 \n" +
                //"“每逢你想要批评任何人的时候，”他对我说，“你就记住，这个世界上所有的人，并不是个个都有过你拥有的那些优越的条件。”";
        ArrayList<String> ret = new ArrayList<>();
        Result terms = ToAnalysis.parse(str);
        for (int i = 0; i < terms.size(); i++) {
            String words = terms.get(i).getName();// 获取单词
            String nominal = terms.get(i).getNatureStr();// 获取词性
            ret.add(words);
            //System.out.print(words + "\t" + nominal + "\n");
        }
        return ret;
    }

    private static void tokenizeDocxFile(String filePath) {
        File file = new File(filePath);
        DocumentInfo doc = DocumentParser.parseFileToDocumentInfo(file);
        if(doc instanceof WordDocumentInfo) {
            String content = ((WordDocumentInfo) doc).getDocStr();
            Result terms = ToAnalysis.parse(content);
            for (int i = 0; i < terms.size(); i++) {
                String words = terms.get(i).getName();
                boolean filtered = false;
                for(String stopToken : stopTokens)
                    if(words.equals(stopToken)) { filtered = true; break; }
                char firstLetter = words.charAt(0);
                if((firstLetter >= 'A' && firstLetter <= 'Z') ||
                        (firstLetter >= 'a' && firstLetter <= 'z') ||
                        (firstLetter >= '0' && firstLetter <= '9'))
                    filtered = true;
                if(filtered) continue;
                wordsCN.add(words);
            }
        }
        else System.out.println("Not a docx file");
    }

    private static void traverseFolder(String path) {
        File file = new File(path);
        if(!file.exists()) return;
        if(!file.isDirectory()) {
            tokenizeDocxFile(path);
            return;
        }
        File[] files = file.listFiles();
        for(File file2 : files) {
            if(file2.isDirectory())
                traverseFolder(file2.getAbsolutePath());
            else
                tokenizeDocxFile(file2.getAbsolutePath());
        }
    }

    public static void main(String[] args) throws IOException {
        WordSegmenter.demo("邮件").forEach(n->{System.out.println(n);});
        /*
        String rootPath = SnowGraphContext.getProjectDocumentPath();
        traverseFolder(rootPath);
        System.out.println("TOTAL CHINESE TOKENS: " + wordsCN.size());

        StringBuilder toPrint = new StringBuilder();
        for(String str : wordsCN) toPrint.append(str + "\n");
        FileOutputStream fout = new FileOutputStream(SnowGraphContext.getProjectChineseTokenPath());
        fout.write(toPrint.toString().getBytes());
        */
    }

}

