export default function(CONFIG, $http, $q, WAMP) {
    return {
        loadObservations(sensor, from, to) {
            console.info(`loading observations for sensor ${sensor} from ${new Date(from)} to ${new Date(to)}`);
            const defer = $q.defer();

            $http.get(CONFIG.URLS.observations.list).success((res) => {
                defer.resolve(res);
            });

            return defer.promise;
        },
        subscribeToNewObservations() {
            //
        }
    };
}