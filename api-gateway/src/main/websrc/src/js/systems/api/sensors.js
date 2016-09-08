import HTTP from '../services/http';
import WAMP from '../services/wamp';
import CONFIG from '../config';

function getSensorLabel(id) {
    return `${id}`;
}

export default {
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
            throw e;
        });
    }
};