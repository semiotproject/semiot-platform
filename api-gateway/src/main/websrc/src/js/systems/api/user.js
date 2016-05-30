export default ["CONFIG", "$http", "$q", function(CONFIG, $http, $q) {

    let currentUser = null;

    function renderUsername(user) {
        document.querySelector('.username').innerHTML = user;
    }

    return {
        getCurrentUser() {
            const defer = $q.defer();
            if (!currentUser) {
                console.info(`loading current user`);

                // defer.resolve({ data: { username: "root", password: "root" } });
                $http({
                    url: CONFIG.URLS.currentUser
                }).success((res) => {
                    console.info(`loaded current user: `, res);
                    currentUser = {
                        data: res
                    };
                    renderUsername(res.username);
                    defer.resolve(currentUser);
                });
            } else {
                defer.resolve(currentUser);
            }

            return defer.promise;
        }
    };
}];