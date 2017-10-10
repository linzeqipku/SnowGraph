import React, {Component} from 'react';
import {Card, CardBody, CardTitle, Table} from "reactstrap";
import {connect} from "react-redux";
import './InformationPanel.css';

var labelCnName = {
    "Class": "类", "Interface": "接口", "Method": "方法", "Field": "域", "DocxFile": "文档文件",
    "DocxSection": "文档章节", "DocxTable": "文档表格", "DocxPlainText": "文档文本信息"
};

const codePropertyCnName = {
    "name": "名称", "fullName": "全名", "access": "访问修饰符", "superClass": "父类", "implements": "实现接口",
    "extends": "父接口", "isAbstract": "是否抽象类(abstract)", "isFinal": "是否不可变(final)",
    "isStatic": "是否静态", "belongTo": "所属类", "comment": "注释", "content": "内容"
};

const docPropertyCnName = {
    "sectionTitle": "标题", "sectionContent": "内容", "tableContent": "表格内容",
    "docxName": "文档名称", "projectName": "项目名称", "plainTextContent": "文本内容"
};

const propertyCnName = Object.assign({}, codePropertyCnName, docPropertyCnName);

const mapStateToProps = (state) => {
    return {
        selectedNode: state.nodes[state.selectedNode],
    }
}

class InformationPanel extends Component {
    render() {
        let body = null;

        if (this.props.selectedNode && this.props.selectedNode.fetched) {
            const properties = Object.keys(propertyCnName)
                .filter(x => this.props.selectedNode.node.data.hasOwnProperty(x))
                .map(x => {
                    let content = this.props.selectedNode.node.data[x];
                    content = (x === "content" || x === "comment") ?
                        <pre className="pre-scrollable" dangerouslySetInnerHTML={{__html: content}}/> : content;
                    return {key: x, label: propertyCnName[x], content};
                });
            const label = this.props.selectedNode.node["metadata"].labels[0];
            body = <Table>
                <tbody>
                <tr>
                    <th>类型：</th>
                    <td>{`${label}(${labelCnName[label]})`}</td>
                </tr>
                {properties.map(p => <tr key={p.key}>
                    <th>{p.label}</th>
                    <td>{p.content}</td>
                </tr>)}
                </tbody>
            </Table>;
        } else if (this.props.selectedNode) {
            body = <div>获取结点信息中...</div>;
        } else {
            body = <div>请先选择一个结点</div>;
        }

        return (
            <Card>
                <CardBody className="CardBody">
                    <CardTitle>实体详细信息</CardTitle>
                    {body}
                </CardBody>
            </Card>
        );
    }
}

InformationPanel = connect(mapStateToProps)(InformationPanel)

export default InformationPanel;
