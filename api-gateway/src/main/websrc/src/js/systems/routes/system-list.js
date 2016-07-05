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
                search: e.target.value.toLowerCase()
            }, () => {
                const { systems, currentIndex } = this.state;
                if (this.filterSystems(systems).length < currentIndex * MAX_ITEMS_PER_PAGE) {
                    this.setState({
                        currentIndex: 1
                    });
                }
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
    componentWillUnmount() {
        this.unsubscribe();
    }
    querySystems() {
        systemAPI.loadSystems().then((res) => {
            this.setState({
                systems: res,
                isLoading: false
            }, () => {
                this.subscribe();
            });
        });
    }
    filterSystems(systems) {
        const { search } = this.state;
        return systems.filter((s) => {
            return s.name.toLowerCase().indexOf(search) > -1;
        });
    }
    paginateSystems(systems) {
        const { currentIndex } = this.state;
        return systems.slice((currentIndex - 1) * MAX_ITEMS_PER_PAGE, currentIndex * MAX_ITEMS_PER_PAGE);
    }
    subscribe() {
        systemAPI.subscribeForNewSystems(this.handleNewSystem);
    }
    unsubscribe() {
        systemAPI.unsubscribeForNewSystems();
    }

    render() {
        const { isLoading, systems, search, currentIndex } = this.state;
        const filteredSystems = this.filterSystems(systems);
        const paginatedSystems = this.paginateSystems(filteredSystems);
        return (
            <section className={isLoading ? "preloader" : ""}>
                <ol className="breadcrumb">
                    <li className="active">Systems</li>
                </ol>
                <SystemListTable systems={paginatedSystems} search={search} onSearchChange={this.handleSearchChange} />
                {
                    filteredSystems.length > MAX_ITEMS_PER_PAGE &&
                        <Pagination
                                prev
                                next
                                first
                                last
                                ellipsis
                                boundaryLinks
                                items={Math.ceil(filteredSystems.length / MAX_ITEMS_PER_PAGE)}
                                maxButtons={5}
                                activePage={currentIndex}
                                onSelect={this.handlePageClick} />
                }
            </section>
        );
    }
}