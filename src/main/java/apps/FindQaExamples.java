package apps;

import searcher.SnowGraphContext;
import searcher.doc.example.StackOverflowExamples;

class FindQaExamples {

    public static void main(String[] args) {
        SnowGraphContext.init();
        StackOverflowExamples.find();
    }

}
