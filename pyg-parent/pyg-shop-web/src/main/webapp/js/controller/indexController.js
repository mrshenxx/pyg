app.controller('indexController',function ($scope, shopLoginService) {
    //显示当前用户名
    $scope.showNickName=function () {
        shopLoginService.nickName().success(
           function (response) {
               $scope.nickName=response.nickName;
           }
        )
    }
})