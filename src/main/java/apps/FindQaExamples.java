package apps;

import searcher.DocSearcher;

public class FindQaExamples {

    public static void main(String[] args) {
        DocSearcher docSearcher = Config.getDocSearcher();
        docSearcher.findExamples();
    }

}
