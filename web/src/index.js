import React from 'react';
import ReactDOM from 'react-dom';
import GraphPanel from './GraphPanel';
import FindEntityPanel from './FindEntityPanel';
import InformationPanel from "./InformationPanel";
import './index.css';
import SearchForm from "./SearchForm";

ReactDOM.render(
	<GraphPanel />,
	document.getElementById('graph-panel')
);

ReactDOM.render(
	<FindEntityPanel />,
	document.getElementById('find-entity-panel')
);

ReactDOM.render(
	<InformationPanel />,
	document.getElementById('information-panel')
);

ReactDOM.render(
	<SearchForm />,
	document.getElementById('search-form')
);
