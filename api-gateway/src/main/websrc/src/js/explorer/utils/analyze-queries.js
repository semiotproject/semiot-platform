export default function($http, CONFIG) {
    return {
        fetchQueries() {
            return $http.get(CONFIG.URLS.analyze.query);
        },
        fetchQueryDetail(queryId) {
            return $http.get(CONFIG.URLS.analyze.events.format(queryId));
        },
        createQuery(payload) {
            return $http.post(CONFIG.URLS.analyze.query, payload, {
                contentType: "application/json"
            });
        },
        removeQuery(queryId) {
            return $http.delete(CONFIG.URLS.analyze.query + "/" + queryId);
        }
    };
}