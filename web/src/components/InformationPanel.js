import React, {Component} from 'react';
import {connect} from "react-redux";
import {
    Card, CardContent, CardHeader, LinearProgress,
    Table, TableBody, TableCell, TableRow, Typography, withStyles
} from "material-ui";
import CodeModal from "./CodeModal";

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

const styles = theme => ({
    normalCell: {
        wordWrap: "break-word",
        whiteSpace: "pre-wrap",
        wordBreak: "break-word",
    }
});

class InformationPanel extends Component {
    render() {
        let body = null;

        const {classes} = this.props;

        if (this.props.selectedNode && this.props.selectedNode.fetched) {
            const properties = Object.keys(propertyCnName)
                .filter(x => this.props.selectedNode.node.data.hasOwnProperty(x))
                .map(x => {
                    let content = this.props.selectedNode.node.data[x];
                    content = (x === "content" || x === "comment") ?
                        <CodeModal code label="SHOW" content={content}/> :
                        <div className={classes.normalCell}>{content.toString()}</div>;
                    return {key: x, label: x, content};
                });
            const label = this.props.selectedNode.node["metadata"].labels[0];
            body = <Table>
                <TableBody>
                    <TableRow>
                        <TableCell>Type: </TableCell>
                        <TableCell>{`${label}`}</TableCell>
                    </TableRow>
                    {properties.map(p => <TableRow key={p.key}>
                        <TableCell>{p.label}</TableCell>
                        <TableCell>{p.content}</TableCell>
                    </TableRow>)}
                </TableBody>
            </Table>;
        } else if (this.props.selectedNode) {
            body = <LinearProgress/>;
        } else {
            body = <Typography component="p"> Please select a node first </Typography>;
        }

        return (
            <Card>
                <CardHeader title="Entity Properties"/>
                <CardContent>
                    {body}
                </CardContent>
            </Card>
        );
    }
}

InformationPanel = connect(mapStateToProps)(InformationPanel)

export default withStyles(styles)(InformationPanel);
