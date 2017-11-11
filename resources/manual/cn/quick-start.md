# SnowGraph Quick Start

环境准备
-------------------------------
- 编译运行环境：Java8;
- 依赖管理：Maven3.

示例数据准备
--------------------------------
示例数据下载链接: https://pan.baidu.com/s/1kV24ubX 密码: 8ccc

示例数据包含开源软件项目Apache Lucene的java源代码数据与StackOverflow社区问答数据，解压后可以放在本地文件系统的任意位置.

配置文件准备
--------------------------------
示例配置文件为：resources/configs/config-quickstart.xml.dist

该配置文件使用XML定义了一个软件项目知识图谱：
- 解析源代码中的实体和关联
- 解析StackOverflow数据中的实体和关联
- 解析StackOverflow数据中的自然语言文本是否提到了代码元素，并建立相应的关联

将该文件复制到项目外的任意位置，并对复制的文件进行修改，在其中定义好如下参数：
- 生成知识图谱的目标位置（原配置文件夹中的"C:/Users/Lin/Desktop/testdata/graphdb-primitive"处）
- 源代码存放在哪个文件夹中（原配置文件夹中的"C:/Users/Lin/Desktop/testdata/sourcecode"处）
- StackOverflow数据存放在哪个文件夹中（原配置文件夹中的"C:/Users/Lin/Desktop/testdata/stackoverflow"处）

生成图数据库
--------------------------------------------------
运行类graphdb。GraphdbGenerator的main函数
- 运行参数：{修改过的配置文件的地址}
- VM参数：-Xms3096m -Xmx3096m

图数据库的生成需要一段时间，请耐心等待程序运行完毕.

生成图数据库后，通过Neo4j图数据库提供的客户端工具(https://neo4j.com/, 3.2.0版本)（网盘: http://pan.baidu.com/s/1kV6745x 密码: qnyj 位置: ./tools/Neo4j-CE-3.2.0-windows.zip），可以对其进行浏览.
