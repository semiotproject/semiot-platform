export default ["CONFIG", "HTTP", function(CONFIG, HTTP) {

    function getSensorLabel(id) {
        return `${id}`;
    }

    return {
        loadSensor(uri) {
            console.info(`loading sensor ${uri}`);
            return HTTP.get(uri).then((res) => {
                return {
                    uri: res['@id'],
                    id: res['dcterms:identifier'],
                    observationsURI: res['apidoc:observations'],
                    label: getSensorLabel(res['dcterms:identifier'])
                };
            }, (e) => {
                console.error(`unable to parse system: e = `, e);
            });
        }
    };
}];
