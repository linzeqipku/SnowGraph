import React, {Component} from 'react';
import {fetchDocumentResult} from "../redux/action";
import {connect} from "react-redux";
import {Button, Input, withStyles} from "material-ui";
import CodeModal from "./CodeModal";

const mapStateToProps = (state) => ({
    question: state.question
})

const styles = theme => ({
    container: {
        margin: theme.spacing.unit * 2
    },
    form: {
        width: "95%"
    },
    search: {
        marginLeft: theme.spacing.unit * 2,
        marginRight: theme.spacing.unit * 2,
        width: "100%",
        flex: 1,
        color: theme.palette.common.white,
        '&:before': {
            backgroundColor: theme.palette.primary[400],
        },
        '&:hover:not(.disabled):before': {
            backgroundColor: theme.palette.primary[200],
        },
        '&:after': {
            backgroundColor: theme.palette.primary[50],
        },
    },
});

const mapDispatchToProps = {
    fetchDocumentResult: fetchDocumentResult
}

class SearchForm extends Component {
    state = {
        input: ""
    }

    componentDidMount() {
        this.setState({input: this.props.question["query"]});
    }

    componentWillReceiveProps(nextProps) {
        this.setState({input: nextProps.question["query"]});
    }

    handleSubmit = event => {
        event.preventDefault();
        this.props.fetchDocumentResult({query: this.state.input});
    }

    handleChange = event => {
        this.setState({input: event.target.value});
    }

    render() {
        const {classes} = this.props;

        return (
            <div>
                <form className={classes.form} onSubmit={this.handleSubmit}>
                    <Input className={classes.search} type="search" placeholder="Search"
                           value={this.state.input} onChange={this.handleChange}
                           multiline/>
                    <Button type="submit" color="contrast">Search</Button>
                    {this.props.question["query2"] &&
                    <CodeModal contrast label="Detail" content={this.props.question["query2"]}/>}
                </form>
            </div>

        );
    }
}

SearchForm = connect(mapStateToProps, mapDispatchToProps)(SearchForm)

export default withStyles(styles)(SearchForm);
