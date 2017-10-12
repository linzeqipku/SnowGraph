import {node2format, relation2format} from "./utils";

export const SELECT_NODE = 'SELECT_NODE';

export const REQUEST_NODE = 'REQUEST_NODE';
export const RECEIVED_NODE = 'RECEIVED_NODE';

export const REQUEST_RELATION_LIST = 'REQUEST_RELATION_LIST';
export const RECEIVED_RELATION_LIST = 'RECEIVED_RELATION_LIST';
export const REQUEST_SHOW_REALTION = 'REQUEST_SHOW_REALTION';
export const RECEIVED_SHOW_REALTION = 'RECEIVED_SHOW_REALTION';

export const REQUEST_GRAPH = 'REQUEST_GRAPH';
export const RECEIVED_GRAPH = 'RECEIVED_GRAPH';
export const DRAW_GRAPH = 'DRAW_GRAPH';

export const GOTO_INDEX = 'GOTO_INDEX';
import $ from 'jquery';

const URL = "http://localhost:8080";

export function selectNode(id) {
    return {type: SELECT_NODE, id};
}

export function requestNode(id) {
    return {type: REQUEST_NODE, id};
}

export function receivedNode(id, node) {
    return {type: RECEIVED_NODE, id, node};
}

export function fetchNode(id, nodes, graph) {
    return function (dispatch) {
        if (id in nodes) return;
        dispatch(requestNode(id));
        return $.post(`${URL}/GetNode`, {id}, result => {
            dispatch(receivedNode(id, result));
            const nodeJson = node2format(result);
            graph.updateWithNeo4jData({results: [{data: [{graph: {nodes: [nodeJson], relationships: []}}]}]});
            dispatch(receivedShowRelation(id, graph));
        });
    }
}

export function requestRelationList(id) {
    return {type: REQUEST_RELATION_LIST, id};
}

export function receivedRelationList(id, relationList) {
    return {type: RECEIVED_RELATION_LIST, id, relationList};
}

export function fetchRelationList(id, relationLists, graph) {
    return function (dispatch) {
        if (id in relationLists) return;
        dispatch(requestRelationList(id));
        return $.post(`${URL}/OutGoingRelation`, {id}, result => {
            dispatch(receivedRelationList(id, result));
            const relationsJson = result.map(relation2format);
            graph.updateWithNeo4jData({results: [{data: [{graph: {nodes: [], relationships: relationsJson}}]}]});
        });
    }
}

export function requestShowRelation(id, relationIndex, relation) {
    return {type: REQUEST_SHOW_REALTION, id, relationIndex, relation};
}

export function receivedShowRelation(id, graph) {
    return {type: RECEIVED_SHOW_REALTION, id, graph};
}

export function requestGraph() {
    return {type: REQUEST_GRAPH};
}

export function receivedGraph(result) {
    return {type: RECEIVED_GRAPH, result};
}

export function drawGraph(graph) {
    return {type: DRAW_GRAPH, graph};
}

export function fetchGraph(query) {
    return function (dispatch) {
        dispatch(requestGraph());
        $.post(`${URL}/CypherQuery`, {params: query})
            .done(result => dispatch(receivedGraph(result)))
            .fail(() => dispatch(receivedGraph(null)));
    }
}

export function gotoIndex() {
    return {type: GOTO_INDEX};
}

