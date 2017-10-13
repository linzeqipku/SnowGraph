import React, {Component} from 'react';
import GraphPanel from "../components/GraphPanel";
import FindEntityPanel from "../components/FindEntityPanel";
import InformationPanel from "../components/InformationPanel";
import SearchForm from "../components/SearchForm";
import {AppBar, Grid, Toolbar, Typography, withStyles} from "material-ui";
import {gotoIndex} from "../redux/action";
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

class GraphTab extends Component {
    render() {
        const {classes} = this.props;
        return (
            <Grid style={{display: this.props.visibility ? "flex" : "none"}} container spacing={0}>
                <Grid item xs={8} className={classes.leftPanel}>
                    <GraphPanel/>
                </Grid>
                <Grid item xs={4} className={classes.rightPanel}>
                    <FindEntityPanel/>
                    <div className={classes.informationPanel}>
                        <InformationPanel/>
                    </div>
                </Grid>
            </Grid>
        );
    }
}

export default withStyles(styles)(GraphTab);
