缺陷追踪系统解析插件：extractors.parsers.jira.JiraKnowledgeExtractor

插件配置
----------------------------------
    <bean id="xxxx" class="extractors.parsers.jira.JiraKnowledgeExtractor">
        <property name="parser">
            <bean id="jiraParser" class="extractors.parsers.jira.JiraKnowledgeExtractor">
                <property name="issueFolderPath" value="E:/data/.../jira" />
            </bean>
        </property>
    </bean>

插件可注入的参数包括：
- parser: 一个对象，这个对象的类实现了extractors.parsers.issuetracker.IssueTrackerParser接口。不同的类用于解析不同格式的缺陷追踪系统（如jira与BugZilla）。

目前，缺陷追踪系统解析插件仅支持对存放在本地文件夹中的jira数据文件进行解析，对应的parser类为pfr.extractors.parsers.jira.JiraKnowledgeExtractor。该类有一个可注入的参数：issueFolderPath，用于指定jira数据文件存放的根目录。

对于jira数据文件而言，其组织结构应为：
- 根目录下应包含多个文件夹，每个文件夹对应一个缺陷报告，文件夹名不影响解析结果；
- 每个缺陷报告文件夹下有一个.json文件，以该缺陷的缺陷名（如"Lucene-1"）命名，该文件来自jira的数据访问API；
- 每个缺陷报告文件夹下可能有一个patches文件夹，该文件夹下有一个或多个文件夹，每个文件夹对应一个补丁，文件夹名为该补丁的编号；
- 每个补丁文件夹下有一个.patch文件，以缺陷的缺陷名命名，同一个缺陷下的补丁的名字是相同的。

数据模型
----------------------------------

缺陷追踪系统解析插件定义了如下类型的实体与关联：
- 实体：ISSUE, PATCH, ISSUECOMMENT, ISSUEUSER.
- 关联：HAVE_PATCH, HAVE_ISSUE_COMMENT, ISSUE_DUPLICATE, ISSUE_AUTHOR, PATCH_AUTHOR, ISSUECOMMENT_AUTHOR.

ISSUE实体
---------------------------------
一个MAIL实体代表缺陷追踪系统中的一个缺陷报告，包含如下属性：
- ISSUE_ID：缺陷报告编号，是一个由数字组成的字符串；
- ISSUE_NAME：缺陷报告名，一般为项目名和缺陷编号的组合，例如“LUCENE-1”；
- ISSUE_SUMMARY：缺陷报告的标题；
- ISSUE_TYPE：缺陷报告的类型，是一个字符串，可能的取值包括："Bug", "Improvement", "New Feature", "Sub-task", "Task", "Test", "Wish"；
- ISSUE_STATUS：缺陷报告的当前状态，是一个字符串，可能的取值包括："Open", "In Progress", "Reopened", "Resolved", "Closed"；
- ISSUE_PRIORITY：缺陷报告的优先级，是一个字符串，可能的取值包括："Blocker", "Critical", "Major", "Minor", "Trivial"；
- ISSUE_RESOLUTION：缺陷的解决状态，是一个字符串，由用户自由填写，例如："Fixed"；
- ISSUE_VERSIONS：该缺陷报告针对的是项目的哪个版本，如果有多个版本，用逗号隔开，例如："1.2,1.3,1.4"；
- ISSUE_FIX_VERSIONS：该缺陷在项目哪个版本被修复了；
- ISSUE_COMPONENTS：该缺陷涉及到的模块，如果有多个模块，用逗号隔开，例如："core/store,core/index"；
- ISSUE_LABELS：该缺陷报告的标签，如果有多个模块，用逗号隔开；
- ISSUE_DESCRIPTION：缺陷描述正文；
- ISSUE_CREATOR_NAME：缺陷报告创建者的名字；
- ISSUE_ASSIGNEE_NAME：被指定来修复该缺陷的用户的名字；
- ISSUE_REPORTER_NAME：缺陷报告的报告者的名字；
- ISSUE_CREATED_DATE：缺陷报告创建时间，是一个字符串，例如："2006-06-06T06:24:38.000+0000"；
- ISSUE_UPDATED_DATE：最近更新时间；
- ISSUE_RESOLUTION_DATE：解决时间。

PATCH实体
--------------------------------------
一个PATCH实体代表缺陷追踪系统中的一个补丁，包含如下属性：
- PATCH_ISSUE_ID：该补丁所属的缺陷报告的编号；
- PATCH_ID：该补丁的编号，是一个由数字构成的字符串；
- PATCH_NAME：该补丁的名字，一般和其所属的缺陷报告的名字相同；
- PATCH_CONTENT：补丁内容，是一个diff文件；
- PATCH_CREATOR_NAME：补丁创建者的名字；
- PATCH_CREATED_DATE：补丁创建日期。

ISSUECOMMENT实体
----------------------------------------
一个ISSUECOMMENT实体代表缺陷追踪系统中的一条对缺陷报告的评论，包含如下属性：
- ISSUECOMMENT_ID：评论的编号，是一个由数字构成的字符串；
- ISSUECOMMENT_BODY：评论正文；
- ISSUECOMMENT_CREATOR_NAME：评论者的名字；
- ISSUECOMMENT_UPDATER_NAME：评论更新者的名字；
- ISSUECOMMENT_CREATED_DATE：评论创建时间；
- ISSUECOMMENT_UPDATED_DATE：评论更新时间。

ISSUEUSER实体
------------------------------------
一个ISSUEUSER实体代表缺陷追踪系统中的一个用户，包含如下属性：
- ISSUEUSER_NAME：用户名字；
- ISSUEUSER_EMAIL_ADDRESS：用户邮箱地址；
- ISSUEUSER_DISPLAY_NAME：用户昵称；
- ISSUEUSER_ACTIVE：布尔值，代表用户目前是否活跃。

关联类型
-------------------------------------

- HAVE_PATCH：补丁与缺陷报告之间的从属关系，(a)-[HAVE_PATCH]->(b)代表补丁b是缺陷报告a中的一个补丁；
- HAVE_ISSUE_COMMENT：评论与缺陷报告之间的从属关系，(a)-[HAVE_ISSUE_COMMENT]->(b)代表评论b是缺陷报告a中的一个补丁；
- ISSUE_DUPLICATE：由管理者标出的重复缺陷，(a)-[ISSUE_DUPLICATE]->(b)代表缺陷报告a与之前的缺陷报告b重复；
- IS_ASSIGNEE_OF_ISSUE：缺陷报告的负责人关系，(a)-[IS_ASSIGNEE_OF_ISSUE]->(b)代表用户a被指定为修复缺陷报告b的负责人；
- IS_CREATOR_OF_ISSUE：缺陷报告的创建者关系，(a)-[IS_CREATOR_OF_ISSUE]->(b)代表用户a是缺陷报告b的创建者；
- IS_REPORTER_OF_ISSUE：缺陷报告的报告者关系，(a)-[IS_REPORTER_OF_ISSUE]->(b)代表用户a是缺陷报告b的报告者；
- IS_CREATOR_OF_PATCH：补丁的创建者，(a)-[IS_CREATOR_OF_PATCH]->(b)代表补丁b是用户a写的；
- IS_CREATOR_OF_ISSUECOMMENT：缺陷报告评论的创建者关系，(a)-[IS_CREATOR_OF_ISSUECOMMENT]->(b)代表用户a是缺陷报告评论b的创建者；
- IS_UPDATER_OF_ISSUECOMMENT：缺陷报告评论的更新者关系，(a)-[IS_UPDATER_OF_ISSUECOMMENT]->(b)代表用户a是缺陷报告评论b的更新者；
