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
            totalSystemsCount: MAX_ITEMS_PER_PAGE,
            search: ""
        };
        this.handleNewSystem = (s) => {
            const { totalSystemsCount, systems } = this.state;
            console.info("registered new system: ", s);
            this.setState({
                systems: systems.length >= MAX_ITEMS_PER_PAGE ? systems : [...systems, s],
                totalSystemsCount: totalSystemsCount + 1
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
                currentIndex: e,
                isLoading: true
            }, this.querySystems.bind(this));
        };
    }
    componentDidMount() {
        this.querySystems();
        this.subscribe();
    }
    componentWillUnmount() {
        this.unsubscribe();
    }
    querySystems() {
        console.info(`querying systems; current index is ${this.state.currentIndex}, items per page: ${MAX_ITEMS_PER_PAGE}`);
        systemAPI.loadSystems(this.state.currentIndex, MAX_ITEMS_PER_PAGE).then((res) => {
            this.setState({
                systems: res.systems,
                totalSystemsCount: res.totalSystemsCount,
                isLoading: false
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
        const { isLoading, systems, search, currentIndex, totalSystemsCount } = this.state;
        return (
            <section className={isLoading ? "preloader" : ""}>
                <ol className="breadcrumb">
                    <li className="active">Systems</li>
                </ol>
                <SystemListTable systems={systems} search={search} onSearchChange={this.handleSearchChange} fromIndex={(currentIndex - 1) * MAX_ITEMS_PER_PAGE} />
                {
                    totalSystemsCount > MAX_ITEMS_PER_PAGE &&
                        <Pagination
                                prev
                                next
                                first
                                last
                                ellipsis
                                boundaryLinks
                                items={Math.ceil(totalSystemsCount/ MAX_ITEMS_PER_PAGE)}
                                maxButtons={5}
                                activePage={currentIndex}
                                onSelect={this.handlePageClick} />
                }
            </section>
        );
    }
}