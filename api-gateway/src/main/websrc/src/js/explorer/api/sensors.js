export default function(CONFIG, $http, $q) {

    function getSensorLabel(id) {
        return `this name would be asked from prototype / ${id}`;
    }

    return {
        loadSensor(uri) {
            console.info(`loading sensor ${uri}`);
            const defer = $q.defer();

            $http.get(uri).success((res) => {
                defer.resolve({
                    uri: res['@id'],
                    id: res['dcterms:identifier'],
                    observationsURI: res['apidoc:observations']['@id'],
                    label: getSensorLabel(res['dcterms:identifier'])
                });
            });

            return defer.promise;
        }
    };
}