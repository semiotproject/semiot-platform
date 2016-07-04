import React, { Component } from 'react';

export default class SensorDetail extends Component {
    constructor(...args) {
        super(...args);
    }

    componentDidMount() {

    }

    loadDetail(uri) {
        console.info('loading sensor detail');
    }

    render() {
        return (
            <div>
            {this.props.toString()}
            </div>
        )
    }
}