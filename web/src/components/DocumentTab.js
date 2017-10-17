import React, {Component} from 'react';
import {LinearProgress, Table, TableBody, TableCell, TableHead, TableRow, withStyles} from "material-ui";
import {fetchDocumentResult} from "../redux/action";
import {connect} from "react-redux";
import RankRow from "./RankRow";

const styles = theme => ({
    container: {
        justifyContent: "center",
    },
    table: {
        width: "70%",
    },
    progress: {
        flexGrow: 1,
        margin: theme.spacing.unit * 4
    }
});

const mapStateToProps = (state) => {
    return {
        question: state.question,
        documentResult: state.documentResult,
    }
}

const mapDispatchToProps = {
    fetchDocumentResult: fetchDocumentResult,
}

class DocumentTab extends Component {

    componentDidMount() {
        this.props.fetchDocumentResult(this.props.question);
    }

    render() {
        const {classes, documentResult, question} = this.props;
        return (
            <div style={{display: this.props.visibility ? "flex" : "none"}} className={classes.container}>
                {documentResult.fetching && <LinearProgress className={classes.progress}/>}
                {documentResult.result != null && <Table className={classes.table}>
                    <TableHead>
                        <TableRow>
                            <TableCell>Rank</TableCell>
                            <TableCell>Title</TableCell>
                            <TableCell>Solr Rank</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {documentResult.result["rankedResults"].map(r => {
                            return <RankRow
                                key={r["answerId"]} rank={r["finalRank"]} title={r["title"]}
                                solrRank={r["solrRank"]}
                                detail={r["body"]} highlight={r["answerId"] === question["answerId"]}
                            />;
                        })}
                    </TableBody>
                </Table>
                }
            </div>
        );
    }
}

DocumentTab = connect(mapStateToProps, mapDispatchToProps)(DocumentTab);

export default withStyles(styles)(DocumentTab);
