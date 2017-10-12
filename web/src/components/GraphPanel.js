import React, {Component} from 'react';
import {Card, CardContent, Typography} from "material-ui";
import Neo4jd3 from '../neo4jd3.js';
import {drawGraph, fetchNode, fetchRelationList, selectNode} from "../action";
import {connect} from "react-redux";

const mapStateToProps = (state) => {
    return {
        isFetchedGraph: state.graph.state === "fetched",
        nodes: state.nodes,
        relationLists: state.relationLists,
        graph: state.graph.graph,
        result: state.graph.result,
        test: state.graph,
    }
}

const mapDispatchToProps = {
    selectNode: selectNode,
    fetchNode: fetchNode,
    fetchRelationList: fetchRelationList,
    drawGraph: drawGraph
}

class GraphPanel extends Component {

    getNodes() {
        return this.props.nodes;
    }

    getRelationLists() {
        return this.props.relationLists;
    }

    getGraph() {
        return this.props.graph;
    }

    componentDidMount() {
        const neo4jd3 = new Neo4jd3('#neo4jd3', {
            showClassChnName: true,
            classes: {
                "Class": {
                    'englishName': "Class",
                    "chnName": "类",
                    "nodeFillColor": "#68bdf6" // light blue
                },
                "Method": {
                    'englishName': "Method",
                    'chnName': "方法",
                    "nodeFillColor": '#6dce9e' // green #1
                },
                'Interface': {
                    'englishName': "Interface",
                    'chnName': "接口",
                    "nodeFillColor": '#faafc2' // light pink
                },
                'Field': {
                    'englishName': "Field",
                    'chnName': "域",
                    "nodeFillColor": '#f2baf6' // purple
                },
                'DocxFile': {
                    'englishName': "DocxFile",
                    'chnName': "docx文件",
                    "nodeFillColor": '#ff928c' // light red
                },
                'DocxPlainText': {
                    'englishName': "DocxPlainText",
                    'chnName': "docx文本",
                    "nodeFillColor": '#fcea7e' // light yellow
                },
                'DocxSection': {
                    'englishName': "DocxSection",
                    'chnName': "docx章节",
                    "nodeFillColor": '#ffc766' // light orange
                },
                'DocxTable': {
                    'englishName': "DocxTable",
                    'chnName': "docx表格",
                    "nodeFillColor": '#405f9e' // navy blue
                }

            },
            showRlationshipsChnName: false,
            relationships: {
                "api_explained_by": {
                    "englishName": "api_explained_by",
                    "chnName": "设计文档 / 本文档对应的代码元素"
                },
                "call_field": {
                    "englishName": "call_field",
                    "chnName": "call_field未定义"
                },
                "call_method": {
                    "englishName": "call_method",
                    "chnName": "本方法调用的方法 / 调用本方法的方法"
                },
                "extend": {
                    "englishName": "extend",
                    "chnName": "子类  / 父类"
                },
                "function_designed_by": {
                    "englishName": "function_designed_by",
                    "chnName": "该用户设计的方法  / 设计该方法的人"
                },
                "hava_field": {
                    "englishName": "hava_field",
                    "chnName": "本方法拥有的域  / 拥有该域的方法"
                },
                "have_method": {
                    "englishName": "have_method",
                    "chnName": "本类拥有的方法  / 拥有本方法的类"
                },
                "have_sub_element": {
                    "englishName": "have_sub_element",
                    "chnName": "需求文档 / 设计文档"
                },
                "implement": {
                    "englishName": "implement",
                    "chnName": "实现的接口  / 实现的接口的类"
                },
                "param": {
                    "englishName": "param",
                    "chnName": "参数  / 以本类型为参数的方法"
                },
                "rt": {
                    "englishName": "rt",
                    "chnName": "返回类型  / 以本类型为返回类型的方法"
                },
                "throw": {
                    "englishName": "throw",
                    "chnName": "抛出异常  / 抛出本异常的方法"
                },
                "type": {
                    "englishName": "type",
                    "chnName": "域的类型  / 定义本类型的方法"
                },
                "variable": {
                    "englishName": "variable",
                    "chnName": "引用  / 引用本类型的方法"
                }

            },
            highlight: [
                {
                    class: 'Project',
                    property: 'name',
                    value: 'neo4jd3'
                }, {
                    class: 'User',
                    property: 'userId',
                    value: 'eisman'
                }
            ],
            icons: {
                'Api': 'gear',
                'Method': '',
                'Field': '',
                'Class': '',
                'Interface': '',
                'Cookie': 'paw',
                'Email': 'at',
                'Git': 'git',
                'Github': 'github',
                'gitCommit': 'github',
                'Google': 'google',
                'Ip': 'map-marker',
                'Issues': 'exclamation-circle',
                'Language': 'language',
                'Options': 'sliders',
                'Password': 'lock',
                'Phone': 'phone',
                'Project': 'folder-open',
                'SecurityChallengeAnswer': 'commenting',
                'User': '',
                'MailUser': '',
                'IssueUser': '',
                'StackOverflowUser': '',
                'gitCommitAuthor': '',
                "mutatedFile": "file-o",
                'zoomFit': 'arrows-alt',
                'zoomIn': 'search-plus',
                'zoomOut': 'search-minus'
            },
            images: {
                'Address': 'img/twemoji/1f3e0.svg',
                'Api': 'img/twemoji/1f527.svg',
                'Method': 'img/twemoji/M.svg',
                'Field': 'img/twemoji/F.svg',
                'Class': 'img/twemoji/C.svg',
                'Interface': 'img/twemoji/I.svg',
                'BirthDate': 'img/twemoji/1f382.svg',
                'Cookie': 'img/twemoji/1f36a.svg',
                'CreditCard': 'img/twemoji/1f4b3.svg',
                'Device': 'img/twemoji/1f4bb.svg',
                'Mail': 'img/twemoji/2709.svg',
                'Git': 'img/twemoji/1f5c3.svg',
                // 'gitCommit' : 'img/twemoji/1f5c3.svg',
                'Github': 'img/twemoji/1f5c4.svg',
                'icons': 'img/twemoji/1f38f.svg',
                'Ip': 'img/twemoji/1f4cd.svg',
                'Issue': 'img/twemoji/1f4a9.svg',
                'Patch': 'img/twemoji/1f4a9.svg',
                'Language': 'img/twemoji/1f1f1-1f1f7.svg',
                'Options': 'img/twemoji/2699.svg',
                'Password': 'img/twemoji/1f511.svg',
//                        'Phone': 'img/twemoji/1f4de.svg',
                'Project': 'img/twemoji/2198.svg',
                'Project|name|neo4jd3': 'img/twemoji/2196.svg',
//                        'SecurityChallengeAnswer': 'img/twemoji/1f4ac.svg',
                'User': 'img/twemoji/1f600.svg',
                'MailUser': 'img/twemoji/user.svg',
                'IssueUser': 'img/twemoji/user.svg',
                'StackOverflowUser': 'img/twemoji/user.svg',
                'gitCommitAuthor': 'img/twemoji/user.svg',
                'StackOverflowQuestion': 'img/twemoji/stackoverflow.svg',
                'StackOverflowAnswer': 'img/twemoji/stackoverflow.svg',
                'StackOverflowComment': 'img/twemoji/stackoverflow.svg'
//                        'zoomFit': 'img/twemoji/2194.svg',
//                        'zoomIn': 'img/twemoji/1f50d.svg',
//                        'zoomOut': 'img/twemoji/1f50e.svg'
            },
            minCollision: 60,
            neo4jData: this.props.result["searchResult"],
            nodeRadius: 40,
            onNodeClick: (node) => {
                this.props.fetchNode(node.id, this.getNodes(), this.getGraph());
                this.props.fetchRelationList(node.id, this.getRelationLists(), this.getGraph());
                this.props.selectNode(node.id);
            },
            onNodeDoubleClick: () => {
            },
            onRelationshipDoubleClick: () => {
            },
            zoomFit: false,
            infoPanel: false,
            lineColor: "black",
            lineWidth: "3",
            relationTextColor: "black",
            relationTextFontSize: "15px",
            nodeFillColor: "#F0F8FF"
        })
        this.props.drawGraph(neo4jd3);
    }

    render() {
        return (
            <Card>
                <CardContent>
                    <Typography type="headline" component="h2"> 相关的代码结构子图 </Typography>
                    <div style={{height: 800}} id="neo4jd3"/>
                </CardContent>
            </Card>
        );
    }
}

GraphPanel = connect(mapStateToProps, mapDispatchToProps)(GraphPanel)

export default GraphPanel;
