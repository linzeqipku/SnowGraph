import {
    RECEIVED_NODE, RECEIVED_RELATION_LIST, REQUEST_NODE, REQUEST_RELATION_LIST,
    SELECT_NODE, REQUEST_SHOW_REALTION,
    UPDATE_GRAPH, RECEIVED_SHOW_REALTION, REQUEST_NEW_GRAPH
} from "./action";
import {cloneDeep} from "lodash";
import {getNodeIDFromRelation, relation2format} from "./utils";

export function selectedNode(state = null, action) {
    switch (action.type) {
        case REQUEST_NEW_GRAPH:
            return null;
        case SELECT_NODE:
            return action.id;
        default:
            return state;
    }
}

export function nodes(state = {}, action) {
    switch (action.type) {
        case REQUEST_NEW_GRAPH:
            return {};
        case REQUEST_NODE:
            return Object.assign({}, state, {
                [action.id]: {
                    fetched: false,
                    node: null
                }
            });
        case RECEIVED_NODE:
            return Object.assign({}, state, {
                [action.id]: {
                    fetched: true,
                    node: action.node
                }
            });
        default:
            return state;
    }
}

export function relationLists(state = {}, action) {
    switch (action.type) {
        case REQUEST_NEW_GRAPH:
            return {};
        case REQUEST_RELATION_LIST:
            return Object.assign({}, state, {
                [action.id]: {
                    fetched: false,
                    node: null
                }
            });
        case RECEIVED_RELATION_LIST:
            return Object.assign({}, state, {
                [action.id]: {
                    fetched: true,
                    relationList: action.relationList.map(r => ({shown: false, raw: r}))
                }
            });
        case REQUEST_SHOW_REALTION: {
            const newState = cloneDeep(state);
            newState[action.id].relationList[action.relationIndex].shown = true;
            return newState;
        }
        default:
            return state;
    }
}

export function waitingRelationLists(state = {}, action) {
    switch (action.type) {
        case REQUEST_NEW_GRAPH:
            return {};
        case REQUEST_SHOW_REALTION:
            const relation = action.relation
            const [startID, endID] = getNodeIDFromRelation(relation);
            const otherID = startID === action.id ? endID : startID;
            let oldList = state[otherID];
            if (!oldList) oldList = [];
            return Object.assign({}, state, {
                [otherID]: [...oldList, action.relation]
            });
        case RECEIVED_SHOW_REALTION:
            const relationList = state[action.id];
            if (!relationList) return state;
            const relationsJson = relationList.map(relation2format);
            action.graph.updateWithNeo4jData({results: [{data: [{graph: {nodes: [], relationships: relationsJson}}]}]});
            return Object.assign({}, state, {
                [action.id]: []
            });
        default:
            return state;
    }
}

export function graph(state = null, action) {
    switch (action.type) {
        case REQUEST_NEW_GRAPH:
            return null;
        case UPDATE_GRAPH:
            return action.graph;
        default:
            return state;
    }
}