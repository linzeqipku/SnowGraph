import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import * as reducers from './reducer';
import {applyMiddleware, combineReducers, compose, createStore} from "redux";
import {Provider} from 'react-redux';
import thunkMiddleware from 'redux-thunk';
import {createMuiTheme, MuiThemeProvider} from "material-ui";
import 'typeface-roboto'
import App from "./App";

const appReducer = combineReducers(reducers);

let store = createStore(appReducer, compose(applyMiddleware(thunkMiddleware), window.__REDUX_DEVTOOLS_EXTENSION__ && window.__REDUX_DEVTOOLS_EXTENSION__()));

const theme = createMuiTheme();

ReactDOM.render(
    <Provider store={store}>
        <MuiThemeProvider theme={theme}>
            <App/>
        </MuiThemeProvider>
    </Provider>,
    document.getElementById('root')
);
