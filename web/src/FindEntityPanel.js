import React, {Component} from 'react';

class FindEntityPanel extends Component {
	render() {
		return (
			<div className="panel panel-primary">
				<div className="panel-heading">
					<h3 className="panel-title">查找关联实体</h3>
				</div>
				<div className="page-body">
					<div className="form-horizontal">
						<div className="control-label col-lg-0">
						</div>
						<div className="col-lg-2">
							<select id="relationSelect" className="form-control">
								<option>NONE</option>
							</select>
						</div>
					</div>
					<table id="relationTypes" className="table">
					</table>
				</div>
			</div>
		);
	}
}

export default FindEntityPanel;
