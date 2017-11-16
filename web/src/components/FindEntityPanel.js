import React, {Component} from 'react';
import {connect} from "react-redux";
import {getNodeIDFromRelation, rename} from "../utils";
import {fetchNode, fetchRelationList, requestShowRelation} from "../redux/action";
import {
    Button, Card, CardContent, CardHeader, FormControl,
    Input, InputLabel, LinearProgress, Select, Typography, withStyles
} from "material-ui";

const styles = theme => ({
    formControl: {
        margin: theme.spacing.unit,
        minWidth: 240,
    }
});

const mapStateToProps = (state) => {
    return {
        selectedNode: state.selectedNode,
        nodes: state.nodes,
        relationLists: state.relationLists,
        graph: state.graph.instance
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
            body = <form onSubmit={this.handleSubmit}>
                <FormControl className={this.props.classes.formControl}>
                    <InputLabel htmlFor="relation-type">Relation Type</InputLabel>
                    <Select native input={<Input id="relation-type" inputRef={(input) => this.input = input}/>}>
                        {relationTypes.map(t => <option key={t} value={t}>{rename(t)}</option>)}
                    </Select>
                </FormControl>
                <Button type="submit">Submit</Button>
            </form>;
        } else if (selectedRelationList) {
            body = <LinearProgress/>;
        } else {
            body = <Typography component="p"> Please select a node first </Typography>;
        }

        return (
            <Card>
                <CardHeader title="Expand Related Entity"/>
                <CardContent>
                    {body}
                </CardContent>
            </Card>

        );
    }
}

FindEntityPanel = connect(mapStateToProps, mapDispatchToProps)(FindEntityPanel)

export default withStyles(styles)(FindEntityPanel);
