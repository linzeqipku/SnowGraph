import {node2format, relation2format} from "./utils";

export const REQUEST_NEW_GRAPH = 'REQUEST_NEW_GRAPH';

export const SELECT_NODE = 'SELECT_NODE';

export const REQUEST_NODE = 'REQUEST_NODE';
export const RECEIVED_NODE = 'RECEIVED_NODE';

export const REQUEST_RELATION_LIST = 'REQUEST_RELATION_LIST';
export const RECEIVED_RELATION_LIST = 'RECEIVED_RELATION_LIST';
export const REQUEST_SHOW_REALTION = 'REQUEST_SHOW_REALTION';
export const RECEIVED_SHOW_REALTION = 'RECEIVED_SHOW_REALTION';
export const UPDATE_GRAPH = 'UPDATE_GRAPH';
import $ from 'jquery';

const URL = "http://localhost:8080";

export function requestNewGraph() {
    return {type: REQUEST_NEW_GRAPH};
}

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

export function updateGraph(graph) {
    return {type: UPDATE_GRAPH, graph};
}

