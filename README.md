# Software Knowledge Graph

![licence](https://img.shields.io/badge/license-Apache2.0-blue.svg)

SnowGraph is a software data analytics platform.

## Features

* Data format support

  * Source code, version control, issue tracking, mailing lists, documentation, online discussions.

* Trace recovery

  * Entities extracted from different data sources are linked automatically through multiple traceability recovery techniques.
  
* Data analytics
  
  * Software data are stored in a neo4j graph database.
  * We can use Neo4j's graph query language, Cypher, to query software data.
  * Different data analytics methods can be implemented as a reusable component using a uniform interface.
  
* In-dev Applications
  * Natural language interface for querying the graph database
  * API-aware text understanding
  * QAbot based on text analysis
  * QAbot based on program analysis
    
## Development Environment

* ![java](https://img.shields.io/badge/java->=1.8.0-blue.svg)

* ![maven](https://img.shields.io/badge/maven->=3.2.0-blue.svg)

* ![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ->=2017.3-blue.svg)

* ![neo4j](https://img.shields.io/badge/neo4j->=3.2.0-blue.svg)
    
## Building a Graph

Example input data:

* [Lucene-data.zip](http://pan.baidu.com/s/1gfF4PZt)

edit snowgraph-builder.yml, like:

```
graphPath: E:/SnowGraphData/lucene/graphdb-tmp

extractors:
    - graphdb.extractors.parsers.javacode.JavaCodeExtractor E:/SnowGraphData/lucene/sourcecode
    - graphdb.extractors.parsers.git.GitExtractor E:/SnowGraphData/lucene/git
    - graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor E:/SnowGraphData/lucene/stackoverflow
    - graphdb.extractors.parsers.jira.JiraExtractor E:/SnowGraphData/lucene/jira
    - graphdb.extractors.parsers.mail.MailListExtractor E:/SnowGraphData/lucene/mbox
    - graphdb.extractors.miners.text.TextExtractor
    - graphdb.extractors.miners.codeembedding.line.LINEExtractor
    - graphdb.extractors.linkers.apimention.ApiMentionExtractor
    - graphdb.extractors.linkers.ref.ReferenceExtractor
```

Run ```graphdb.framework.SnowGraphBuilder``` (VM arguments: ```-Xms2000m -Xmx2000m```).
This process may take a long time.

With the above property file, the graph database is generated in ```E:/SnowGraphData/lucene/graphdb-tmp```.
