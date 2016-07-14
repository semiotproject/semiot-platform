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
        const { systems, search, fromIndex } = this.props;
        return (
            <div className="table-responsive system-list">
                <table className="table table-striped">
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
                                        <td>{fromIndex + s.index}</td>
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