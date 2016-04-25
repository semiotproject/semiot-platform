export default function(CONFIG, $http, $q, WAMP) {
    return {
        loadSystems() {
            console.info('loading systems list');
            const defer = $q.defer();

            $http.get(CONFIG.URLS.systems.list).success((res) => {
                defer.resolve(res);
            });

            return defer.promise;
        },
        subscribeToNewSystems(callback) {
            WAMP.subscribe({
                topic: CONFIG.TOPICS['device_registered'],
                callback
            });
        }
    };
}