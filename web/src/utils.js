import {translation} from "./translation";

export function rename(str) {
    if (translation.relationships[str] && translation.relationships[str]["englishName"])
        return translation.relationships[str].englishName;
    if (translation.in_relationships[str] && translation.in_relationships[str]["englishName"])
        return translation.in_relationships[str].englishName;
    if (translation.ou_relationships[str] && translation.ou_relationships[str]["englishName"])
        return translation.ou_relationships[str].englishName;
    if (str.substring(0,3) === "in_") return str.substring(3)+"(incoming)";
    if (str.substring(0,3) === "ou_") return str.substring(3)+"(outgoing)";
    return str;
}

export function getNodeIDFromRelation(relation) {
    const pattern = new RegExp('/db/data/node/(\\d+)');
    const startID = parseInt(pattern.exec(relation.start)[1], 10);
    const endID = parseInt(pattern.exec(relation.end)[1], 10);
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