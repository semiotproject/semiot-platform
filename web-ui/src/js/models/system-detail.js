"use strict";

import { EventEmitter } from 'events';
import moment from 'moment';

export default function(
    $http,
    commonUtils,
    sparqlUtils,
    tsdbUtils,
    wampUtils,
    CONFIG
) {

    class Instance extends EventEmitter {
        constructor() {
            super();
        }
        subscribe() {
            wampUtils.subscribe(
                [
                    {
                        topic:  CONFIG.TOPICS.device_registered,
                        callback: this.onDeviceRegistered.bind(this)
                    }
                ]
            );
        }
        fetchSystemName(uri, callback) {
            return sparqlUtils.executeQuery(CONFIG.SPARQL.queries.getSystemName.format(uri), callback);
        }
        fetchSystemSensors(uri, callback) {
            return sparqlUtils.executeQuery(CONFIG.SPARQL.queries.getSystemSensors.format(uri), callback);
        }
        fetchSystemDetail(uri, callback) {
            return sparqlUtils.executeQuery(CONFIG.SPARQL.queries.getSystemDetail.format(uri), callback);
        }
        fetchArchiveObservations(metric, range, type) {
            return tsdbUtils.getArchiveObservations(metric, range, type);
        }
        fetchArchiveStates(metric, range) {
            return tsdbUtils.getArchiveStates(metric, range);
        }
        fetchLastTestimonials(metric) {
            return tsdbUtils.getLastObservation(metric);
        }
    }

    return new Instance();
}

