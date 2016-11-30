package pfr.plugins.parsers.commits;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import pfr.annotations.ConceptDeclaration;
import pfr.annotations.PropertyDeclaration;
import pfr.annotations.RelationDeclaration;


public class PfrPluginForCommits {
    @ConceptDeclaration
    public static final String COMMIT = "commit";
    @PropertyDeclaration(parent = COMMIT)
    public static final String CONTENT = "content";
    @PropertyDeclaration(parent = COMMIT)
    public static final String SHA = "sha";

    @RelationDeclaration
    public static final String PARENT_TO = "parentTo";

    public String path;
    public Commits commits;

    public void setPath(String p) throws Exception {
        path = p;
        commits = new Commits();
        commits.getCommits(parseFile(new File(p)));
    }


    public void getCommitsFromGit(String url, String outFileName) throws Exception {
        Document doc;
        int num = 0;
        File outFile = new File(outFileName);
        FileWriter writer = new FileWriter(outFile);
        while (true) {
            num++;
            doc = Jsoup.connect(url + "?page=" + num).
                    ignoreContentType(true).timeout(10000).get();
            Thread.sleep((long) (3000 + Math.random() * 1000));

            String pre = doc.body().text();

            writer.write(pre + "\n");
            if (pre.length() < 10) break;
        }
        writer.close();
    }

    public void parseFile(File inFile, File outFile) throws Exception {
        FileWriter writer = new FileWriter(outFile);
        List<String> list = parseFile(inFile);
        for (String line : list) {
            writer.write(line + "\n");
        }
        writer.close();
    }

    public List parseFile(File inFile) throws Exception {
        List<String> ans = new ArrayList();
        BufferedReader reader = new BufferedReader(new FileReader(inFile));
        String line = reader.readLine();

        while (line != null) {
            int begin = 1;
            int end = 1;
            while (true) {
                end = line.indexOf("]}") + 2;
                if (end == 1) break;
                String commit = line.substring(begin, end);
                line = line.substring(end + 1);
                begin = 0;
                ans.add(commit);
            }
            line = reader.readLine();
        }
        reader.close();
        return ans;
    }

    public void runTry(File dbFile, Commits commits) {
        if (dbFile.exists()) {
            dbFile.delete();
        }
        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
        try (Transaction tx = db.beginTx()) {
            for (Commit commit : commits.commits) {
                commit.setNode(db.createNode());
                commit.node.addLabel(Label.label(COMMIT));
                commit.node.setProperty(CONTENT, commit.content);
                commit.node.setProperty(SHA, commit.sha);
            }
            Linker linker = new Linker(commits);
            linker.getParentLink();
            for (Entry<Commit, Commit> entry : linker.parentMap.entrySet()) {
                entry.getKey().node.createRelationshipTo(entry.getValue().node, RelationshipType.withName(PARENT_TO));
            }
            tx.success();
        }
        db.shutdown();
    }

    public static void main(String args[]) throws Exception {
        PfrPluginForCommits test = new PfrPluginForCommits();
        //获取commits
        //test.getCommitsFromGit("https://api.github.com/repos/apache/lucenenet/commits", "C:\\Users\\Liwp\\Desktop\\知识库\\test\\out.txt");
        //解析commits文件
        //test.parseFile(new File("C:\\Users\\Liwp\\Desktop\\知识库\\test\\out.txt"), new File("C:\\Users\\Liwp\\Desktop\\知识库\\test\\commits.txt"));
        //解析得到commits列表
        Commits c = new Commits();
        c.getCommits(test.parseFile(new File("C:\\Users\\Liwp\\Desktop\\知识库\\test\\out.txt")));
        test.runTry(new File("C:\\Users\\Liwp\\Desktop\\知识库\\test\\try"), c);

    }

    public void run(GraphDatabaseService db) {
        try (Transaction tx = db.beginTx()) {
            for (Commit commit : commits.commits) {
                commit.setNode(db.createNode());
                commit.node.addLabel(Label.label(COMMIT));
                commit.node.setProperty(CONTENT, commit.content);
                commit.node.setProperty(SHA, commit.sha);
            }
            Linker linker = new Linker(commits);
            linker.getParentLink();
            for (Entry<Commit, Commit> entry : linker.parentMap.entrySet()) {
                entry.getKey().node.createRelationshipTo(entry.getValue().node, RelationshipType.withName(PARENT_TO));
            }
            tx.success();
        }
        db.shutdown();
    }

}

class Commits {
    public List<Commit> commits;

    public void getCommits(List<String> list) {
        commits = new ArrayList();
        for (String one : list) {
            Commit tmp = new Commit();
            tmp.content = one;
            tmp.sha = one.substring(8, 48);
            int index = one.indexOf("\"parents\":[");
            if (index != -1) {
                one = one.substring(index);
                Pattern p = Pattern.compile("([a-z0-9]{40})");
                Matcher m = p.matcher(one);
                while (m.find()) {
                    if (tmp.parents.contains(m.group(0))) {
                        continue;
                    }
                    tmp.parents.add(m.group(0));
                }
                commits.add(tmp);
            } else {
                tmp.parents = null;
                commits.add(tmp);
            }
        }
    }
}

class Commit {
    public String sha;
    public String content;
    public List<String> parents;
    public Node node;

    Commit() {
        sha = null;
        content = null;
        parents = new ArrayList<String>();
        node = null;
    }

    public void setNode(Node n) {
        this.node = n;
    }
}

class Linker {
    public Commits commits;
    public Map<Commit, Commit> parentMap;

    public void getParentLink() {
        parentMap = new IdentityHashMap();
        for (Commit one : commits.commits) {
            if (one.parents == null) continue;
            if (one.parents.size() > 1) {
                System.out.println(one.node.getId());
            }
            for (String parent : one.parents) {
                for (Commit two : commits.commits) {
                    if (two.sha.equals(parent)) {
                        Commit x = new Commit();
                        x.content = one.content;
                        x.sha = one.sha;
                        x.node = one.node;
                        x.parents = one.parents;
                        parentMap.put(x, two);
                        break;
                    }
                }
            }
        }
    }

    Linker(Commits c) {
        commits = c;
    }
}

