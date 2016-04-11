export default function($scope, analyzeQueriesUtils) {

    $scope.editorOptions = {
        lineWrapping : true,
        lineNumbers: true,
        matchBrackets: true,
        mode: 'application/sparql-query'
    };

    $scope.name = "New query";
    $scope.text = "SELECT * from";

    $scope.submit = function() {
        analyzeQueriesUtils.createQuery({
            text: $scope.text,
            name: $scope.name
        }).then(() => {
            location.path = "/#/queries";
        });
    };
}