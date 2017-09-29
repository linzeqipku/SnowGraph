import React, {Component} from 'react';

class InformationPanel extends Component {
	render() {
		return (
			<div className="panel panel-primary">
				<div className="panel-heading">
					<h3 className="panel-title">实体详细信息</h3>
				</div>
				<div className="panel panel-default">
					<table id="otherinfo" className="table">
					</table>
				</div>
				<div className="panel panel-default">
					<table id="data" className="table">
					</table>
				</div>
				<div className="panel panel-default">
					<table id="relation" className="table">
					</table>
				</div>
			</div>
		);
	}
}

export default InformationPanel;
