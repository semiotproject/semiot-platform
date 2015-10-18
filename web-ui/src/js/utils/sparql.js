"use strict";

export default function($http, CONFIG) {

    // helpers
    let getPrefixes = function() {
        let str = [];
        for (let key in CONFIG.SPARQL.prefixes) {
            str.push(`PREFIX ${key}: <${CONFIG.SPARQL.prefixes[key]}>`);
        }
        return str.join('\n') + '\n';
    };

    let instance = {
        executeQuery: function(query, callback) {
            let config = {
                params: {
                    query: getPrefixes() + query
                },
                headers: { Accept: "application/sparql-results+json" }
            };
            return $http.get(CONFIG.URLS.tripleStore, config).success(callback);
        }
    };

    return instance;
}