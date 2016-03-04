export default function($http) {
    return {
        fetchQueries() {
            return $http.get(CONFIG.URLS.analyze.query);
        },
        fetchQueryEvent(queryId) {
            return $http.get(CONFIG.URLS.analyze.query.events.format(queryId));
        },
        createQuery(payload) {
            return $http.post(CONFIG.URLS.analyze.query, {
                data: JSON.stringify(payload),
                contentType: "application/json"
            });
        },
        removeQuery(queryId) {
            return $http.remove(CONFIG.URLS.analyze.query.events.format(queryId));
        }
    };
}