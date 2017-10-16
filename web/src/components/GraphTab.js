import React, {Component} from 'react';
import GraphPanel from "../components/GraphPanel";
import FindEntityPanel from "../components/FindEntityPanel";
import InformationPanel from "../components/InformationPanel";
import {Grid, LinearProgress, withStyles} from "material-ui";
import {connect} from "react-redux";

const styles = theme => ({
    leftPanel: {
        padding: theme.spacing.unit * 2,
    },
    rightPanel: {
        paddingTop: theme.spacing.unit * 2,
        paddingRight: theme.spacing.unit * 2,
    },
    informationPanel: {
        marginTop: theme.spacing.unit * 2,
    }
});

const mapStateToProps = (state) => {
    return {
        fetchingGraph: state.graph.fetching,
    }
}

class GraphTab extends Component {
    render() {
        const {classes} = this.props;
        const show = <Grid style={{display: this.props.visibility ? "flex" : "none"}} container spacing={0}>
            <Grid item xs={8} className={classes.leftPanel}>
                <GraphPanel/>
            </Grid>
            <Grid item xs={4} className={classes.rightPanel}>
                <FindEntityPanel/>
                <div className={classes.informationPanel}>
                    <InformationPanel/>
                </div>
            </Grid>
        </Grid>;
        const notShow = <Grid style={{display: this.props.visibility ? "flex" : "none"}} container spacing={0}>
            <LinearProgress/>
        </Grid>;
        return this.props.fetchingGraph ? notShow : show;
    }
}

GraphTab = connect(mapStateToProps)(GraphTab)

export default withStyles(styles)(GraphTab);
