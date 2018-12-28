app.service('uploadService',function ($http) {
    //上传文件
    this.uploadFile=function () {
        var formdate = new FormData();
        //文件上传框的name
        formdate.append('file',file.files[0]);

       return $http({
           url:'../upload.do',
           method:'post',
           data:formdate,
           headers:{'Content-type':undefined},
          transformRequest:angular.identity
       });

    }
});