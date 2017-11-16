StackOverflow解析插件：graphdb.extractors.parsers.stackoverflow.StackOverflowKnowledgeExtractor

插件配置
-------------------------------

插件配置示例：

    <bean id="xxxx" class="graphdb.extractors.parsers.stackoverflow.StackOverflowKnowledgeExtractor">
        <property name="folderPath" value="E:/data/.../qa" />
    </bean>

插件可注入的参数包括：
- folderPath，字符串类型，代表StackOverflow数据文件夹路径。该文件夹下应有5个xml文件：Questions.xml, Answers.xml, Comments.xml, Users.xml, PostLinks.xml。各个文件的格式按照StackOverflow的原始Data Dump的格式组织。

数据模型
------------------------------

StackOverflow解析插件定义了如下类型的实体与关联：
-实体：QUESTION, ANSWER, COMMENT, USER.
-关联：HAVE_ANSWER, HAVE_COMMENT, AUTHOR, DUPLICATE.

QUESTION实体
-------------------------------
一个QUESTION实体代表StackOverflow中的一个问题，包含如下属性：
- QUESTION_ID：字符串，表示问题的唯一编号，由数字组成，可以用 http://stackoverflow.com/questions/{QUESTION_ID}/ 来访问该问题；
- QUESTION_CREATION_DATE：问题的创建时间，例如“2009-01-07T15:18:38.573”；
- QUESTION_SCORE：问题的得分，一个自然数；
- QUESTION_VIEW_COUNT：问题被浏览过的次数；
- QUESTION_BODY：问题正文，html格式；
- QUESTION_OWNER_USER_ID：提问者的编号；
- QUESTION_TITLE：问题的标题；
- QUESTION_TAGS：问题的标签，例如"&lt;java&gt;&lt;ms-word&gt;&lt;apache-poi&gt;"。

ANSWER实体
--------------------------------
一个ANSWER实体代表StackOverflow中的一个答案，包含如下属性：
- ANSWER_ACCEPTED：布尔值，代表该答案是否被提问者标为最佳答案，一个问题只能有0个或1个最佳答案；
- ANSWER_ID：答案的唯一编号，字符串，由数字构成；
- ANSWER_PARENT_QUESTION_ID：该答案所属的问题的编号；
- ANSWER_CREATION_DATE：答案的创建时间，例如“2009-01-07T15:18:38.573”；
- ANSWER_SCORE：答案得分，是一个自然数；
- ANSWER_BODY：答案正文，html格式；
- ANSWER_OWNER_USER_ID：回答者的编号。

COMMENT实体
-------------------------------
一个COMMENT实体代表StackOverflow中的一条评论（可以是问题评论，也可以是答案评论），包含如下属性：
- COMMENT_ID：评论的唯一编号，字符串，由数字构成；
- COMMENT_PARENT_ID：评论所属的问题/答案的编号，问题和答案采用共同编号，不会发生冲突；
- COMMENT_SCORE：评论得分，是一个自然数；
- COMMENT_TEXT：评论正文；
- COMMENT_CREATION_DATE：评论的创建时间，例如“2009-01-07T15:18:38.573”；
- COMMENT_USER_ID：评论者的编号。

USER实体
--------------------------------
一个USER实体代表StackOverflow中的一个用户，包含如下属性：
- USER_ID：用户的唯一编号，字符串，由数字构成；
- USER_REPUTATION：用户的声望值，一个自然数；
- USER_CREATION_DATE：用户的注册时间，例如“2009-01-07T15:18:38.573”；
- USER_DISPLAY_NAME：用户的昵称；
- USER_LAST_ACCESS_DATE：用户最后登陆的时间；
- USER_VIEWS：该用户的主页被多少人看过；
- USER_UP_VOTES：该用户一共点了多少次赞，是一个自然数；
- USER_DOWN_VOTES：该用户一共点了多少次踩，是一个自然熟。

关联类型
---------------------------------
- HAVE_ANSWER：问题有答案，(a)-[HAVE_ANSWER]->(b)代表b是问题a的一个答案；
- HAVE_COMMENT：问题/答案有评论，(a)-[HAVE_COMMENT]->(b)代表b是问题/答案a的一条评论；
- AUTHOR：作者，(a)-[AUTHOR]->(b)代表用户a是问题/答案/评论的作者；
- DUPLICATE：用户标注的重复问题关联，(a)-[DUPLICATE]->(b)代表问题a是一个重复问题，和以往的问题b重复。
