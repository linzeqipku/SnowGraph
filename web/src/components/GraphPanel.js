import React, {Component} from 'react';
import {Card, CardBody, CardTitle} from "reactstrap";

class GraphPanel extends Component {
    render() {
        return (
            <Card>
                <CardBody>
                    <CardTitle>相关的代码结构子图</CardTitle>
                    <button id="first" className="btn btn-lg"></button>
                    <button id="former" className="btn btn-lg"></button>
                    <button id="latter" className="btn btn-lg"></button>
                    <button id="last" className="btn btn-lg"></button>
                    <div id="neo4jd3">
                    </div>
                </CardBody>
            </Card>
        );
    }
}

export default GraphPanel;
