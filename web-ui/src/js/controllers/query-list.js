export default function($scope, analyzeQueriesUtils, CONFIG) {
    $scope.queries = [

    ];

    $scope.init = function() {
        analyzeQueriesUtils.fetchQueries().done((data) => {
            console.info('loaded queries: ', data);
            this.queries = data;
        });
    };

    $scope.init();
}