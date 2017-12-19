package searcher.codepattern;

import de.parsemis.graph.Graph;
import org.apache.http.client.fluent.Request;
import org.json.JSONArray;
import searcher.codepattern.code.mining.Miner;
import searcher.codepattern.code.mining.MiningNode;
import searcher.codepattern.utils.CFGUtil;
import searcher.codepattern.utils.ParseUtil;

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

public class Test {
    public static <T> T getResultFromFuture(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        String result = Request.Get("http://162.105.88.28:8080/searchGithub?query=create+index").execute().returnContent().asString();
        ExecutorService pool = Executors.newFixedThreadPool(100);

        JSONArray arr = new JSONArray(result);

        List<Future<String>> futures = StreamSupport.stream(arr.spliterator(), true)
            .map(x -> (String) x)
            .map(x -> pool.submit(() -> Request.Get(x).execute().returnContent().asString()))
            .collect(Collectors.toList());

        List<String> methods = futures.stream()
            .map(Test::getResultFromFuture)
            .map(ParseUtil::parseFileContent)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        System.out.println(methods.size());

        List<Graph<MiningNode, Integer>> results = Miner.mine(methods, Miner.createSetting(4, 3));
        try {
            PrintStream ps = new PrintStream("out/result-ddg.txt");
            ps.println(results.size());
            for (Graph<MiningNode, Integer> graph : results) {
                CFGUtil.printGraph(graph, ps);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        pool.shutdown();
    }

}
