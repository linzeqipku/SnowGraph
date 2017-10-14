import React, {Component} from 'react';
import {
    AppBar, Grid, LinearProgress, Table, TableBody, TableCell, TableHead, TableRow, Toolbar, Typography, withStyles
} from "material-ui";
import {fetchDocumentResult, gotoIndex} from "../redux/action";
import {connect} from "react-redux";

const styles = theme => ({
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
        const {classes, documentResult} = this.props;
        return (
            <div style={{display: this.props.visibility ? "block" : "none"}}>
                {documentResult.fetching && <LinearProgress/>}
                {documentResult.result != null && <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Rank</TableCell>
                            <TableCell>Title</TableCell>
                            <TableCell>Solr Rank</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {documentResult.result["rankedResults"].map(r => <TableRow key={r["answerId"]}>
                            <TableCell>{r["finalRank"]}</TableCell>
                            <TableCell>{r["title"]}</TableCell>
                            <TableCell>{r["solrRank"]}</TableCell>
                        </TableRow>)}
                    </TableBody>
                </Table>
                }
            </div>
        );
    }
}

DocumentTab = connect(mapStateToProps, mapDispatchToProps)(DocumentTab);

export default withStyles(styles)(DocumentTab);
