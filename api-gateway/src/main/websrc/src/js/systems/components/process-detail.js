import React, { Component } from 'react';
import moment from 'moment';
import utils from '../utils/common';
import WAMP from '../services/wamp';

import processAPI from '../api/processes';

export default class ProcessDetail extends Component {
    constructor(...args) {
        super(...args);

        this.state = {
            label: "",
            isLoading: true,
            operations: [],
            lastCommandResult: null,
            currentOperation: null,
            customOperationParams: []
        }

        this.handlePerformOperation = (operation) => {
            return () => {
                console.info(`performing operation `, operation);
                this.hasAdditionalParams(operation) ?
                    this.showDialogForOperation(operation) :
                    processAPI.performOperation(operation);
            };
        };
    }

    componentDidMount() {
        this.loadDetail(this.props.uri);
    }
    componentWillUnmount() {
        this.unsubscribe();
    }

    performOperationWithParams() {
        const { currentOperation,customOperationParams } = this.state;
        customOperationParams.forEach((p) => {
            const value = this.refs[p.label].value;
            const param = currentOperation.body['dul:hasParameter'].forEach((c, i) => {
                if (c['sh:property']['sh:name']['@value'] === p.label) {
                    currentOperation.body['dul:hasParameter'][i]['dul:hasParameterDataValue'] = value;
                }
            });
        });
        processAPI.performOperation(currentOperation);
        $('#process-modal').modal('hide');
    }
    hasAdditionalParams(operation) {
        return operation.body['dul:hasParameter'];
    }
    showDialogForOperation(operation) {
        console.info("showing operation dialog");
        this.setState({
            customOperationParams: this.getCustomOperationParameters(operation),
            currentOperation: operation
        }, () => {
            $('#process-modal').modal({});
        });
    }
    getCustomOperationParameters(operation) {
        return operation.body['dul:hasParameter'].map((p) => {
            return {
                label: p['sh:property']['sh:name']['@value'],
                defaultValue: p['sh:property']['sh:defaultValue']
            }
        });
    }

    loadDetail(uri) {
        console.info('loading process detail');
        processAPI.loadProcessInformation(uri).then((process) => {
            this.setState({
                label: process.id,
                operations: process.operations,
                lastCommandResult: process.result,
                wamp: process.wamp
            }, () => {
                this.subscribe();
            });
        });
    }
    subscribe() {
        const { wamp } = this.state;
        if (wamp && wamp.topic && wamp.endpoint) {
            console.info("subscribing to process..");
            processAPI.subscribe(wamp.endpoint, wamp.topic, (data) => {
                console.info(`received new command result! `, data);
                this.setState({
                    lastCommandResult: data
                });
            });
        } else {
            console.warn(`unable to subscribe to processes' command results: `, this.props.uri);
        }
    }
    unsubscribe() {
        console.info("unsubscribing from process..");
        this.props.wamp && WAMP.unsubscribe(this.props.wamp.topic);
    }

    renderModalContent() {
        const { customOperationParams } = this.state;
        if (!customOperationParams) {
            return null;
        }
        return (
             <form className="form-horizontal">
                {
                    customOperationParams.map((p, i) => {
                        return (
                            <div className="form-group label-floating" key={i}>
                                <label className="control-label">{p.label}</label>
                                <input type="text" className="form-control" ref={p.label} defaultValue={p.defaultValue} />
                            </div>
                        );
                    })
                }
            </form>
        )
    }
    renderModal() {
        return (
            <div className="modal fade" id="process-modal" tabIndex="-1" role="dialog">
                <div className="modal-dialog">
                    <div className="modal-content">
                        <div className="modal-header">
                            <button type="button" className="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                            <h4 className="modal-title">Perform operation with params</h4>
                        </div>
                        <div className="modal-body">
                            {this.renderModalContent()}
                        </div>
                        <div className="modal-footer">
                            <button type="button" className="btn btn-primary modal-decline" data-dismiss="modal">Cancel</button>
                            <button type="button" className="btn btn-warning modal-accept" onClick={this.performOperationWithParams.bind(this)}>Perform</button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    render() {
        const { label, lastCommandResult, operations } = this.state;
        const { baseColor } = this.props;
        return (
            <div className="process-view">
                <h4>{label}</h4>
                {
                    operations.map((o) => {
                        const isLast = lastCommandResult.value === o.id;
                        return (
                            <button className={`btn operation-btn btn-raised ${isLast ? "active" : ""}`} style={{
                                backgroundColor: utils.lightenDarkenColor(baseColor, isLast ? -40 : 0)
                            }} onClick={this.handlePerformOperation(o)} key={o.id}>
                                {
                                    this.hasAdditionalParams(o) &&
                                        <i className="fa fa-cogs"></i>
                                }
                                <div>{o.label}</div>
                                {
                                    isLast &&
                                        <div>last command result at {moment(lastCommandResult.timestamp).format('HH:mm:ss')}</div>
                                }
                            </button>
                        );
                    })
                }
                {this.renderModal()}
            </div>
        )
    }
}