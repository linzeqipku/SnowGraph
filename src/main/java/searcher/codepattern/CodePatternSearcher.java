package searcher.codepattern;

import de.parsemis.graph.Graph;
import searcher.codepattern.code.mining.Miner;
import searcher.codepattern.code.mining.MiningNode;
import searcher.codepattern.utils.ParseUtil;
import webapp.SnowGraphContext;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
