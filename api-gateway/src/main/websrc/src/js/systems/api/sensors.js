export default ["CONFIG", "$http", "$q", function(CONFIG, $http, $q) {

    function getSensorLabel(id) {
        return `this name would be asked from prototype / ${id}`;
    }

    return {
        loadSensor(uri) {
            console.info(`loading sensor ${uri}`);
            const defer = $q.defer();

            $http({
                url: uri,
                headers: {
                    'Accept': 'application/json'
                }
            }).success((res) => {
                defer.resolve({
                    uri: res['@id'],
                    id: res['dcterms:identifier'],
                    observationsURI: res['apidoc:observations'],
                    label: getSensorLabel(res['dcterms:identifier'])
                });
            });

            return defer.promise;
        }
    };
}];
