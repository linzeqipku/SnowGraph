package searcher.doc.example;

import org.apache.commons.io.FileUtils;
import rest.resource.SampleQuestion;
import searcher.SnowGraphContext;
import searcher.doc.DocSearcher;

import java.io.*;
import java.util.*;

public class StackOverflowExamples {

    private List<SampleQuestion> list=new ArrayList<>();

    /**
     * 寻找重排序后效果好的StackOverflow问答对作为例子
     */
    public static void find() {

        String qaExamplePath=SnowGraphContext.getDataPath()+"/qaexamples";

        try {
            FileUtils.write(new File(qaExamplePath), "");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        int count = 0, irCount = 0;
        int qCnt = 0;
        for (long queryId : SnowGraphContext.getDocSearcherContext().getStackOverflowQuestionIds()) {
            qCnt++;
            List<DocSearcher.DocSearchResult> list = DocSearcher.search(SnowGraphContext.getDocSearcherContext().getQuery(queryId),SnowGraphContext.getDocSearcherContext());
            if (list.size() < 20)
                continue;
            for (int i = 0; i < 20; i++) {
                DocSearcher.DocSearchResult current = list.get(i);
                if (current.getId() == SnowGraphContext.getDocSearcherContext().getAnswerId(queryId)) {
                    irCount++;
                    //System.out.println(current.newRank+" "+current.irRank);
                    if (current.getNewRank() < current.getIrRank()) {
                        String res = count + " " + queryId + " " + current.getId() + " "
                                + current.getIrRank() + "-->" + current.getNewRank();
                        System.out.println(res + " (" + qCnt + ")");
                        try {
                            FileUtils.write(new File(qaExamplePath), res + "\n", true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        count++;
                    }
                }
            }
            //System.out.println("query count: " + qCnt + " " + qCnt * 1.0 / qSize * 100 + "%");
        }
        System.out.println("irCount: " + irCount);
    }

    public SampleQuestion getRandomExampleQuery(){
        if (list.size()==0)
            return new SampleQuestion("");
        return list.get(new Random().nextInt(list.size()));
    }

    public StackOverflowExamples(){

        String exampleFilePath=SnowGraphContext.getDataPath()+"/qaexamples";

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(exampleFilePath)),
                    "UTF-8"));
            String lineTxt;
            while ((lineTxt = br.readLine()) != null) {
                if (lineTxt.length()==0)
                    continue;
                String[] names = lineTxt.split(" ");
                long id=Long.parseLong(names[1]);
                list.add(new SampleQuestion(SnowGraphContext.getDocSearcherContext().getQuery(id)));
            }
            br.close();
        } catch (Exception e) {
            System.err.println("read errors :" + e);
        }

    }

}
