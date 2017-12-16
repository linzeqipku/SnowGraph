# Software Knowledge Graph

![licence](https://img.shields.io/badge/license-Apache2.0-blue.svg)

SnowGraph is a data analysis and knowledge mining toolkit for software engineering researchers.

## Features

* Data analytics

  * From multi-source and heterogeneous software engineering data (e.g., source code, version control, documentation, mailing list, issue tracker and online forum) to a uniform graph
    
    * Entities: classes, methods, commits, document sections, users, emails, issue reports, forum posts, ...
    
    * Traces (relations): inheritance, method_invocation, have_document_section, code_element_mentioned_in, ...
    
  * Graph query language and graphical browsing interface
  
  * High extendability for various data types

* Knowledge mining

  * Extract knowledge from the graph
  
    * API usage examples, API semantic representations, business topics, domain ontologies, ...
    
  * High extendability for various knowledge mining algorithms

* Question answering

  * Graph-based question answering

    * Question example: "Which developer answered the most emails about class RAMDirectory?"
  
  * Text-based question answering

    * Find passages from the graph to answer questions
    
    * Question example: "How to get all field names in an IndexReader?"
    
## Dependencies

* Programming language
 
  ![java](https://img.shields.io/badge/java->=1.8.0-blue.svg)
  
* Project management
 
  ![maven](https://img.shields.io/badge/maven->=3.2.0-blue.svg)
  
* Graph database engine
 
  ![neo4j](https://img.shields.io/badge/neo4j->=3.2.0-blue.svg)
    
## Building a Graph

Example input data:

* [Lucene-data.zip](http://pan.baidu.com/s/1gfF4PZt)

Write a spring bean property xml file, like this:

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="graph" class="graphdb.framework.GraphBuilder">
    	<property name="graphPath" value="E:/SnowGraphData/lucene/graphdb-base"/>
        <property name="extractors">
            <list>
                <ref bean="codegraph" />
                <ref bean="sograph" />
                <ref bean="mailgraph" />
                <ref bean="issuegraph" />
                <ref bean="gitgraph" />
                <ref bean="line" />
                <ref bean="text" />
                <ref bean="apimention" />
                <ref bean="reference" />
            </list>
        </property>
    </bean>
    <bean id="codegraph" class="graphdb.extractors.parsers.javacode.JavaCodeExtractor">
        <property name="srcPath" value="E:/SnowGraphData/lucene/sourcecode" />
    </bean>
    <bean id="gitgraph" class="graphdb.extractors.parsers.git.GitExtractor">
        <property name="gitFolderPath" value="E:/SnowGraphData/lucene/git" />
    </bean>
    <bean id="sograph" class="graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor">
        <property name="folderPath" value="E:/SnowGraphData/lucene/stackoverflow" />
    </bean>
    <bean id="issuegraph" class="graphdb.extractors.parsers.jira.JiraExtractor">
        <property name="issueFolderPath" value="E:/SnowGraphData/lucene/jira" />
    </bean>
    <bean id="mailgraph" class="graphdb.extractors.parsers.mail.MailListExtractor">
        <property name="mboxPath" value="E:/SnowGraphData/lucene/mbox" />
    </bean>
    <bean id="line" class="graphdb.extractors.miners.codeembedding.line.LINEExtractor" />
    <bean id="text" class="graphdb.extractors.miners.text.TextExtractor" />
    <bean id="apimention" class="graphdb.extractors.linkers.apimention.ApiMentionExtractor" />
    <bean id="reference" class="graphdb.extractors.linkers.ref.ReferenceExtractor" />
</beans>
```

Run ```apps.buildgraph.BuildGraph {property_xml_file_path}``` (need a large memory allocation pool for JVM, for example, set VM arguments to ```-Xms5000m -Xmx5000m```).
This process may take a long time.

With the above property file, the graph database is generated in ```E:/SnowGraphData/lucene/graphdb-base```.
