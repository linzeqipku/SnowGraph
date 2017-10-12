import React, {Component} from 'react';
import GraphPanel from "./components/GraphPanel";
import FindEntityPanel from "./components/FindEntityPanel";
import InformationPanel from "./components/InformationPanel";
import SearchForm from "./components/SearchForm";
import {AppBar, Grid, Toolbar, Typography, withStyles} from "material-ui";

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

class ResultPage extends Component {
    render() {
        const {classes} = this.props;
        return (
            <div>
                <AppBar color="primary" position="static">
                    <Toolbar>
                        <Typography type="title" color="inherit">
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

export default withStyles(styles)(ResultPage);
