import React, {Component} from 'react';
import {Typography, Dialog, withStyles} from "material-ui";

const styles = theme => ({
    container: {
        margin: theme.spacing.unit * 2,
        overflow: "auto",
        wordBreak: "break-all",
        whiteSpace: "pre-wrap"
    },
});

class CodeModal extends Component {
    state = {
        open: false
    };

    handleClickOpen = () => {
        this.setState({open: true});
    };

    handleRequestClose = () => {
        this.setState({open: false});
    };

    render() {
        const {classes} = this.props;

        return (
            <div>
                <Typography onClick={this.handleClickOpen} component="a" href="#">SHOW</Typography>
                <Dialog onRequestClose={this.handleRequestClose} open={this.state.open}>
                    <pre className={classes.container} dangerouslySetInnerHTML={{__html: this.props.content}}/>
                </Dialog>
            </div>
        );
    }
}

export default withStyles(styles)(CodeModal);
