package experiments;

import graphdb.extractors.miners.text.TextExtractor;
import graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import searcher.api.ApiLocator;
import searcher.api.SubGraph;
import searcher.api.expand.ExpandFlossNodes;
import searcher.index.LuceneSearchResult;
import webapp.SnowGraphContext;
import webapp.SnowView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = SnowView.class)
public class TestDocRank {


    @Autowired
    private SnowGraphContext context;

    @Test
    public void test() throws IOException {
        Map<Long,Long> q2a=new HashMap<>();
        Map<Long,String> qMap=new HashMap<>();
        Session session=context.getNeo4jBoltDriver().session();
        StatementResult result=session.run("match (q:"+ StackOverflowExtractor.QUESTION+")-[:"+StackOverflowExtractor.HAVE_ANSWER+"]->(a:"+StackOverflowExtractor.ANSWER
                        +") where a."+StackOverflowExtractor.ANSWER_ACCEPTED+"=true return q."+ TextExtractor.TITLE+", id(q), id(a)");
        while (result.hasNext()){
            Record record=result.next();
            long qId=record.get("id(q)").asLong();
            long aId=record.get("id(a)").asLong();
            String query=record.get("q."+TextExtractor.TITLE).asString();
            q2a.put(qId,aId);
            qMap.put(qId,query);
        }
        session.close();
        int i=0;
        FileUtils.deleteDirectory(new File("D:/test/snow"));
        int count=0;
        for (long qId:qMap.keySet()){
            System.out.println(count+"/"+qMap.size());
            count++;
            String query=qMap.get(qId);
            SubGraph searchResult1= ApiLocator.query(query,context.getApiLocatorContext(),false);
            List<LuceneSearchResult> luceneSearchResults=context.getApiLocatorContext().getLuceneSearcher().query(query,true);
            List<DocUnit> originDocUnitList=constructList(luceneSearchResults,q2a.get(qId));
            luceneSearchResults.sort((LuceneSearchResult a, LuceneSearchResult b)->{
                Double aDist=new Double(ExpandFlossNodes.dist(a.nodeSet,searchResult1.getNodes(),context.getApiLocatorContext()));
                Double bDist=new Double(ExpandFlossNodes.dist(b.nodeSet,searchResult1.getNodes(),context.getApiLocatorContext()));
                return aDist.compareTo(bDist);
            });
            List<DocUnit> updatedDocUnitList=constructList(luceneSearchResults,q2a.get(qId));
            int rank0=100,rank1=100;
            for (DocUnit docUnit:originDocUnitList)
                if (docUnit.highlight)
                    rank0=docUnit.rank;
            for (DocUnit docUnit:updatedDocUnitList)
                if (docUnit.highlight)
                    rank1=docUnit.rank;
            if (rank1<rank0){
                File f=new File("D:/test/snow/"+i+".txt");
                FileUtils.write(f,query+"\r\n",true);
                for (int j=0;j<originDocUnitList.size();j++){

                    FileUtils.write(f,originDocUnitList.get(j).rank+"\r\n",true);
                    FileUtils.write(f,originDocUnitList.get(j).highlight+"\r\n",true);
                    FileUtils.write(f,originDocUnitList.get(j).title+"\r\n",true);
                    String content=originDocUnitList.get(j).content.replace("\r","").replace("\n","#!wsadabab!#");
                    FileUtils.write(f,content+"\r\n",true);

                    FileUtils.write(f,updatedDocUnitList.get(j).rank+"\r\n",true);
                    FileUtils.write(f,updatedDocUnitList.get(j).highlight+"\r\n",true);
                    FileUtils.write(f,updatedDocUnitList.get(j).title+"\r\n",true);
                    content=updatedDocUnitList.get(j).content.replace("\r","").replace("\n","#!wsadabab!#");
                    FileUtils.write(f,content+"\r\n",true);

                }
                i++;
            }
        }
    }

    private List<DocUnit> constructList(List<LuceneSearchResult> luceneSearchResults, long aId){
        List<DocUnit> r=new ArrayList<>();
        for (int i=0;i<20&&i<luceneSearchResults.size();i++){
            LuceneSearchResult luceneSearchResult=luceneSearchResults.get(i);
            DocUnit docUnit=new DocUnit();
            docUnit.rank=i+1;
            docUnit.title= Jsoup.parse("<html>"+luceneSearchResult.content+"</html>").text();
            docUnit.title= docUnit.title.length()>100?docUnit.title.substring(0,100)+"...":docUnit.title;
            docUnit.content=luceneSearchResult.content;
            docUnit.highlight=(luceneSearchResult.id==aId);
            r.add(docUnit);
        }
        return r;
    }

    class DocUnit{
        String title,content;
        int rank;
        boolean highlight;
    }

}
