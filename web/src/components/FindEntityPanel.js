import React, {Component} from 'react';
import {Button, Card, CardBody, CardTitle, Form, FormGroup, Input, Label} from "reactstrap";
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

class FindEntityPanel extends Component {
    constructor(props) {
        super(props);

        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleSubmit(event) {
        event.preventDefault();

        const catalog = this.input.value;
        const props = this.props;

        const updateRelations = [];
        const relationList = props.relationLists[props.selectedNode].relationList;

        if (relationList.filter(x => !x.shown).filter(x => x["raw"].type === catalog).length !== 0) {
            let remain = 5, i = 0;
            while (i < relationList.length && remain > 0) {
                const relation = relationList[i];
                ++i;
                if (relation.shown) continue;
                if (relation["raw"].type !== catalog) continue;
                props.requestShowRelation(this.props.selectedNode, i, relation["raw"]);
                --remain;
                updateRelations.push(relation);
            }
        }

        updateRelations.forEach((relation) => {
            const [startID, endID] = getNodeIDFromRelation(relation["raw"]);
            const otherID = startID === props.selectedNode ? endID : startID;
            props.fetchNode(otherID, props.nodes, props.graph);
            props.fetchRelationList(otherID, props.relationLists, props.graph);
        });

    }

    render() {
        let body = null;

        const selectedRelationList = this.props.relationLists[this.props.selectedNode];

        if (selectedRelationList && selectedRelationList.fetched) {
            const relationTypes = [...new Set(selectedRelationList.relationList.map(x => x["raw"].type))];
            body = <Form onSubmit={this.handleSubmit}>
                <FormGroup>
                    <Label for="relation-type">选择关联实体类型</Label>
                    <Input type="select" id="relation-type" innerRef={(input) => this.input = input}>
                        {relationTypes.map(t => <option key={t} value={t}>{rename(t)}</option>)}
                    </Input>
                </FormGroup>
                <Button>Submit</Button>
            </Form>;
        } else if (selectedRelationList) {
            body = <div>获取结点信息中...</div>;
        } else {
            body = <div>请先选择一个结点</div>;
        }

        return (
            <Card>
                <CardBody>
                    <CardTitle>查找关联实体</CardTitle>
                    {body}
                </CardBody>
            </Card>

        );
    }
}

FindEntityPanel = connect(mapStateToProps, mapDispatchToProps)(FindEntityPanel)

export default FindEntityPanel;
