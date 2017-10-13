import React, {Component} from 'react';
import {withStyles} from "material-ui";
import {connect} from "react-redux";
import GraphPage from "./pages/ResultPage";
import IndexPage from "./pages/IndexPage";

const styles = theme => ({});

const mapStateToProps = (state) => {
    return {
        page: state.page
    }
}

class App extends Component {
    render() {
        return this.props.page === "index" ? <IndexPage/> : <GraphPage/>
    }
}

App = connect(mapStateToProps)(App)

export default withStyles(styles)(App);
