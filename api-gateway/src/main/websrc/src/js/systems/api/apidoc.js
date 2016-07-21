export default function(CONFIG, $http, $q) {

    function getAPIDocURL(json) {
        return json['@context']['apidoc'];
    }

    function getAPIDocPostfix(json) {
        let apiDocPostfix;
        json['@type'].forEach((t) => {
            if (t.indexOf('apidoc') > -1) {
                apiDocPostfix = t;
            }
        });
        return apiDocPostfix;
    }

    function getParsedAPIDocPostfix(json) {
        return getAPIDocPostfix(json).split(":")[1];
    }

    function constructAPIDocURL(json) {
        return getAPIDocURL(json) + getParsedAPIDocPostfix(json);
    }

    return {
        loadAPIDoc(json) {
            console.info(`loading api documetation for ${json['@id']}`);
            const defer = $q.defer();

            console.log(constructAPIDocURL(json));

            try {
                $http({
                    url: constructAPIDocURL(json),
                    headers: {
                        'Accept': 'application/json'
                    }
                }).success((res) => {
                    res['hydra:supportedClass'].forEach((c) => {
                        if (c['@id'] === getAPIDocPostfix(json)) {
                            defer.resolve(c);
                        }
                    });
                    // defer.resolve(res);
                });
            } catch(e) {
                console.error('failed to load api doc: error = ', e);
                defer.reject();
            }

            return defer.promise;
        }
    };
}