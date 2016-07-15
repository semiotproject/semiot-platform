import React, { Component } from 'react';
import systemAPI from '../api/systems';
import { Pagination } from 'react-bootstrap';

import SystemListTable from '../components/system-list-table';
import { browserHistory } from 'react-router'

const MAX_ITEMS_PER_PAGE = 10;

export default class SystemList extends Component {
    constructor(...args) {
        super(...args);

        this.state = {
            systems: [],
            isLoading: true,
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
            /*this.setState({
                search: e.target.value.toLowerCase()
            }, () => {
                const { systems, currentIndex } = this.state;
                if (systems.length < currentIndex * MAX_ITEMS_PER_PAGE) {
                    this.setPage(1);
                }
            });*/
        };
        this.handlePageClick = (e) => {
            this.setPage(e);
        };
    }
    setPage(page) {
        browserHistory.push(`/systems?page=${page}`)
    }
    getCurrentPage() {
        return this.props.location.query.page || 1;
    }
    componentDidMount() {
        this.querySystems();
        this.subscribe();
    }
    componentDidUpdate(prevProps) {
        if (prevProps.location.query.page !== this.props.location.query.page) {
            this.querySystems();
        }
    }
    componentWillUnmount() {
        this.unsubscribe();
    }
    querySystems() {
        const page = this.getCurrentPage();
        console.info(`querying systems; current page is ${page}, items per page: ${MAX_ITEMS_PER_PAGE}`);
        systemAPI.loadSystems(page, MAX_ITEMS_PER_PAGE).then((res) => {
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
    subscribe() {
        systemAPI.subscribeForNewSystems(this.handleNewSystem);
    }
    unsubscribe() {
        systemAPI.unsubscribeForNewSystems();
    }

    render() {
        const page = this.getCurrentPage();
        const { isLoading, systems, search, totalSystemsCount } = this.state;
        return (
            <section className={isLoading ? "preloader" : ""}>
                <ol className="breadcrumb">
                    <li className="active">Systems</li>
                </ol>
                <SystemListTable systems={systems} search={search} onSearchChange={this.handleSearchChange} fromIndex={(page - 1) * MAX_ITEMS_PER_PAGE} />
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
                                activePage={page}
                                onSelect={this.handlePageClick} />
                }
            </section>
        );
    }
}