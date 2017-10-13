import React, {Component} from 'react';
import {Button, CircularProgress, Input, Typography, withStyles} from "material-ui";
import SearchIcon from 'material-ui-icons/Search'
import {fetchGraph, fetchNode, fetchRelationList, selectNode} from "../redux/action";
import {connect} from "react-redux";

const styles = theme => ({
    page: {
        background: theme.palette.primary[500],
        display: "flex",
        height: "100%",
        width: "100%",
        position: "absolute",
        alignItems: "center",
        justifyContent: "center",
        top: 0,
        left: 0,
    },
    container: {
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        height: "100%",
        width: "50%",
    },
    title: {
        color: theme.palette.common.white
    },
    introduction: {
        color: theme.palette.primary[50]
    },
    featureList: {
        color: theme.palette.primary[50]
    },
    search: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        width: "80%",
    },
    searchInput: {
        flex: 1,
        marginLeft: theme.spacing.unit * 2,
        marginRight: theme.spacing.unit * 2,
        color: theme.palette.primary[50],
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

const mapStateToProps = (state) => {
    return {
        graph: state.graph
    }
}

const mapDispatchToProps = {
    fetchNode: fetchNode,
    fetchRelationList: fetchRelationList,
    selectNode: selectNode,
    fetchGraph: fetchGraph
}

class IndexPage extends Component {
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
            <div elevation={0} className={classes.page}>
                <div elevation={0} className={classes.container}>
                    <Typography component="h1" type="display4" className={classes.title}>SEI SnowGraph</Typography>
                    <Typography component="h2" type="headline" className={classes.introduction}>
                        SnowGraph (Software Knowledge Graph) is a project for creating software-specific
                        question-answering
                        bot. Given a software project and various software engineering data of it, you can use SnowGraph
                        to:
                    </Typography>
                    <ul className={classes.featureList}>
                        <li>
                            <Typography component="h3" type="body1" className={classes.introduction}>
                                Creating a software-specific knowledge graph automatically. SnowGraph will extract
                                entities
                                from software engineering data, analyze relationships between them, and fuse them into a
                                uniform graph database. Software developers can access the software-specific knowledge
                                graph
                                through graphic user interface or graph query language.
                            </Typography>
                        </li>
                        <li>
                            <Typography component="h3" type="body1" className={classes.introduction}>
                                Creating a software-specific question answering bot automatically. Given a natural
                                language
                                user question about the software project, the QA bot can return passages from software
                                engineering data to answer the question.
                            </Typography>
                        </li>
                    </ul>
                    <Typography component="h2" type="headline" className={classes.introduction}>Get
                        Started:</Typography>
                    <form className={classes.search} onSubmit={this.handleSubmit}>
                        <Input className={classes.searchInput} placeholder="Please enter your question..."
                               inputRef={input => this.input = input}/>
                        {this.props.graph.fetching ?
                            <CircularProgress color="accent" size={55}/> :
                            <Button fab type="submit" color="accent"><SearchIcon/></Button>}
                    </form>
                </div>
            </div>
        );
    }
}

IndexPage = connect(mapStateToProps, mapDispatchToProps)(IndexPage)

export default withStyles(styles)(IndexPage);
