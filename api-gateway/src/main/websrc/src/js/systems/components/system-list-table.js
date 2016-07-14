import React, { Component } from 'react';
import { Link } from 'react-router';

export default class SystemListTable extends Component {
    constructor(...args) {
        super(...args);

        this.handleSearchChange = (e) => {
            this.props.onSearchChange && this.props.onSearchChange(e);
        };
    }

    render() {
        const { systems, search } = this.props;
        return (
            <div className="table-responsive system-list">
                <table className="table table-striped">
                    <thead>
                        <tr>
                            <th>
                                <label>{systems.length}</label>
                            </th>
                            <th>
                                <div className="form-group is-empty">
                                    <div>
                                        <input
                                            onChange={this.handleSearchChange}
                                            placeholder="Name"
                                            className="form-control"
                                            value={search}
                                            type="text"
                                        />
                                    </div>
                                    <span className="material-input"></span>
                                </div>
                            </th>
                            <th>

                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        {
                            systems.length === 0 &&
                                <tr>
                                    <td>No systems registered</td>
                                    <td></td>
                                    <td></td>
                                </tr>
                        }
                        {
                            systems.map((s) => {
                                return (
                                    <tr key={s.index}>
                                        <td>{s.index}</td>
                                        <td><Link to={'/systems/' + s.id}>{s.name}</Link></td>
                                        <td>
                                        </td>
                                    </tr>
                                );
                            })
                        }
                    </tbody>
                </table>
            </div>
        );
    }
}