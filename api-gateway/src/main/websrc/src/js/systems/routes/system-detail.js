import React, { Component } from 'react';
import systemAPI from '../api/systems';
import CONFIG from '../config';

import SensorDetail from '../components/sensor-detail';
import ProcessDetail from '../components/process-detail';

export default class SystemDetail extends Component {
    constructor(...args) {
        super(...args);

        this.state = {
            name: "",
            isLoading: true,
            sensors: [],
            processes: []
        };
    }

    componentDidMount() {
        const { systemId } = this.props.params;
        console.info(`loading system with id = ${systemId}`);
        this.loadSystem(CONFIG.URLS.base + location.pathname);
    }

    loadSystem(uri) {
        systemAPI.loadSystem(uri).then((system) => {
            this.setState({
                isLoading: false,
                name: system.name,
                sensors: system.sensors,
                processes: system.processes
            });
        });
    }

    render() {
        const { name, isLoading, sensors, processes } = this.state;
        return (
            <section>
                <ol className="breadcrumb">
                    <li><a href="/systems">Systems</a></li>
                    <li className="active">{name}</li>
                </ol>
                <div id="single-system-wrapper" className={isLoading ? "preloader" : ""}>
                    <div>
                        <h4>Available sensors:</h4>
                        {
                            sensors.map((s, i) => { return <SensorDetail key={i} data={s}/> })
                        }
                    </div>
                    <div>
                        <h4>Available processes:</h4>
                        {
                            processes.map((p, i) => { return <ProcessDetail key={i} data={p}/> })
                        }
                    </div>
                </div>
            </section>
        )
    }
}