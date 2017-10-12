import React, {Component} from 'react';
import {fetchGraph} from "../action";
import {connect} from "react-redux";
import './SearchForm.css';
import {Input, withStyles} from "material-ui";

const mapStateToProps = (state) => ({})

const styles = theme => ({
    form: {
        width: "75%"
    },
    search: {
        marginLeft: theme.spacing.unit * 2,
        marginRight: theme.spacing.unit * 2,
        width: "100%",
        color: theme.palette.primary[100],
        '&:before': {
            backgroundColor: theme.palette.primary[400],
        },
        '&:hover:not(.disabled):before': {
            backgroundColor: theme.palette.primary[200],
        },
        '&:after': {
            backgroundColor: theme.palette.primary[50],
        },
    }
});

const mapDispatchToProps = {
    fetchGraph: fetchGraph
}

class SearchForm extends Component {
    constructor(props) {
        super(props);

        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleSubmit(event) {
        event.preventDefault();
        this.props.fetchGraph(this.input.value);
    }

    render() {
        const {classes} = this.props;

        return (
            <form className={classes.form} onSubmit={this.handleSubmit}>
                <Input className={classes.search} type="search" placeholder="Search" inputRef={input => this.input = input}/>
            </form>
        );
    }
}

SearchForm = connect(mapStateToProps, mapDispatchToProps)(SearchForm)

export default withStyles(styles)(SearchForm);
