# SnowGraph快速入门

环境准备
-------------------------------
- 编译运行环境：Java8;
- 依赖管理：Maven3.

示例数据准备
--------------------------------
开发团队成员可到内网文件服务器的 SnowGraph/testdata/lucene 文件夹中下载示例数据。

这一测试数据集包含开源项目Apache Lucene的源代码、邮件归档、缺陷库、StackOverflow归档这四类数据。

配置文件准备
--------------------------------
示例配置文件在项目目录resources/configs下，共有两个：
- config-primitive.xml.dist
- config-copy.xml.dist

请勿更改这两个配置文件，而应当复制它们（可以复制到项目外的任意位置，建议复制在原目录下），并去除.dist后缀，再进行更改。

起示例作用的主要是config-primitive.xml：

    <beans>
        <bean id="graph" class="pfr.framework.KnowledgeGraphBuilder">
            <property name="graphPath" value="C:/Users/Lin/Desktop/testdata/graphdb-primitive"/>
            <property name="pfrPlugins">
                <list>
                    <ref bean="codegraph" />
                    <ref bean="sograph" />
	    		        	<ref bean="mailgraph" />
                    <ref bean="issuegraph" />
                </list>
            </property>
        </bean>
        <bean id="codegraph" class="pfr.plugins.parsers.javacode.PfrPluginForJavaCode">
            <property name="srcPath" value="C:/Users/Lin/Desktop/testdata/sourcecode" />
        </bean>
        <bean id="sograph" class="pfr.plugins.parsers.stackoverflow.PfrPluginForStackOverflow">
            <property name="folderPath" value="C:/Users/Lin/Desktop/testdata/stackoverflow" />
        </bean>
        <bean id="issuegraph" class="pfr.plugins.parsers.issuetracker.PfrPluginForIssueTracker">
	    	<property name="extractor">
	    		<bean id="jiraExtractor" class="pfr.plugins.parsers.issuetracker.extractors.JiraExtractor">
	    			<property name="issueFolderPath" value="C:/Users/Lin/Desktop/testdata/jira" />
	    		</bean>
	    	</property>
	    </bean>
    	<bean id="mailgraph" class="pfr.plugins.parsers.mail.PfrPluginForMailList">
	    	<property name="mboxPath" value="C:/Users/Lin/Desktop/testdata/mbox" />
    	</bean>
    </beans>

该配置文件用于解析源代码、邮件归档、缺陷库、StackOverflow归档这四类数据，并生成相应的软件知识图谱。

需要配置的参数包括：
- 生成的知识图谱存放在哪个文件夹中（"C:/Users/Lin/Desktop/testdata/graphdb-primitive"）
- 源代码存放在哪个文件夹中（"C:/Users/Lin/Desktop/testdata/sourcecode"）
- StackOverflow归档存放在哪个文件夹中（"C:/Users/Lin/Desktop/testdata/stackoverflow"）
- 缺陷库归档存放在哪个文件夹中（"C:/Users/Lin/Desktop/testdata/jira"）
- 邮件归档存放在哪个文件夹中（"C:/Users/Lin/Desktop/testdata/mbox"）

config-copy.xml则示例了在开发过程中如何避免做大量的重复解析：

<beans>
    <bean id="graph" class="pfr.framework.KnowledgeGraphBuilder">
        <property name="graphPath" value="C:/Users/Lin/Desktop/testdata/graphdb-copy"/>
		    <property name="baseGraphPath" value="C:/Users/Lin/Desktop/testdata/graphdb-primitive"/>
    </bean>
</beans>

即：为graph这个bean注入baseGraphPath属性，则知识图谱不再是从零开始构建，而是会先复制一个已有的软件知识图谱（"C:/Users/Lin/Desktop/testdata/graphdb-primitive"），再基于其继续添加新的软件知识。
