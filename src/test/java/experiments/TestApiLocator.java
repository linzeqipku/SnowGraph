package experiments;

import experiments.entity.ApiLocatorResult;
import experiments.entity.TestDataItem;
import experiments.entity.TestDataSet;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import searcher.api.ApiLocator;
import webapp.SnowGraphContext;
import webapp.SnowView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = SnowView.class)
public class TestApiLocator {

    @Autowired
    private SnowGraphContext context;

    @Test
    public void test() throws IOException, ParseException {

        String optDir=context.getDataDir()+"/test_api_locator";

        TestDataSet testDataSet=new Yaml().loadAs(new FileInputStream(new File(SnowGraphContext.class.getResource("/").getPath()+"lucene-test.yml")),TestDataSet.class);

        FileUtils.deleteDirectory(new File(optDir));

        for (TestDataItem testDataItem:testDataSet.getItems()){
            ApiLocatorResult apiLocatorResult=new ApiLocatorResult();
            apiLocatorResult.setItem(testDataItem);
            for (long id: ApiLocator.query(testDataItem.getQuery(),context.getApiLocatorContext(),false).getNodes()){
                String sig=context.getApiLocatorContext().getId2Sig().get(id);
                apiLocatorResult.add(sig);
            }
            String itemDir=optDir+"/"+testDataItem.getQuery().replace(" ","_")+"/";
            DumperOptions options=new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setSplitLines(false);
            FileUtils.write(new File(itemDir+"/apiLocation.txt"),new Yaml(options).dump(apiLocatorResult));
            /*
            List<Graph<MiningNode, Integer>> graphs= CodePatternSearcher.run(testDataItem.getQuery(),context);
            for (int i=0;i<graphs.size();i++){
                Graph<MiningNode, Integer> graph=graphs.get(i);
                File file=new File(itemDir+"/"+(i+1)+".txt");
                FileUtils.write(file,"");
                CFGUtil.printGraph(graph,new PrintStream(file));
            }
            */
        }

    }

}
