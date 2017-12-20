package searcher.codepattern;

import de.parsemis.graph.Graph;
import org.apache.http.client.fluent.Request;
import org.json.JSONArray;
import searcher.codepattern.code.mining.Miner;
import searcher.codepattern.code.mining.MiningNode;
import searcher.codepattern.utils.CFGUtil;
import searcher.codepattern.utils.ParseUtil;
import searcher.github.GithubCodeSearcher;
import webapp.SnowGraphContext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CodePatternSearcher {

    public static List<Graph<MiningNode, Integer>> run(String query, SnowGraphContext context) throws IOException {

        List<String> contents=new GithubCodeSearcher(context).search(query, GithubCodeSearcher.RETURN_MODE.CONTENT);

        List<String> methods = contents.stream()
            .map(ParseUtil::parseFileContent)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        return Miner.mine(methods, Miner.createSetting(4, 3));

    }

}
