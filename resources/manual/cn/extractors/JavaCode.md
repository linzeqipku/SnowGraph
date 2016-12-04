Java源代码解析插件：extractors.parsers.javacode.JavaCodeKnowledgeExtractor

插件配置
------------------------

插件配置示例：

    <bean id="xxxx" class="extractors.parsers.javacode.JavaCodeKnowledgeExtractor">
        <property name="srcPath" value="E:/data/.../src" />
    </bean>

插件可注入的参数包括：
- srcPath，字符串类型，代表源代码文件夹在本地存放的根目录。需要注意的是，目前暂不支持对压缩文件以及远程文件的解析。

数据模型
-------------------------

Java源代码解析插件定义了如下类型的实体与关联：
- 实体：CLASS, INTERFACE, METHOD, FIELD.
- 关联：EXTEND, IMPLEMENT, THROW, PARAM, RT, HAVE_METHOD, HAVE_FIELD, CALL_METHOD, CALL_FIELD, TYPE, VARIABLE.

### CLASS实体
一个CLASS实体代表Java源代码文件中的一个类，包含如下属性：
- CLASS_NAME: 类名（短名，不包含包路径，如“IndexReader”）；
- CLASS_FULLNAME：全名（包含包路径，如“org.apache.lucene.index.IndexReader”）；
- CLASS_SUPERCLASS：父类的全名（如“java.lang.object”）；
- CLASS_IMPLEMENTS：该类实现了的接口的全名（若实现了多个接口，则接口之间以“, ”分隔）；
- CLASS_IS_ABSTRACT：布尔值，代表该类是否是抽象类；
- CLASS_IS_FINAL：布尔值，代表该类是否是终态的；
- CLASS_ACCESS：字符串，代表该类的访问修饰符，可能的取值包括："private", "protected", "public", "package"；
- CLASS_COMMENT：Javadoc注释；
- CLASS_CONTENT：该类的全文。

### INTERFACE实体
一个INTERFACE实体代表Java源代码文件中的一个接口，包含如下属性：
- INTERFACE_NAME：接口名字；
- INTERFACE_FULLNAME：接口全名；
- INTERFACE_SUPERINTERFACES：接口继承的父接口，若有多个父接口，则父接口之间以“, ”分隔；
- INTERFACE_ACCESS：接口的访问修饰符，可能的取值包括："private", "protected", "public", "package"；
- INTERFACE_COMMENT：Javadoc注释；
- INTERFACE_CONTENT：接口的全文。

### METHOD实体
一个METHOD实体代表Java源代码文件中的一个方法，包含如下属性：
- METHOD_NAME：方法名字，如“add”；
- METHOD_BELONGTO：方法所属的类/接口的全名；
- METHOD_PARAMS：方法声明中的参数字符串，如“String a, List&lt;Integer&gt; b”；
- METHOD_RETURN：方法声明中的返回字符串，如“String”，“void”；
- METHOD_THROWS：方法抛出的异常的全名，如果抛出多个异常，使用“, ”进行分隔；
- METHOD_ACCESS：方法的访问修饰符，可能的取值包括："private", "protected", "public", "package"；
- METHOD_IS_ABSTRACT：布尔值，代表方法是否是抽象方法；
- METHOD_IS_STATIC：布尔值，代表方法是否是静态方法；
- METHOD_IS_FINAL：布尔值，代表方法是否是终态的；
- METHOD_IS_SYNCHRONIZED：布尔值，代表方法是否是同步的；
- METHOD_IS_CONSTRUCTOR：布尔值，代表方法是否是构造方法；
- METHOD_COMMENT：javadoc注释；
- METHOD_CONTENT：方法体全文。

### FIELD实体
一个FIELD实体代表Java源代码文件中的一个域，包含如下属性：
- FIELD_NAME：域名；
- FIELD_TYPE：字符串，在定义域的时候声明的域的类型；
- FIELD_BELONGTO：域所属的类的全名；
- FIELD_IS_STATIC：布尔值，域是否是静态的；
- FIELD_ACCESS：布尔值，域的访问修饰符，可能的取值包括："private", "protected", "public", "package"；
- FIELD_COMMENT：javadoc注释。

### 关联类型
- EXTEND：类/接口的继承关系，(a)-[EXTEND]-&gt(b)代表a是b的子类/子接口；
- IMPLEMENT：实现，(a)-[IMPLEMENT]-&gt(b)代表类a实现了接口b；
- THROW：抛出异常，(a)-[THROW]-&gt(b)代表方法a抛出异常b；
- PARAM：参数，(a)-[PARAM]-&gt(b)代表方法a的参数中有类/接口b；
- RT：返回类型，(a)-[RT]-&gt(b)代表方法a的返回类型中有类/接口b；
- HAVE_METHOD：(a)-[HAVE_METHOD]-&gt(b)代表类/接口a包含方法b；
- HAVE_FIELD：(a)-[HAVE_FIELD]-&gt(b)代表类/接口a包含域b；
- CALL_METHOD：(a)-[CALL_METHOD]-&gt(b)代表方法a调用了方法b；
- CALL_FIELD：(a)-[CALL_FIELD]-&gt(b)代表方法a使用了域b；
- TYPE：(a)-[TYPE]-&gt(b)代表域a的类型中有类/接口b；
- VARIABLE：(a)-[VARIABLE]-&gt(b)代表方法a中定义的某些局部变量的类型中有类/接口b。
