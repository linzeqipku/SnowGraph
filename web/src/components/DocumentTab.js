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

class DocumentTab extends Component {
    render() {
        const {classes} = this.props;
        return (
            <div style={{display: this.props.visibility ? "block" : "none"}}>Document Tab</div>
        );
    }
}

export default withStyles(styles)(DocumentTab);
