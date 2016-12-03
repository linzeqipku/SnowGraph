代码元素引用关联插件：extractors.linkers.codeindoc.CodeInDocKnowledgeExtractor

插件配置
-----------------------------------------------------
插件配置示例：

    <bean id="xxxxx" class="extractors.linkers.codeindoc.CodeInDocKnowledgeExtractor">
        <property name="focusSet">
            <set>
                <value>extractors.parsers.stackoverflow.StackOverflowKnowledgeExtractor.QUESTION_BODY</value>
                <value>extractors.parsers.stackoverflow.StackOverflowKnowledgeExtractor.QUESTION_TITLE</value>
                <value>extractors.parsers.stackoverflow.StackOverflowKnowledgeExtractor.ANSWER_BODY</value>
                <value>extractors.parsers.stackoverflow.StackOverflowKnowledgeExtractor.COMMENT_TEXT</value>
                <value>....</value>
            </set>
        </property>
    </bean>

插件可注入的参数包括：
- focusSet，是一个列表，该列表的每一项指定了软件知识图谱中的某种类型的实体的某个属性。本插件将处理这些类型的实体的这些属性，解析其中引用了哪些代码元素，并建立关联。
