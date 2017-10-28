git commit解析插件：graphdb.extractors.parsers.git.GitExtractor

插件配置
---

插件配置示例：

    <bean id="xxxx" class="graphdb.extractors.parsers.mail.git.GitExtractor">
        <property name="gitPath" value="E:/data/.../git" />
    </bean>
插件可注入的参数包括：
* gitPath, 字符串类型，代表commit文件的本地存放根目录。需要注意的是，本插件只针对git中原始commit格式文件进行解析。文件内部子文件不影响解析结果。

数据模型
---

git commit解析插件定义了如下类型的实体与关联：<br>
* 实体: `COMMIT, COMMITAUTHOR`, `MUTATEDFILE`。 <br>
* 关联: `IS_AUTHOR_OF_COMMIT`, `DELETER_OF_FILE`, `CREATER_OF_FILE`, `MODIFIER_OF_FILE`, `PARENT_OF_COMMIT`, `COMMIT_CHANGE_THE_CLASS`, `FILE_CONTAIN_THE_CLASS`。 <br>

### COMMIT实体 <br>
一个COMMIT实体代表项目代码提交历史的一次提交（commit）,包含如下属性：
* COMMIT_UUID: 该次提交的编号。如：00005759ba8fedbf4331d5d44fc83601e73e06b9 ；<br>
* COMMIT_VERSION:该次提交对应的项目版本号 ；<br>
* COMMIT_AUTHOR：该次提交的提交人。如：Oliver Cao ；<br>
* COMMIT_DATE:该次提交的提交时间。如：Tue Mar 10 04:21:48 CST 2015 ；<br>
* COMMIT_LOGMESSAGE：该次提交的提交信息 ；<br>
* COMMIT_PARENT_UUID：上次提交对应的编号 ；<br>
* COMMIT_SVN_URL：该次提交对应的SVN历史版本信息地址 。<br>

### COMMITAUTHOR实体 <br>
一个COMMITAUTHOR实体对应一次提交的提交人，包含如下属性：
* COMMITAUTHOR_NAME ：提交人姓名。 <br>

### MUTATEDFILE实体 <br>
一个MUTATEDFILE实体代表一次提交中被修改的文件我，包含如下属性：
* MUTATEDFILE_TYPE：被修改文件的修改类型，包含：ADDED（新增），DELETEED（删除），MODIFIED（修改），MODECHANGED()；<br>
* MUTATEDFILE_FILE_NAME：被修改文件的文件名； <br>
* MUTATEDFILE_API_QUALIFIEDNAME：被修改文件中涉及到的类的全名，如：apache.lucene.codecs.bulkvint.BulkVIntPostingsFormat；<br>
* MUTATEDFILE_API_NAME：被修改文件中涉及到的类的类名，如：BulkVIntPostingsFormat；<br>
* MUTATEDFILE_FORMER_NAME：被修改文件修改前的文件名。注：新增（ADDED）文件无此属性；<br>
* MUTATEDFILE_LATTER_NAME：被修改文件修改后的文件名。注：删除（DELETEED）文件无此属性；<br>
* MUTATEDFILE_CREATER_UUID：新增文件对应的提交编号；<br>
* MUTATEDFILE_DELETER_UUID：删除文件对应的提交编号；<br>

### 关联类型
* IS_AUTHOR_OF_COMMIT：(a)-[IS_AUTHOR_OF_COMMIT]->(b)代表提交人a提交了b ； <br>
* DELETER_OF_FILE：(a)-[DELETER_OF_FILE]->(b)代表在提交b删除了文件a ；<br>
* CREATER_OF_FILE：(a)-[CREATER_OF_FILE]->(b)代表在提交b创建了文件a ；<br>
* MODIFIER_OF_FILE：(a)-[MODIFIER_OF_FILE]->(b)代表在提交b修改了文件a ；<br>
* PARENT_OF_COMMIT：(a)-[PARENT_OF_COMMIT]->(b)代表提交a是提交b的上一次提交 ； <br>
* COMMIT_CHANGE_THE_CLASS：(a)-[COMMIT_CHANGE_THE_CLASS]->(b)代表提交a中修改类b ； <br>
* FILE_CONTAIN_THE_CLASS：(a)-[FILE_CONTAIN_THE_CLASS]->(b)代表文件a中包含类b 。 <br>

  
