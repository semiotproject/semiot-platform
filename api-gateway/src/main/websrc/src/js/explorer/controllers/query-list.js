export default function($scope, analyzeQueriesUtils, CONFIG) {
    $scope.queries = [

    ];

    $scope.init = function() {
        analyzeQueriesUtils.fetchQueries().then((res) => {
            this.queries = res.data.map((d) => {
                d.created = moment(new Date(parseInt(d.created))).format('YYYY/MM/DD HH:mm:ss');
                console.log(d);
                return d;
            });

            console.info('loaded queries: ', this.queries);
        });
    };

    $scope.remove = function(id) {
        analyzeQueriesUtils.removeQuery(id).then((res) => {
            this.queries.forEach((q, index) => {
                if (q.id === id) {
                    this.queries.splice(index, 1);
                }
            });
        });
    };

    $scope.init();
}