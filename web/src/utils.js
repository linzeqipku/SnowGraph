export function rename(str) {
    if (str === "extend") return "父类 / 子类";
    if (str === "implement") return "实现的接口 / 实现本接口的类";
    if (str === "throw") return "抛出异常 / 抛出本异常的方法";
    if (str === "param") return "参数 / 以本类型为参数的方法";
    if (str === "rt") return "返回类型 / 以本类型为返回类型的方法";
    if (str === "have_method") return "声明方法 / 所属类型";
    if (str === "have_field") return "声明域 / 所属域";
    if (str === "call_method") return "本方法调用的方法 / 调用本方法的方法";
    if (str === "call_field") return "调用这个域的方法 / 本方法调用的域";
    if (str === "type") return "域的类型 / 定义为本类型的域";
    if (str === "variable") return "引用 / 引用本类型的方法";
    if (str === "have_sub_element") return "下级文档元素 / 上级文档元素";
    if (str === "api_explained_by") return "设计文档 / 本文档对应的代码元素";
    if (str === "function_designed_by") return "需求文档 / 设计文档";
    if (str.substr(3) === "extend" && str[0] === 'i') return "子类";
    if (str.substr(3) === "extend" && str[0] === 'o') return "父类 ";
    if (str.substr(3) === "implement" && str[0] === 'i') return "实现本接口的类";
    if (str.substr(3) === "implement" && str[0] === 'o') return "实现的接口";
    if (str.substr(3) === "throw" && str[0] === 'i') return "抛出本异常的方法";
    if (str.substr(3) === "throw" && str[0] === 'o') return "抛出异常";
    if (str.substr(3) === "param" && str[0] === 'i') return "以本类型为参数的方法";
    if (str.substr(3) === "param" && str[0] === 'o') return "参数";
    if (str.substr(3) === "rt" && str[0] === 'i') return "以本类型为返回类型的方法";
    if (str.substr(3) === "rt" && str[0] === 'o') return "返回类型";
    if (str.substr(3) === "have_method" && str[0] === 'i') return "所属类型";
    if (str.substr(3) === "have_method" && str[0] === 'o') return "声明方法";
    if (str.substr(3) === "have_field" && str[0] === 'i') return "所属类";
    if (str.substr(3) === "have_field" && str[0] === 'o') return "声明域";
    if (str.substr(3) === "call_method" && str[0] === 'i') return "调用本方法的方法";
    if (str.substr(3) === "call_method" && str[0] === 'o') return "本方法调用的方法";
    if (str.substr(3) === "call_field" && str[0] === 'i') return "调用这个域的方法";
    if (str.substr(3) === "call_field" && str[0] === 'o') return "本方法调用的域";
    if (str.substr(3) === "type" && str[0] === 'i') return "定义为本类型的域";
    if (str.substr(3) === "type" && str[0] === 'o') return "域的类型";
    if (str.substr(3) === "variable" && str[0] === 'i') return "引用本类型的方法";
    if (str.substr(3) === "variable" && str[0] === 'o') return "引用";
    if (str.substr(3) === "have_sub_element" && str[0] === 'i') return "上级文档元素";
    if (str.substr(3) === "have_sub_element" && str[0] === 'o') return "下级文档元素";
    if (str.substr(3) === "api_explained_by" && str[0] === 'i') return "本文档对应的代码元素";
    if (str.substr(3) === "api_explained_by" && str[0] === 'o') return "设计文档";
    if (str.substr(3) === "function_designed_by" && str[0] === 'i') return "设计文档";
    if (str.substr(3) === "function_designed_by" && str[0] === 'o') return "需求文档";
    return str;
}

export function getNodeIDFromRelation(relation) {
    const startID = parseInt(relation.start.substr(35), 10);
    const endID = parseInt(relation.end.substr(35), 10);
    return [startID, endID];
}

export function relation2format(relation) {
    const [startID, endID] = getNodeIDFromRelation(relation);
    return {
        id: relation["metadata"].id, type: relation.type.substr(3), startNode: startID, endNode: endID, properties: {}
    };
}

export function node2format(node) {
    return {
        id: node["metadata"].id,
        labels: node["metadata"].labels,
        properties: node.data
    };
}