export default function(
    $scope,
    $routeParams,
    analyzeQueriesUtils,
    CONFIG
) {

    $scope.query = {};

    // call when page is loaded
    $scope.init = function(id) {
        $scope.isLoading = true;
        analyzeQueriesUtils.fetchQueryDetail(id).then((res) => {
          console.info('result query is: ', res.data);
          this.query.events = res.data;
          $scope.isLoading = false;
        });

        analyzeQueriesUtils.fetchQueries().then((res) => {
            res.data.forEach((a, index) => {
                if (a.id === id) {
                  this.query = $.extend({}, this.query, res.data[index]);
                }
            });
        });
    };

    $scope.init(decodeURIComponent($routeParams.query_id));
}