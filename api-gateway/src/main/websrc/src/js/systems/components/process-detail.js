import React, { Component } from 'react';

export default class ProcessDetail extends Component {
    constructor(...args) {
        super(...args);
    }

    componentDidMount() {

    }

    loadDetail(uri) {
        console.info('loading process detail');
    }

    render() {
        return (
            <div>
            {this.props.toString()}
            </div>
        )
    }
}