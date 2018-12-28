var app = angular.module('pinyougou', []);//定义品优购模块

//定义过滤器
app.filter('trustHtml', ['$sce', function ($sce) {
    return function (data) {//传入参数是被过滤内容
        return $sce.trustAsHtml(data)//返回过滤后的内容

    }
}]);