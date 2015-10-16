"use strict";

import { EventEmitter } from 'events';
import moment from 'moment';

export default function(
    $http,
    commonUtils,
    sparqlUtils,
    CONFIG
) {

    class Instance extends EventEmitter {
        constructor() {
            super();
        }
        fetchSystemName(uri, callback) {
            return sparqlUtils.executeQuery(CONFIG.SPARQL.queries.getSystemName.format(uri), callback);
        }
        fetchSystemEndpoint(uri, callback) {
            return sparqlUtils.executeQuery(CONFIG.SPARQL.queries.getSystemEndpoint.format(uri), callback);
        }
        fetchArchiveTestimonials(metric, range) {
            return $http.get(CONFIG.URLS.tsdb.format(
                moment(range[0] - CONFIG.TIMEZONE_OFFSET).format('YYYY/MM/DD-HH:mm:ss'),
                moment(range[1] - CONFIG.TIMEZONE_OFFSET).format('YYYY/MM/DD-HH:mm:ss'),
                commonUtils.parseTopicFromEndpoint(metric),
                {}
            ));
        }
    }

    return new Instance();
}
