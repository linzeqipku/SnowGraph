import React, {Component} from 'react';
import {withStyles} from "material-ui";
import {connect} from "react-redux";
import SearchPage from "./ResultPage";
import IndexPage from "./IndexPage";

const styles = theme => ({});

const mapStateToProps = (state) => {
    return {
        showIndex: state.graph.state === "fetching" || state.graph.state === "none"
    }
}

class App extends Component {
    render() {
        return this.props.showIndex ? <IndexPage/> : <SearchPage/>
    }
}

App = connect(mapStateToProps)(App)

export default withStyles(styles)(App);
