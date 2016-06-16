export default ["$http", "$q", function($http, $q) {

    return {
        get(url) {
            return this.query(url, "GET");
        },
        query(url, method, data = undefined) {
            console.info(`performing http '${method}'' request on ${url}; data is`, data);
            const defer = $q.defer();

            const options = {
                method,
                url,
                data,
                headers: {
                    'Accept': 'application/json'
                }
            };

            if (method === "POST") {
                options.headers['Content-Type'] = "application/ld+json";
            }

            $http(options).then((res) => {
                defer.resolve(res.data);
            }, () => {
                defer.reject();
            });

            return defer.promise;
        }
    };
}];