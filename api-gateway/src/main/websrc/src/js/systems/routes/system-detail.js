import React, { Component } from 'react';
import systemAPI from '../api/systems';
import CONFIG from '../config';
import { Link } from 'react-router';

import SensorDetail from '../components/sensor-detail';
import ProcessDetail from '../components/process-detail';

const COLORS = [
    "#4bc0c0",
    "#37495F",
    "#29AD61",
    "#3598DB",
    "#7266B8"
];

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
        let colorIndex = 0;
        return (
            <section>
                <ol className="breadcrumb">
                    <li><Link to="/systems">Systems</Link></li>
                    <li className="active">{name}</li>
                </ol>
                <div id="single-system-wrapper" className={"container" + (isLoading ? " preloader" : "")}>
                    <div>
                        {
                            sensors.map((s, i) => {
                                return <SensorDetail key={i}
                                    uri={s.uri}
                                    baseColor={COLORS[colorIndex++ % COLORS.length]}
                                />
                            })
                        }
                    </div>
                    <div>
                        {
                            processes.map((p, i) => {
                                const color = COLORS[colorIndex++ % COLORS.length];
                                return <ProcessDetail key={i}
                                    uri={p.uri}
                                    baseColor={color}
                                />
                            })
                        }
                    </div>
                </div>
            </section>
        )
    }
}