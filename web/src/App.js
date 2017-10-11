import React, {Component} from 'react';
import GraphPanel from "./components/GraphPanel";
import FindEntityPanel from "./components/FindEntityPanel";
import InformationPanel from "./components/InformationPanel";
import SearchForm from "./components/SearchForm";
import {AppBar, Grid, Toolbar, Typography, withStyles} from "material-ui";

const styles = theme => ({
    brand: {
        marginRight: 20,
    },
});

class App extends Component {
    render() {
        return (
            <div>
                <AppBar color="primary" position="static">
                    <Toolbar>
                        <Typography className={this.props.classes.brand} type="title" color="inherit">
                            SEI SNOW Project
                        </Typography>
                        <SearchForm/>
                    </Toolbar>
                </AppBar>

                <Grid container spacing={0}>
                    <Grid item xs={7}>
                        <div className="py-md-3">
                            <GraphPanel/>
                        </div>
                    </Grid>
                    <Grid item xs={5}>
                        <div className="py-md-3">
                            <FindEntityPanel/>
                        </div>
                        <InformationPanel/>
                    </Grid>
                </Grid>
            </div>
        );
    }
}

App = withStyles(styles)(App);

export default withStyles(styles)(App);
