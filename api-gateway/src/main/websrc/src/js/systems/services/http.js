export default ["$http", "$q", function($http, $q) {

    return {
        get(url) {
            console.info(`performing http GET on ${url}`);
            const defer = $q.defer();

            $http({
                url: url,
                headers: {
                    'Accept': 'application/json'
                }
            }).then((res) => {
                defer.resolve(res.data);
            }, () => {
                defer.reject();
            });

            return defer.promise;
        }
    };
}];