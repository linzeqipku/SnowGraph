# SnowGraph

SnowGraph(Software Knowledge Graph)是一个软件知识图谱的构造框架。
给定一个软件项目，SnowGraph能够对其中多源异构的软件数据进行解析与汇聚，并在此基础上对软件项目中的知识进行融合与提炼。
其输入是一个软件项目的各种数据资源，输出是该项目的一个统一的软件知识图谱。

SnowGraph旨在从三个方面对软件数据研究者提供帮助：
- SnowGraph完成了多源异构数据的解析与汇聚，研究者们可以直接使用统一的形式化方法来查询软件数据；
- SnowGraph从数据中识别了不同实体间各种类型的关联关系，能够帮助研究者更好地完成跨数据源的研究工作；
- SnowGraph实现了插件机制，使得知识提炼具有很高的可扩展性与可复用性。

开发者准备
--------------------------
SnowGraph以Java语言编写，在Eclipse中进行开发，通过Maven进行依赖管理。

参与SnowGraph的开发需要做如下环境配置工作：
- 安装Maven3；
- 安装JDK+JRE 1.8或以上版本；
- 在Eclipse上安装并配置m2e插件(maven to Eclipse)；
- Fork出本项目的一个分支，并通过Eclipse自带的git插件将该分支导入到Eclipse的workspace内；
- 设置项目的Java编译与运行环境为1.8。

通过一个示例来快速了解SnowGraph的用法
----------------------------
编写并运行如下代码，可以为一个软件项目构造知识图谱：

    @SuppressWarnings("resource")
    ApplicationContext context=new ClassPathXmlApplicationContext("/a/b/c/d.xml");
    KnowledgeGraphBuilder graphBuilder=(KnowledgeGraphBuilder) context.getBean("graph");
    graphBuilder.buildGraph();

我们需要提供的是一个xml配置文件(上例中为："/a/b/c/d.xml")，用于指定SnowGraph需要解析哪些数据以及需要提炼出哪些知识。以下是一个配置文件的编写示例：

    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://www.springframework.org/schema/beans
            http://www.springframework.org/schema/beans/spring-beans.xsd">
        <bean id="graph" class="pfr.framework.KnowledgeGraphBuilder">
            <property name="graphPath" value="E:/graphdb-jfreechart"/>
            <property name="pfrPlugins">
                <list>
                    <ref bean="codegraph" />
                    <ref bean="sograph" />
                    <ref bean="codelinker" />
                    <ref bean="codeembedding" />
                    <ref bean="tokenizer" />
                </list>
            </property>
        </bean>
        <bean id="codegraph" class="pfr.plugins.parsers.javacode.PfrPluginForJavaCode">
            <property name="srcPath" value="E:/data/jfreechart/src" />
        </bean>
        <bean id="sograph" class="pfr.plugins.parsers.stackoverflow.PfrPluginForStackOverflow">
            <property name="folderPath" value="E:/data/jfreechart/qa" />
        </bean>
        <bean id="codelinker" class="pfr.plugins.refiners.codelinking.PfrPluginForCodeLinking">
            <property name="focusSet">
                <set>
                    <value>pfr.plugins.parsers.stackoverflow.PfrPluginForStackOverflow.QUESTION_BODY</value>
                    <value>pfr.plugins.parsers.stackoverflow.PfrPluginForStackOverflow.QUESTION_TITLE</value>
                    <value>pfr.plugins.parsers.stackoverflow.PfrPluginForStackOverflow.ANSWER_BODY</value>
                    <value>pfr.plugins.parsers.stackoverflow.PfrPluginForStackOverflow.COMMENT_TEXT</value>
                </set>
            </property>
        </bean>
        <bean id="codeembedding" class="pfr.plugins.refiners.codeembedding.PfrPluginForTransE" />
        <bean id="tokenizer" class="pfr.plugins.refiners.tokenizer.PfrPluginForTokenizer">
            <property name="focusSet">
                <set>
                    <value>pfr.plugins.parsers.stackoverflow.PfrPluginForStackOverflow.QUESTION_BODY</value>
                    <value>pfr.plugins.parsers.stackoverflow.PfrPluginForStackOverflow.QUESTION_TITLE</value>
                    <value>pfr.plugins.parsers.stackoverflow.PfrPluginForStackOverflow.ANSWER_BODY</value>
                    <value>pfr.plugins.parsers.stackoverflow.PfrPluginForStackOverflow.COMMENT_TEXT</value>
                </set>
            </property>
        </bean>
    </beans>

该配置文件指定了知识图谱将被构造在 E:/graphdb-jfreechart 这一文件夹中，构造该知识图谱的过程为：

1. 使用插件pfr.plugins.parsers.javacode.PfrPluginForJavaCode来解析文件夹E:/data/jfreechart/src中的源代码文件的代码结构，并加入知识图谱；
2. 使用插件pfr.plugins.parsers.stackoverflow.PfrPluginForStackOverflow来解析文件夹E:/data/jfreechart/qa中存储的stackoverflow数据，并加入知识图谱；
3. 使用插件pfr.plugins.refiners.codelinking.PfrPluginForCodeLinking来建立stackoverflow中的实体与代码元素之间的关联关系；
4. 使用插件pfr.plugins.refiners.codeembedding.PfrPluginForTransE来挖掘代码元素的语义向量；
5. 使用插件pfr.plugins.refiners.tokenizer.PfrPluginForTokenizer来对stackoverflow实体中的文本信息做词法处理。

作为一个用于快速了解的例子，大家目前不用知道具体的输入数据格式以及各个插件的输出是什么。只要知道，通过这个配置文件，我们告诉了SnowGraph：我们需要解析两类软件数据（1,2）；我们还需要进一步地对解析结果做挖掘与提炼（3,4,5）。
根据这个配置文件，SnowGraph对我们提供的软件数据进行处理，得到了一个简易的软件项目知识图谱。
该知识图谱通过Neo4j图数据库系统进行存储、管理与查询支持。
