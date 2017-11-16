# 开发你的数据解析/知识提炼模块

模块接口
-------------------------------------
SnowGraph为软件工程数据的研究者提供了统一的接口来开发新的数据解析模块或知识提炼模块:

[graphdb.framework.Extractor](https://github.com/linzeqipku/SnowGraph/blob/master/src/main/java/graphdb/framework/Extractor.java)

该接口只包含一个方法：void run(GraphDatabaseService graphDB);

示例场景
--------------------------------------
我们以这样一个简单的场景来说明如何开发一个新的数据解析/知识提炼模块：

我想要在[Quick Start](https://github.com/linzeqipku/SnowGraph/blob/master/resources/manual/cn/quick-start.md)中生成的知识图谱的基础上，从StackOverflow数据的文本内容中抽取出所有代码片段，并加入到知识图谱中. 每段代码片段表示为一个实体，且将该实体与相应的StackOverflow实体相关联.

模块开发
--------------------------------------

1. 新建一个类，这个类实现接口[graphdb.framework.Extractor](https://github.com/linzeqipku/SnowGraph/blob/master/src/main/java/graphdb/framework/Extractor.java)

2. 如果该模块需要解析已有知识图谱之外的数据，在这个类中定义一个set方法作为数据入口. 如：

<pre><code>
public void setSrcPath(String srcPath) {
    this.srcPath = srcPath;
}
</pre></code>

在这个示例场景中，无需用到外源数据，因此这一步可以跳过.

3. 在这个类中定义要添加的新实体类型
<pre><code>
@EntityDeclaration
public static final String CODE_FRAGMENT = "CodeFragment";
</pre></code>

4. 在这个类中定义要添加的新属性
<pre><code>
@PropertyDeclaration(parent = CODE_FRAGMENT)
public static final String CODE_FRAGMENT_CONTENT = "content";
</pre></code>

5. 在这个类中定义要添加的新关联类型
<pre><code>
@RelationshipDeclaration
public static final String HAVE_CODE_FRAGMENT = "haveCodeFragment";
</pre></code>

6. 实现方法void run(GraphDatabaseService graphDB)：
<pre><code>
void run(GraphDatabaseService graphDB){
    ...//从graphDB中读取已有的知识图谱中的内容
    ...//抽取代码片段
    ...//将实体和边写入到graphDB中
}
</pre></code>
其中，图数据库的读写可参考[neo4j java Embedded API](https://neo4j.com/docs/java-reference/current/#tutorials-java-embedded).

或参考[本项目中已实现的模块](https://github.com/linzeqipku/SnowGraph/blob/master/resources/manual/cn/list.md).

运行模块
--------------------------------------

1. 编写测试用的配置文件，例如：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="graph" class="graphdb.framework.GraphBuilder">
        <property name="graphPath" value="{生成知识图谱的目标地址}"/>
        <property name="extractors">
            <list>
                <ref bean="sograph" />
                <ref bean="codefrag" />
            </list>
        </property>
    </bean>
    <bean id="sograph" class="graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor">
        <property name="folderPath" value="{源代码文件夹路径}" />
    </bean>
	  <bean id="codefrag" class="{新定义的这个类的带包路径的全名}">
    </bean>
</beans>
```

2. 通过这个配置文件来生成知识图谱.

调试模式
--------------------------------------

在上面的例子中，每次我们想要调试我们新开发的模块，都得重新运行一次StackOverflowExtractor模块，这很浪费时间.
因此，我们允许开发者分步生成知识图谱.

例如，在这个例子中，我们可以先使用如下配置文件生成一个只解析了StackOverflow数据的知识图谱：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="graph" class="graphdb.framework.GraphBuilder">
        <property name="graphPath" value="{临时知识图谱的存放地址}"/>
        <property name="extractors">
            <list>
                <ref bean="sograph" />
            </list>
        </property>
    </bean>
    <bean id="sograph" class="graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor">
        <property name="folderPath" value="{源代码文件夹路径}" />
    </bean>
</beans>
```

之后，每次调试新编写的模块时，按如下配置文件运行即可：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="graph" class="graphdb.framework.GraphBuilder">
        <property name="graphPath" value="{生成知识图谱的目标地址}"/>
        <property name="baseGraphPath" value="{之前定义的临时知识图谱的存放地址}"/>
        <property name="extractors">
            <list>
                <ref bean="codefrag" />
            </list>
        </property>
    </bean>
	  <bean id="codefrag" class="{新定义的这个类的带包路径的全名}">
    </bean>
</beans>
```
