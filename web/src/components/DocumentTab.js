import React, {Component} from 'react';
import {
    AppBar,
    Grid,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    Toolbar,
    Typography,
    withStyles
} from "material-ui";
import {gotoIndex} from "../redux/action";
import {connect} from "react-redux";

const styles = theme => ({
    leftPanel: {
        padding: theme.spacing.unit * 2,
    },
    rightPanel: {
        paddingTop: theme.spacing.unit * 2,
        paddingRight: theme.spacing.unit * 2,
    },
    informationPanel: {
        marginTop: theme.spacing.unit * 2,
    }
});

class DocumentTab extends Component {
    render() {
        const {classes} = this.props;
        return (
            <div style={{display: this.props.visibility ? "block" : "none"}}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Rank</TableCell>
                            <TableCell>Title</TableCell>
                            <TableCell>Solr Rank</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                    </TableBody>
                </Table>
            </div>
        );
    }
}

export default withStyles(styles)(DocumentTab);
