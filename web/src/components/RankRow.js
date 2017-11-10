import React, {Component} from 'react';
import {TableCell, TableRow, withStyles} from "material-ui";
import ExpandMoreIcon from 'material-ui-icons/ExpandMore'
import ExpandLessIcon from 'material-ui-icons/ExpandLess'
import {connect} from "react-redux";

const styles = theme => ({
    detail: {
        borderLeft: "0.25rem",
        borderLeftStyle: "solid",
        borderLeftColor: theme.palette.secondary[500],
        paddingLeft: theme.spacing.unit
    },
    cellRank: {
        width: "8%"
    },
    cellMain: {
        width: "84%",
        overflowWrap: "normal",
        whiteSpace: "normal"
    },
    highlight: {
        background: theme.palette.primary[50]
    }
});

const mapStateToProps = (state) => {
    return {
        question: state.question,
        documentResult: state.documentResult,
    }
}

const mapDispatchToProps = {}

class RankRow extends Component {
    state = {
        expand: false
    };

    constructor(props) {
        super(props);
        this.handleExpandMore = this.handleExpandMore.bind(this);
        this.handleExpandLess = this.handleExpandLess.bind(this);
    }

    handleExpandMore() {
        this.setState({expand: true});
    }

    handleExpandLess() {
        this.setState({expand: false});
    }

    render() {
        const {classes, rank, title, solrRank, detail, highlight} = this.props;
        let delta = (solrRank - rank).toString();
        if (delta[0] !== '-') delta = `+${delta}`;
        const color = solrRank < rank ? "green" : (solrRank > rank ? "red" : "black");
        return (
            <TableRow style={highlight ? {background: "#EEEEEE"} : {}}>
                <TableCell className={classes.cellRank}>{rank}<span style={{color: color}}>{` (${delta})`}</span></TableCell>
                <TableCell className={classes.cellMain}>
                    {title}
                    {!this.state.expand && <ExpandMoreIcon onClick={this.handleExpandMore}/>}
                    {this.state.expand && <ExpandLessIcon onClick={this.handleExpandLess}/>}
                    {this.state.expand && <div className={classes.detail} dangerouslySetInnerHTML={{__html: detail}}/>}
                </TableCell>
                <TableCell className={classes.cellRank}>{solrRank}</TableCell>
            </TableRow>
        );
    }
}

RankRow = connect(mapStateToProps, mapDispatchToProps)(RankRow);

export default withStyles(styles)(RankRow);
