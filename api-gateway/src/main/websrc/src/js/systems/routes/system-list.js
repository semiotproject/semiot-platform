import React, { Component } from 'react';
import systemAPI from '../api/systems';
import { Pagination } from 'react-bootstrap';

import SystemListTable from '../components/system-list-table';
import { browserHistory } from 'react-router'

const DEFAULT_PAGE_NUMBER = 1;
const DEFAULT_PAGE_SIZE = 10;

export default class SystemList extends Component {
    constructor(...args) {
        super(...args);

        this.state = {
            systems: [],
            isLoading: true,
            totalSystemsCount: DEFAULT_PAGE_SIZE,
            search: ""
        };
        this.handleNewSystem = (s) => {
            const { totalSystemsCount, systems } = this.state;
            const pageSize = this.getCurrentPageSize();
            console.info("registered new system: ", s);
            this.setState({
                systems: systems.length >= pageSize ? systems : [...systems, s],
                totalSystemsCount: totalSystemsCount + 1
            });
        };
        this.handleSearchChange = (e) => {
            //
        };
        this.handlePageClick = (e) => {
            this.setPage(e);
        };
    }
    setPage(page) {
        browserHistory.push(`/systems?page=${page}&size=${this.getCurrentPageSize()}`)
    }
    getCurrentPage() {
        return this.props.location.query.page || DEFAULT_PAGE_NUMBER;
    }
    getCurrentPageSize() {
        return this.props.location.query.size || DEFAULT_PAGE_SIZE;
    }
    componentDidMount() {
        let { page, size } = this.props.location.query;
        if (!page || !size) {
            if (!page) {
                page = DEFAULT_PAGE_NUMBER;
            }
            if (!size) {
                size = DEFAULT_PAGE_SIZE;
            }
            browserHistory.replace(`/systems?page=${page}&size=${size}`)
        } else {
            this.querySystems();
        }
        this.subscribe();
    }
    componentDidUpdate(prevProps) {
        if (prevProps.location.query.page !== this.props.location.query.page || prevProps.location.query.size !== this.props.location.query.size) {
            this.querySystems();
        }
    }
    componentWillUnmount() {
        this.unsubscribe();
    }
    querySystems() {
        const page = this.getCurrentPage();
        const size = this.getCurrentPageSize();
        console.info(`querying systems; current page is ${page}, items per page: ${size}`);
        systemAPI.loadSystems(page, size).then((res) => {
            this.setState({
                systems: res.systems,
                totalSystemsCount: res.totalSystemsCount,
                isLoading: false
            });
        }, () => {
            this.setState({
                error: true,
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
        const size = this.getCurrentPageSize();
        const { isLoading, error, systems, search, totalSystemsCount } = this.state;
        let content;
        if (error) {
            content = "error while loading data";
        } else {
            content = [
                <ol className="breadcrumb">
                    <li className="active">Systems</li>
                </ol>,
                <SystemListTable systems={systems} search={search} onSearchChange={this.handleSearchChange} fromIndex={(page - 1) * size} />,
                totalSystemsCount > size &&
                    <Pagination
                            prev
                            next
                            first
                            last
                            ellipsis
                            boundaryLinks
                            items={Math.ceil(totalSystemsCount/ size)}
                            maxButtons={5}
                            activePage={parseInt(page)}
                            onSelect={this.handlePageClick} />
            ];
        }
        return (
            <section className={isLoading ? "preloader" : ""}>
                {content}
            </section>
        );
    }
}