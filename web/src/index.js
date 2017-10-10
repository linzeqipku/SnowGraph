import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from "./App";
import * as reducers from './reducer';
import {applyMiddleware, combineReducers, compose, createStore} from "redux";
import {Provider} from 'react-redux'
import thunkMiddleware from 'redux-thunk'

const appReducer = combineReducers(reducers);

let store = createStore(appReducer, compose(applyMiddleware(thunkMiddleware), window.__REDUX_DEVTOOLS_EXTENSION__ && window.__REDUX_DEVTOOLS_EXTENSION__()));

ReactDOM.render(
    <Provider store={store}>
        <App/>
    </Provider>,
    document.getElementById('root')
);
