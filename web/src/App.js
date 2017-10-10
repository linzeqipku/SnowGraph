import React, {Component} from 'react';
import GraphPanel from "./components/GraphPanel";
import FindEntityPanel from "./components/FindEntityPanel";
import InformationPanel from "./components/InformationPanel";
import {Col, Collapse, Container, Navbar, NavbarBrand, Row} from "reactstrap";
import SearchForm from "./components/SearchForm";
import './App.css';

class App extends Component {
    render() {
        return (
            <div>
                <Navbar className="Navbar" dark={true} color="dark">
                    <NavbarBrand href="/index.html"> SEI SNOW Project </NavbarBrand>
                    <Collapse className="w-75" isOpen={true}>
                        <SearchForm/>
                    </Collapse>
                </Navbar>

                <Container fluid={true}>
                    <Row>
                        <Col md="7">
                            <div className="py-md-3">
                                <GraphPanel/>
                            </div>
                        </Col>
                        <Col md="5">
                            <div className="py-md-3">
                                <FindEntityPanel/>
                            </div>
                            <InformationPanel/>
                        </Col>
                    </Row>

                </Container>
            </div>
        );
    }
}

export default App;
