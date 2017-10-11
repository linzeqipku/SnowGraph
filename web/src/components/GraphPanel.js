import React, {Component} from 'react';
import {Card, CardContent, Typography} from "material-ui";

class GraphPanel extends Component {
    render() {
        return (
            <Card>
                <CardContent>
                    <Typography type="headline" component="h2"> 相关的代码结构子图 </Typography>
                    <div style={{height: 800}} id="neo4jd3"/>
                </CardContent>
            </Card>
        );
    }
}

export default GraphPanel;
