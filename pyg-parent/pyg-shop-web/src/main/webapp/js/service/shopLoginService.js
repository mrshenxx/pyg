app.service("shopLoginService",function ($http) {
    this.nickName=function () {
      return $http.get('../shoplogin/name.do')
    }
})