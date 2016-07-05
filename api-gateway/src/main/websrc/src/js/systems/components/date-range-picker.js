import React, { Component } from 'react';
import moment from 'moment';
import DateTimeField from 'react-bootstrap-datetimepicker';

export default class Picker extends Component {
    constructor(...args) {
        super(...args);

        this.state = {
            inputFormat: "MM/DD/YY h:mm A"
        }

        this.handleChange = (which) => {
            return (e) => {
                console.info("date range changed");
                this.setState({
                    [which]: parseInt(e)
                });
            };
        }
        this.handleNowClick = () => {
            this.setState({
                end: Date.now()
            })
        };
        this.handleSelectClick = () => {
            this.props.onChange && this.props.onChange([this.state.start, this.state.end]);
        };
    }

    componentDidMount() {
        this.setDateTime();
    }
    componentDidUpdate(prevProps) {
        if (prevProps.start !== this.props.start || prevProps.end !== this.props.end) {
            this.setDateTime();
        }
    }
    setDateTime() {
        this.setState({
            start: this.props.start,
            end: this.props.end
        })
    }

    render() {
        const { inputFormat, start, end } = this.state;
        return (
            <div className="range-control">
                <div className="chart-control">
                    <label>From</label>
                    <DateTimeField
                        dateTime={start}
                        inputFormat={inputFormat}
                        onChange={this.handleChange("start")}
                    />
                </div>
                <div className="chart-control">
                    <label>
                        <span>To</span>
                        <a onClick={this.handleNowClick}>[now]</a>
                    </label>
                    <DateTimeField
                        dateTime={end}
                        inputFormat={inputFormat}
                        onChange={this.handleChange("end")}
                    />
                </div>
                <button className="btn btn-primary btn-raised btn-sm" onClick={this.handleSelectClick}>Select</button>
            </div>
        )
    }
}