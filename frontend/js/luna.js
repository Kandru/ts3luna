/**
 * Created by jko on 17.04.2017.
 */

"use strict";

var lunaOptions = {
    host: "http://localhost:8080",
    contextPath: "/api",
    buildUrl: function (url) {
        return this.host + this.contextPath + url;
    }
};


