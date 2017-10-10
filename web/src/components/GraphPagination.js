import React, {Component} from 'react';
import {
    Button, Card, CardBody, CardTitle, Form, FormGroup, Input, Label, Pagination, PaginationItem,
    PaginationLink
} from "reactstrap";
import {connect} from "react-redux";
import {getNodeIDFromRelation, rename} from "../utils";
import {fetchNode, fetchRelationList, requestShowRelation} from "../action";


const mapStateToProps = (state) => {
    return {
        selectedNode: state.selectedNode,
        nodes: state.nodes,
        relationLists: state.relationLists,
        graph: state.graph
    }
}

const mapDispatchToProps = {
    fetchNode: fetchNode,
    fetchRelationList: fetchRelationList,
    requestShowRelation: requestShowRelation
}

class GraphPagination extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <Pagination>
                <PaginationItem>
                    <PaginationLink previous href="#" />
                </PaginationItem>
                <PaginationItem>
                    <PaginationLink href="#"> 1 </PaginationLink>
                </PaginationItem>
                <PaginationItem>
                    <PaginationLink href="#"> 2 </PaginationLink>
                </PaginationItem>
                <PaginationItem>
                    <PaginationLink href="#"> 3 </PaginationLink>
                </PaginationItem>
                <PaginationItem>
                    <PaginationLink href="#"> 4 </PaginationLink>
                </PaginationItem>
                <PaginationItem>
                    <PaginationLink href="#"> 5 </PaginationLink>
                </PaginationItem>
                <PaginationItem>
                    <PaginationLink next href="#" />
                </PaginationItem>
            </Pagination>

        );
    }
}

GraphPagination = connect(mapStateToProps, mapDispatchToProps)(GraphPagination)

export default GraphPagination;
