import React, {Component} from 'react';
import GraphPanel from "./components/GraphPanel";
import FindEntityPanel from "./components/FindEntityPanel";
import InformationPanel from "./components/InformationPanel";
import SearchForm from "./components/SearchForm";
import {AppBar, Grid, Toolbar, Typography, withStyles} from "material-ui";
import {gotoIndex} from "./action";
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
    },
    brand: {
        textDecoration: "none"
    }
});

const mapStateToProps = (state) => {
    return {}
}

const mapDispatchToProps = {
    gotoIndex: gotoIndex
}

class GraphPage extends Component {
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

                <Grid container spacing={0}>
                    <Grid item xs={7} className={classes.leftPanel}>
                        <GraphPanel/>
                    </Grid>
                    <Grid item xs={5} className={classes.rightPanel}>
                        <FindEntityPanel/>
                        <div className={classes.informationPanel}>
                            <InformationPanel/>
                        </div>
                    </Grid>
                </Grid>
            </div>
        );
    }
}

GraphPage = connect(mapStateToProps, mapDispatchToProps)(GraphPage);

export default withStyles(styles)(GraphPage);
