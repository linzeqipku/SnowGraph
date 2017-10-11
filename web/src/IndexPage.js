import React, {Component} from 'react';
import './IndexPage.css';

class IndexPage extends Component {
    render() {
        return (
            <div>
                <h1 className="display-3">SEI SnowGraph</h1>
                <p className="lead">This is a simple hero unit, a simple Jumbotron-style component for calling
                    extra attention to featured content or information.</p>
                <hr className="my-2"/>
                <p>It uses utility classes for typgraphy and spacing to space content out within the larger
                    container.</p>
                <p className="lead">
                </p>
            </div>
        );
    }
}

export default IndexPage;
