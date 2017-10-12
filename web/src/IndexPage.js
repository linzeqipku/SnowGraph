import React, {Component} from 'react';
import {Button, CircularProgress, Input, Typography, withStyles} from "material-ui";
import SearchIcon from 'material-ui-icons/Search'
import {fetchGraph, fetchNode, fetchRelationList, selectNode} from "./action";
import {connect} from "react-redux";

const styles = theme => ({
    container: {
        background: theme.palette.primary[500],
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        height: "100%",
        width: "100%",
        position: "absolute",
        top: 0,
        left: 0,
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
        width: "50%",
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
            <div elevation={0} className={classes.container}>
                <Typography component="h1" type="display4" className={classes.title}>SEI SnowGraph</Typography>
                <Typography component="h2" type="headline" className={classes.introduction}>
                    SnowGraph是一个软件项目知识图谱的生成框架，其功能/特征包括:
                </Typography>
                <ul className={classes.featureList}>
                    <li>
                        <Typography component="h3" type="body1" className={classes.introduction}>
                            以一个软件项目中多源异构的数据为输入，生成相应的知识图谱
                        </Typography>
                    </li>
                    <li>
                        <Typography component="h3" type="body1" className={classes.introduction}>
                            知识图谱的生成是高度模块化的，用户可以自由插装数据解析模块与知识提炼模块，从而灵活地定制所需的知识图谱
                        </Typography>
                    </li>
                    <li>
                        <Typography component="h3" type="body1" className={classes.introduction}>
                            我们为数据解析/知识提炼预留了公共接口，研究者可以将他们的知识提炼算法实现为SnowGraph中的一个模块，从而扩展已有的知识图谱
                        </Typography>
                    </li>
                </ul>
                <Typography component="h2" type="headline" className={classes.introduction}>开始使用:</Typography>
                <form className={classes.search} onSubmit={this.handleSubmit}>
                    <Input className={classes.searchInput} placeholder="Please enter your question..."
                           inputRef={input => this.input = input}/>
                    {this.props.graph.fetching ?
                        <CircularProgress color="accent" size={55}/> :
                        <Button fab type="submit" color="accent"><SearchIcon/></Button>}
                </form>
            </div>
        );
    }
}

IndexPage = connect(mapStateToProps, mapDispatchToProps)(IndexPage)

export default withStyles(styles)(IndexPage);
