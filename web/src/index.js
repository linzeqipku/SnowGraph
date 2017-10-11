import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from "./App";
import * as reducers from './reducer';
import {applyMiddleware, combineReducers, compose, createStore} from "redux";
import {Provider} from 'react-redux';
import thunkMiddleware from 'redux-thunk';
import {BrowserRouter, Route} from "react-router-dom";
import {createMuiTheme, MuiThemeProvider} from "material-ui";
import IndexPage from "./IndexPage";

const appReducer = combineReducers(reducers);

let store = createStore(appReducer, compose(applyMiddleware(thunkMiddleware), window.__REDUX_DEVTOOLS_EXTENSION__ && window.__REDUX_DEVTOOLS_EXTENSION__()));

const theme = createMuiTheme();

ReactDOM.render(
    <Provider store={store}>
        <BrowserRouter>
            <MuiThemeProvider theme={theme}>
                <div>
                    <Route exact={true} path="/" component={IndexPage}/>
                    <Route path="/search" component={App}/>
                </div>
            </MuiThemeProvider>
        </BrowserRouter>
    </Provider>,
    document.getElementById('root')
);
