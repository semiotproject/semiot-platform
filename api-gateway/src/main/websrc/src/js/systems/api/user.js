export default function(CONFIG, $http, $q) {

    let currentUser = null;

    return {
        getCurrentUser() {
            console.info(`loading current user`);
            const defer = $q.defer();


            defer.resolve({ data: { username: "root", password: "root" } });

            return defer.promise;
        }
    };
}