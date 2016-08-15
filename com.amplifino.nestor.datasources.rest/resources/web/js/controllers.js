var dataSourceControllers = angular.module('sqlControllers', []);

dataSourceControllers.controller('SqlCtrl', ['$scope', 'DataSource' , 'Table',
                                                   	function ($scope, DataSource, Table) {
	$scope.gridOptions={enableGridMenu: true};
	$scope.dataSources=DataSource.query( {} , function(data) {
		if (data.length > 0) {
			$scope.selectedDataSource = data[0];
		}
	});
	
	$scope.submitQuery = function()  {
		delete $scope.gridOptions.columnDefs;
		delete $scope.gridOptions.data;
		$scope.message = "submitting query ";
		$scope.sqlResult = DataSource.select( { name: $scope.selectedDataSource.name }, $scope.sqlText, function (data) {
			$scope.message = (data.tuples ? "selected " : "inserted/updated " ) + data.rowCount + " rows";
			$scope.gridOptions.data = data.tuples;
		}, function(httpResponse) {
			$scope.message = "query failed: " + httpResponse.status + " " + httpResponse.statusText + " " + httpResponse.data;
		});
	}
	

	$scope.addTable = function() {
		$scope.sqlText += " " + $scope.selectedTable.name;
	}
	
	$scope.addColumns = function() {
		$scope.sqlText += $scope.selectedColumns
			.map( function(col) {
				return " " + col.name })
			.toString();
	}
		
}]);
