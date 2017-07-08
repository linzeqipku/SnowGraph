邮件列表解析插件：graphdb.extractors.parsers.mail.MailListKnowledgeExtractor

插件配置
------------------

插件配置示例：

    <bean id="xxxx" class="graphdb.extractors.parsers.mail.MailListKnowledgeExtractor">
        <property name="mboxPath" value="E:/data/.../mbox" />
    </bean>

插件可注入的参数包括：
- mboxPath，字符串类型，代表邮件归档文件的本地存放根目录。需要注意的是，目前本插件只对mbox格式的邮件归档进行支持，且不支持对远程文件的解析。文件夹内部的子文件夹结构不影响解析结果。

数据模型
-----------------------
邮件列表解析插件定义了如下类型的实体与关联：
- 实体：MAIL, MAILUSER.
- 关联：MAIL_IN_REPLY_TO, MAIL_SENDER, MAIL_RECEIVER.

### MAIL实体

一个MAIL实体代表邮件列表中的一封邮件，包含如下属性：
- MAIL_ID：字符串，实际上是一个数字，是邮件在邮件归档中的唯一编号；
- MAIL_SUBJECT：邮件的题目；
- MAIL_SENDER_NAME：字符串，发件人的名字；
- MAIL_SENDER_MAIL：发件人的邮件地址；
- MAIL_RECEIVER_NAMES：收件人的名字，若有多个收件人则用“, ”分隔；
- MAIL_RECEIVER_MAILS：收件人的邮件地址，和收件人的名字顺序对应，若有多个收件人则用“, ”分隔；
- MAIL_DATE：发件日期，例如“Tue, 1 Nov 2016 14:51:25 +0900”；
- MAIL_BODY：邮件正文。

### MAILUSER实体

一个MAILUSER实体代表邮件列表中的一个发件人/收件人地址，包含如下属性：
- MAILUSER_NAME：名字；
- MAILUSER_MAIL：邮箱地址。

### 关联类型
- MAIL_IN_REPLY_TO：(a)-[MAIL_IN_REPLY_TO]-&gt(b)代表a是对邮件b的回复；
- MAIL_SENDER：(a)-[MAIL_SENDER]-&gt(b)代表a是邮件b的发送者；
- MAIL_RECEIVER：(a)-[MAIL_RECEIVER]-&gt(b)代表a是邮件b的接受者。
