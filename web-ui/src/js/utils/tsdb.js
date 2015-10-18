"use strict";

export default function($http, CONFIG) {

    let instance = {
        getArchiveObservations: function(metric, range) {
            return $http.get(CONFIG.URLS.tsdb.archive.format(
                moment(range[0] - CONFIG.TIMEZONE_OFFSET).format('YYYY/MM/DD-HH:mm:ss'),
                moment(range[1] - CONFIG.TIMEZONE_OFFSET).format('YYYY/MM/DD-HH:mm:ss'),
                metric
            ));
        },
        getLastObservation: function(metric) {
            return $http.get(CONFIG.URLS.tsdb.last.format(metric));
        }
    };

    return instance;
}