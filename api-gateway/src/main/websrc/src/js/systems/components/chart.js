import React, { Component } from 'react';
import Chart from "chart.js";
import commonUtils from '../utils/common';

export default class ObservationChart extends Component {
    constructor(...args) {
        super(...args);
    }

   componentDidMount() {
      let chartCanvas = this.refs.chart;

      let myChart = new Chart(chartCanvas, {
        type: 'line',
        data: {
            datasets: [{
                backgroundColor: commonUtils.convertHex(this.props.color, 40),
                borderColor: this.props.color,
                borderWidth: 0.5,
                label: this.props.label,
                data: []
            }]
        },
        options: {
            maintainAspectRatio: false,
            scales: {
                xAxes: [{
                    type: 'time',
                    time: {
                        displayFormats: {
                            quarter: 'MMM YYYY'
                        }
                    }
                }]
            }
        }
      });

      this.setState({ chart: myChart });
    }
    componentDidUpdate() {
        let chart = this.state.chart;

        chart.data.datasets[0].data = this.props.data.map((d) => {
            return {
                x: d.timestamp,
                y: d.value
            }
        });

        chart.update();
    }

    render() {
        return (
            <canvas ref={'chart'} height={'300'} width={'600'}></canvas>
        )
    }
}