import React, {Component} from 'react';

class GraphPanel extends Component {
	render() {
		return (
			<div className="panel panel-primary">
				<div className="panel-heading">
					<h3 className="panel-title">相关的代码结构子图</h3>
				</div>
				<div className="panel-body">
					<button id="first" className="btn btn-lg"></button>
					<button id="former" className="btn btn-lg"></button>
					<button id="latter" className="btn btn-lg"></button>
					<button id="last" className="btn btn-lg"></button>
				</div>
				<div id="neo4jd3">
				</div>
			</div>
		);
	}
}

export default GraphPanel;
