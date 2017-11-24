package apps;

import searcher.ir.LuceneSearcher;

import java.io.IOException;

class BuildLuceneIndex {

    public static void main(String[] args) {
        try {
            new LuceneSearcher().index(true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
