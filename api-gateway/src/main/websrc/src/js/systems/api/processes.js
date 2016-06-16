export default ["CONFIG", "HTTP", "WAMP", "$q", "commonUtils", function(CONFIG, HTTP, WAMP, $q, commonUtils) {

    Object.prototype.renameProperty = function (oldName, newName) {
         // Do nothing if the names are the same
         if (oldName == newName) {
             return this;
         }
        // Check for the old property name to avoid a ReferenceError in strict mode.
        if (this.hasOwnProperty(oldName)) {
            this[newName] = this[oldName];
            delete this[oldName];
        }
        return this;
    };

    function convertShaclProperyToPredicate(obj) {
        obj.renameProperty("sh:property", "sh:predicate");
        return obj;
    }

    function parseOperations(uri, o) {
        return commonUtils.ensureArray(o["hydra:expects"]).map((e) => {
            const result = {
                method: o["hydra:method"],
                id: e["dcterms:identifier"],
                label: e["rdfs:label"]["@value"],
                body: e,
                uri
            };
            if (e['dul:hasParameter']) {
                result.body = $.extend(e, {
                    'dul:hasParameter': e['dul:hasParameter'].map(convertShaclProperyToPredicate)
                });
            }
            return result;
        });
    }

    return {
        loadProcessInformation(uri) {
            console.info(`loading process info from ${uri}`);
            const defer = $q.defer();
            const process = {
                uri
            };

            HTTP.get(uri).then((res) => {
                process.id = res['dcterms:identifier'];
                process.operations = parseOperations(uri, res['hydra:supportedOperation']);
                debugger;
                process.commandResults = [];

                this.loadCommandResults(res['apidoc:commandResults']).then((commandResults) => {
                    process.commandResults = commandResults;
                    defer.resolve(process);
                });
            });

            return defer.promise;
        },
        loadCommandResults(uri) {
            console.info(`loading command results from ${uri}`);
            return HTTP.get(uri).then((res) => {
                return res["hydra:member"].map((m) => {
                    return {
                        timestamp: (new Date(m['dul:hasEventTime'])).toString(),
                        result: m['semiot:isResultOf']['dcterms:identifier']
                    };
                });
            });
        },
        performOperation(o) {
            return HTTP.query(o.uri, o.method, o.body);
        }
    };

}];