import React, {Component} from 'react';
import SearchForm from "../components/SearchForm";
import {AppBar, Grid, Tabs, Tab, Toolbar, Typography, withStyles} from "material-ui";
import {changeTab, gotoIndex} from "../redux/action";
import {connect} from "react-redux";
import GraphTab from "../components/GraphTab";
import DocumentTab from "../components/DocumentTab";

const styles = theme => ({
    brand: {
        textDecoration: "none"
    }
});

const mapStateToProps = (state) => {
    return {
        tab: state.tab
    }
}

const mapDispatchToProps = {
    gotoIndex: gotoIndex,
    changeTab: changeTab
}

class ResultPage extends Component {
    render() {
        const {classes} = this.props;
        return (
            <div>
                <AppBar color="primary" position="static">
                    <Toolbar>
                        <Typography className={classes.brand} href="#" type="title" color="inherit" component="a"
                                    onClick={this.props.gotoIndex}>
                            SEI SNOW Project
                        </Typography>
                        <SearchForm/>
                    </Toolbar>
                </AppBar>

                <Tabs value={this.props.tab} onChange={(e, v) => this.props.changeTab(v)}>
                    <Tab value="document" label="Document"/>
                    <Tab value="api-graph" label="API Graph"/>
                </Tabs>

                <DocumentTab visibility={this.props.tab === "document"}/>
                <GraphTab visibility={this.props.tab === "api-graph"}/>
            </div>
        );
    }
}

ResultPage = connect(mapStateToProps, mapDispatchToProps)(ResultPage);

export default withStyles(styles)(ResultPage);
