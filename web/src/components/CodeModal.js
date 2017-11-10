import React, {Component} from 'react';
import {Dialog, withStyles, Button} from "material-ui";
import Prism from 'prismjs';

const styles = theme => ({
    container: {
        margin: theme.spacing.unit * 2,
        overflow: "auto",
        wordBreak: "break-all",
        whiteSpace: "pre-wrap",
    },
    button: {
        display: "inline",
    }
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

        const content = this.props.code ? Prism.highlight(this.props.content, Prism.languages.javascript) : this.props.content;

        return (
            <span>
                <Button className={classes.button} color={this.props.contrast ? "contrast" : "default"}
                        onClick={this.handleClickOpen}>
                    {this.props.label}
                </Button>
                <Dialog fullWidth maxWidth="md" onRequestClose={this.handleRequestClose} open={this.state.open}>
                    <pre className={classes.container} dangerouslySetInnerHTML={{__html: content}}/>
                </Dialog>
            </span>
        );
    }
}

export default withStyles(styles)(CodeModal);
