import React, {Component} from 'react';
import {Button, CircularProgress, Input, Typography, withStyles} from "material-ui";
import SearchIcon from 'material-ui-icons/Search'
import HelpIcon from 'material-ui-icons/Help'
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
        width: "100%",
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
    },
    help: {
        margin: theme.spacing.unit * 2
    },
    helpText: {
        color: "inherit"
    },
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

const queries = [
    {
        "query": "has anyone investigated theoretically whether surface flexibility can stabilize a laminar boundary layer.",
        "id": "205"
    },
    {
        "query": "what similarity laws must be obeyed when constructing aeroelastic models of heated high speed aircraft.",
        "id": "1"
    },
    {"query": "has anyone programmed a pump design method for a high-speed digital computer.", "id": "128"},
    {"query": "papers dealing with uniformly loaded sectors.", "id": "192"},
    {"query": "what are the experimental results for the creep buckling of columns.", "id": "135"},
    {
        "query": "do the discrepancies among current analyses of the vorticity effect on stagnation-point heat transfer result primarily from the differences in the viscosity-temperature law assumed.",
        "id": "75"
    },
    {"query": "has anyone investigated the shear buckling of stiffened plates.", "id": "222"},
    {
        "query": "has anyone formally determined the influence of joule heating,  produced by the induced current,  in magnetohydrodynamic free convection flows under general conditions.",
        "id": "20"
    },
    {
        "query": "how do kuchemann's and multhopp's methods for calculating lift distributions on swept wings in subsonic flow compare with each other and with experiment.",
        "id": "82"
    },
    {
        "query": "what is the effect on cylinder buckling of a circumferential stress system that varies in the axial direction.",
        "id": "213"
    },
    {
        "query": "what are the factors which influence the time required to invert large structural matrices.",
        "id": "24"
    },
    {
        "query": "does a membrane theory exist by which the behaviour of pressurized membrane cylinders in bending can be predicted.",
        "id": "146"
    },
    {
        "query": "what is the effect of cross sectional shape on the flow over simple delta wings with sharp leading edges.",
        "id": "29"
    },
    {
        "query": "has anyone analytically investigated the stabilizing influence of soft elastic cores on the buckling strength of cylindrical shells subjected to non-uniform external pressure.",
        "id": "210"
    },
    {
        "query": "what is the best theoretical method for calculating pressure on the surface of a wing alone.",
        "id": "151"
    },
    {
        "query": "why does the incremental theory and the deformation theory of plastic stress-strain relationship differ greatly when applied to stability problems.",
        "id": "101"
    },
    {"query": "work on flow in channels at low reynolds numbers.", "id": "175"},
    {
        "query": "given that an uncontrolled vehicle will tumble as it enters an atmosphere, is it possible to predict when and how it will stop tumbling and its subsequent motion.",
        "id": "99"
    },
    {
        "query": "references on lyapunov's method on the stability of linear differential equations with periodic coefficients.",
        "id": "173"
    },
    {"query": "how can one detect transition phenomena in hypersonic wakes.", "id": "40"},
    {"query": "panels subjected to aerodynamic heating.", "id": "109"},
    {"query": "how can wing-body,  flow field interference effects be approximated rationally.", "id": "186"},
    {
        "query": "what are the aerodynamic interference effects on the fin lift and body lift of a fin-body combination.",
        "id": "118"
    },
    {"query": "what controls leading-edge attachment at transonic speeds.", "id": "48"},
    {
        "query": "what is the magnitude and distribution of lift over the cone and the cylindrical portion of a cone-cylinder configuration.",
        "id": "116"
    },
    {
        "query": "has the solution of the clamped plate problem,  in the classical theory of bending,  been reduced to two successive membrane boundary value problems.",
        "id": "112"
    },
    {
        "query": "effects of leading-edge bluntness on the flutter characteristics of some square-planform double-wedge airfoils at mach numbers less than 15.4.",
        "id": "182"
    },
    {
        "query": "how far around a cylinder and under what conditions of flow,  if any, is the velocity just outside of the boundary layer a linear function of the distance around the cylinder.",
        "id": "62"
    }
]

class IndexPage extends Component {
    constructor(props) {
        super(props);

        this.handleSubmit = this.handleSubmit.bind(this);
        this.generateRandomQuestion = this.generateRandomQuestion.bind(this);
    }

    handleSubmit(event) {
        event.preventDefault();
        this.props.fetchGraph(this.input.value);
    }

    generateRandomQuestion() {
        this.input.value = queries[Math.floor(Math.random()) % queries.length].query;
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
                    <Typography component="h2" type="headline" className={classes.introduction}>
                        Get Started:
                    </Typography>
                    <form className={classes.search} onSubmit={this.handleSubmit}>
                        <Input className={classes.searchInput} placeholder="Please enter your question..."
                               inputRef={input => this.input = input}/>
                        {this.props.graph.fetching ?
                            <CircularProgress color="accent" size={55}/> :
                            <Button fab type="submit" color="accent"><SearchIcon/></Button>}
                        <Button className={classes.help} fab type="button" color="accent"
                                onClick={this.generateRandomQuestion}>
                            <Typography className={classes.helpText} component="h2" type="headline">?</Typography>
                        </Button>
                    </form>
                </div>
            </div>
        );
    }
}

IndexPage = connect(mapStateToProps, mapDispatchToProps)(IndexPage)

export default withStyles(styles)(IndexPage);
