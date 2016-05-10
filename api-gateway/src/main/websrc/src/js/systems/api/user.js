export default function(CONFIG, $http, $q) {

    let currentUser = null;

    return {
        getCurrentUser() {
            console.info(`loading current user`);
            const defer = $q.defer();

            if (currentUser) {
                console.info(`getting current user from cache`);
                defer.resolve(currentUser);
            } else {
                $http.get(CONFIG.URLS.currentUser).then((res) => {
                    console.info(`loaded current user: `, res);
                    currentUser = res;
                    defer.resolve(currentUser);
                }, () => {
                    defer.resolve();
                });
            }

            return defer.promise;
        }
    };
}