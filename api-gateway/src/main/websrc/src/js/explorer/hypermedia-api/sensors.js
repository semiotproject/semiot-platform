export default function(CONFIG, $http, $q) {
    return {
        loadSensors(system) {
            console.info('loading sensor list for system ', system);
            const defer = $q.defer();

            $http.get(CONFIG.URLS.sensors.list).success((res) => {
                defer.resolve(res);
            });

            return defer.promise;
        }
    };
}