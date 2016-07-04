import React, { Component } from 'react';
import systemAPI from '../api/systems';
import { Pagination } from 'react-bootstrap';

import SystemListTable from '../components/system-list-table';

const MAX_ITEMS_PER_PAGE = 10;

export default class SystemList extends Component {
    constructor(...args) {
        super(...args);

        this.state = {
            systems: [],
            isLoading: true,
            currentIndex: 1,
            search: ""
        };
        this.handleNewSystem = (s) => {
            console.info("registered new system: ", s);
            this.setState({
                systems: [...this.state.systems, s]
            });
        };
        this.handleSearchChange = (e) => {
            this.setState({
                search: e.target.value
            });
        };
        this.handlePageClick = (e) => {
            this.setState({
                currentIndex: e
            });
        };
    }
    componentDidMount() {
        this.querySystems();
    }
    querySystems() {
        systemAPI.loadSystems().then((res) => {
            this.setState({
                systems: res,
                isLoading: false
            })
            systemAPI.subscribeForNewSystems(this.handleNewSystem);
        });
    }
    filterSystems(systems) {
        const { currentIndex, search } = this.state;
        return systems.filter((s) => {
            return s.name.indexOf(search) > -1;
        }).slice((currentIndex - 1) * MAX_ITEMS_PER_PAGE, currentIndex * MAX_ITEMS_PER_PAGE);
    } 

    render() {
        const { isLoading, systems, search, currentIndex } = this.state;
        return (
            <section className={isLoading ? "preloader" : ""}>
                <ol className="breadcrumb">
                    <li className="active">Systems</li>
                </ol>
                <SystemListTable systems={this.filterSystems(systems)} search={search} onSearchChange={this.handleSearchChange} />
                <Pagination
                        prev
                        next
                        first
                        last
                        ellipsis
                        boundaryLinks
                        items={systems.length / MAX_ITEMS_PER_PAGE}
                        maxButtons={5}
                        activePage={currentIndex}
                        onSelect={this.handlePageClick} />
            </section>
        );
    }
}