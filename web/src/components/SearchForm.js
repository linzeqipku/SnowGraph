import React, {Component} from 'react';
import {fetchGraph} from "../redux/action";
import {connect} from "react-redux";
import {Card, CardContent, CardHeader, Input, withStyles} from "material-ui";
import CodeModal from "./CodeModal";

const mapStateToProps = (state) => ({
    question: state.question,
    richQuestion: state.richQuestion
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
            <Card className={classes.container}>
                <CardHeader title="Question"/>
                <CardContent>
                    <form className={classes.form} onSubmit={this.handleSubmit}>
                        <Input className={classes.search} type="search" placeholder="Search" value={this.props.question}
                               inputRef={input => this.input = input} multiline/>
                    </form>
                    <CodeModal content={this.props.richQuestion}/>
                </CardContent>
            </Card>

        );
    }
}

SearchForm = connect(mapStateToProps, mapDispatchToProps)(SearchForm)

export default withStyles(styles)(SearchForm);
