/**
 * Created by jko on 17.04.2017.
 */
"use strict";

function getStorage() {
    var storage = window.localStorage;
    if (storage === undefined) {
        // TODO handle somehow
    }
    return storage;
}

function isAuthTokenValid() {
    var storage = getStorage();
    return storage.authTokenExpires || storage.authToken || storage.authTokenExpires > new Date();
}

function isAuthTicketValid() {
    var storage = getStorage();
    return storage.authTicketExpires || storage.authTicket || storage.authTicketExpires > new Date();
}

function getAuthToken() {
    if (!isAuthTokenValid()) {
        return undefined;
    }
    var storage = getStorage();
    return storage.authToken;
}

function getAuthTicket() {
    if (!isAuthTicketValid()) {
        return undefined;
    }
    var storage = getStorage();
    return storage.authTicket;
}

function obtainTicket(clDbId) {
    return $.ajax({
        type: 'POST',
        url: '/auth/challenge',
        cache: false,
        contentType: 'application/json',
        crossDomain: true,
        data: JSON.stringify({
            clientDbId: clDbId
        }),

    }).done((data) => {
        var storage = getStorage();
        storage.authTicketExpires = new Date(new Date().getTime() + data.expires * 60 * 1000);
        storage.authTicket = data.challenge;
    }).fail((jqXHR) => {
        // add an error label
    });
}

function obtainToken(password) {
    return $.ajax({
        type: 'POST',
        url: '/auth/authenticate',
        cache: false,
        contentType: 'application/json',
        data: JSON.stringify({
            challenge: getStorage().authTicket,
            identification: password
        })
    }).done((data) => {
        var storage = getStorage();
        if (data.success) {
            storage.authTokenExpires = new Date(new Date().getTime() + data.expires * 60 * 1000);
            storage.authToken = data.authToken;
        }
    }).fail((jqXHR) => {
        // add an error label
    });
}

function verifyAuthentication() {
    return $.ajax({
        type: 'GET',
        url: '/auth/check',
        data: {
            jwt: getAuthToken()
        },
        cache: false
    });
}

if (!window.location.href.includes("login")) {
    verifyAuthentication().fail(() => window.location = "/login.html");
}
