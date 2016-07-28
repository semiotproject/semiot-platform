import React, { Component } from 'react';
import sensorAPI from '../api/sensors';
import observationAPI from '../api/observations';
import DateRangePicker from './date-range-picker';
import Chart from './chart';
import moment from 'moment';
import WAMP from '../services/wamp';

function getDefaultRange() {
    let now = (new Date()).getTime();
    let end_date = new Date(now);
    let start_date = (new Date(now - 0.5 * 3600 * 1000));

    return [start_date.getTime(), end_date.getTime()];
}

const MODES = {
    "archive": {
        label: "Archive"
    },
    "real-time": {
        label: "Real time"
    }
};

export default class SensorDetail extends Component {
    constructor(...args) {
        super(...args);

        this.state = {
            label: "",
            mode: "real-time",
            isLoading: true,
            observations: [],
            range: getDefaultRange()
        };
        this.handleModeClick = (mode) => {
            return () => {
                console.info(`mode ${mode} selected`);
                this.setState({
                    mode
                }, () => {
                    this.state.mode === "real-time" ? this.subscribe() : this.unsubscribe();
                });
            };
        };
        this.handleRangeChanged = (range) => {
            console.info("range changed");
            this.setState({
                range
            }, () => {
                this.loadObservations();
            });
        };
        this.handleNewObservationReceived = (obs) => {
            console.info("new observation received: ", obs);
            this.setState({
                observations: [...this.state.observations, obs]
            })
        }
    }

    componentDidMount() {
        this.loadDetail(this.props.uri);
    }
    componentWillUnmount() {
        this.unsubscribe();
    }
    getLastObservation() {
        return this.state.observations[this.state.observations.length - 1];
    }

    loadDetail(uri) {
        console.info('loading sensor detail');
        sensorAPI.loadSensor(uri).then((sensor) => {
            this.setState({
                label: sensor.label,
                observationsURI: sensor.observationsURI
            }, () => {
                this.loadObservations().then(() => {
                    this.subscribe();
                });
            });
        });
    }
    loadObservations() {
        const { observationsURI, range } = this.state;
        return observationAPI.loadObservations(observationsURI, range).then((obs) => {
            this.setState({
                observations: obs.sort((a, b) => {
                    return a.timestamp > b.timestamp ? 1 : -1
                }),
                isLoading: false
            });
        });
    }
    subscribe() {
        const { observationsURI } = this.state;
        console.info(`subscribing on observations URI ${observationsURI}`);
        observationAPI.loadWAMPTopic(observationsURI).then((res) => {
            console.info(`loaded WAMP topic: `, res);
            this.setState({
                WAMPTopic: res.topic
            }, () => {
                observationAPI.subscribeForNewObservations(res.endpoint, res.topic, this.handleNewObservationReceived);
            })
        });
    }
    unsubscribe() {
        if (this.state.WAMPTopic) {
            console.info("unsubscribing..");
            WAMP.unsubscribe(this.state.WAMPTopic);
        } else {
            console.warn("not found WAMPTopic; do nothing");
        }
    }
    filterObservations(obs) {
        return this.state.mode === "real-time" ? obs.slice(Math.max(obs.length - 5, 1)) : obs;
    }

    renderModeControls() {
        const { mode } = this.state;
        return (
            <div className="mode-control btn-group">
                {
                    Object.keys(MODES).map((k) => {
                        return (
                            <label key={k} className={`btn btn-raised btn-sm ${mode === k ? " active" : ""}`}
                                onClick={mode !== k && this.handleModeClick(k)}
                            >
                                {MODES[k].label}
                            </label>
                        );
                    })
                }
            </div>
        );
    }

    render() {
        const { label, range, mode, observations, isLoading } = this.state;
        const { baseColor } = this.props;
        const lastObservation = this.getLastObservation();
        return (
            <div className={`sensor-view ${isLoading ? "preloader" : ""}`}>
                {
                    !isLoading &&
                        <div className="sensor-last-observation raised" style={{
                            backgroundColor: baseColor
                        }}>
                            <span>
                                {
                                    lastObservation ?
                                        <span className="big">{lastObservation.value}</span> :
                                        <i className="fa fa-clock-o" style={{
                                            marginBottom: "5px",
                                            fontSize: "22px"
                                        }}></i>
                                }
                                <span>{label}</span>
                                {
                                    lastObservation ?
                                        <span>last observation at {moment(lastObservation.timestamp).format('HH:mm:ss')}</span> :
                                        <span>no observations found</span>
                                }
                                </span>
                        </div>
                }
                {this.renderModeControls()}
                {
                    mode === "archive" &&
                        <DateRangePicker start={range[0]} end={range[1]} onChange={this.handleRangeChanged}/>
                }
                {
                    !isLoading && (
                        lastObservation ?
                            <Chart data={this.filterObservations(observations)} color={baseColor} label={label}/> :
                            (
                                mode === "archive" ?
                                    <div className="no-observations-banner">No observations found</div> :
                                    <div className="no-observations-banner">Waiting for the first observation..</div>
                            )
                    )
                }
            </div>
        )
    }
}