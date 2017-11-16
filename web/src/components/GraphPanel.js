import React, {Component} from 'react';
import {Card, CardContent, CardHeader} from "material-ui";
import {Neo4jD3} from '../neo4jd3.js';
import {drawGraph, fetchNode, fetchRelationList, selectNode} from "../redux/action";
import {connect} from "react-redux";
import {translation} from "../translation";

const mapStateToProps = (state) => {
    return {
        nodes: state.nodes,
        relationLists: state.relationLists,
        graph: state.graph.instance,
        result: state.graph.result,
        fetchingGraph: state.graph.fetching,
        redraw: state.graph.toBeDrawn
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

    updateD3() {
        const neo4jd3 = new Neo4jD3('#neo4jd3', {
            showClassChnName: false,
            classes: translation.classes,
            showRlationshipsChnName: false,
            relationships: translation.relationships,
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
            minCollision: 60,
            neo4jData: this.props.result["searchResult"],
            nodeRadius: 40,
            onNodeClick: (node) => {
                this.props.fetchNode(node.id, this.getNodes(), this.getGraph());
                this.props.fetchRelationList(node.id, this.getRelationLists(), this.getGraph());
                this.props.selectNode(node.id);
            },
            onNodeDoubleClick: (node) => {
                neo4jd3.removeNode(node);
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

    componentDidMount() {
        if (this.props.redraw) this.updateD3();
    }

    componentDidUpdate() {
        if (this.props.redraw) this.updateD3();
    }

    render() {
        return (
            <Card>
                <CardHeader title="Related API Code Graph"/>
                <CardContent>
                    {!this.props.fetchingGraph && <div style={{height: 800}} id="neo4jd3"/>}
                </CardContent>
            </Card>
        );
    }
}

GraphPanel = connect(mapStateToProps, mapDispatchToProps)(GraphPanel)

export default GraphPanel;
