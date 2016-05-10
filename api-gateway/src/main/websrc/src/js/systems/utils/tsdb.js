"use strict";

export default function($http, commonUtils, CONFIG) {

    let instance = {
        getArchiveObservations: function(system, sensor, range) {
            return $http.get(CONFIG.URLS.tsdb.archiveQuantity.format(
                system,
                sensor,
                moment(range[0] - CONFIG.TIMEZONE_OFFSET).format('YYYY-MM-DDTHH:mm:ss'),
                moment(range[1] - CONFIG.TIMEZONE_OFFSET).format('YYYY-MM-DDTHH:mm:ss')
            ));
        },
        getArchiveStates: function(metric, range) {
            return $http.get(CONFIG.URLS.tsdb.archiveEnum.format(
                moment(range[0] - CONFIG.TIMEZONE_OFFSET).format('YYYY-MM-DDTHH:mm:ss'),
                moment(range[1] - CONFIG.TIMEZONE_OFFSET).format('YYYY-MM-DDTHH:mm:ss'),
                metric
            ));
        },
        getLastObservation: function(metric) {
            return $http.get(CONFIG.URLS.tsdb.last.format(metric));
        }
    };

    return instance;
}