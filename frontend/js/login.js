/**
 * Created by jko on 17.04.2017.
 */

function loadPage(url) {

    return $.ajax({
        type: 'GET',
        url: '/views/pages/login/' + url,
        dataType: 'html',
        cache: false,
        async: false,
        beforeSend: function () {
            $("#login-content").css({opacity: 0});
        },
        success: function (data) {
            $('html, body').animate({scrollTop: 0}, 0);
            $("#login-content").html(data).delay(250).animate({opacity: 1}, 0);
            window.location.hash = url;
        },
        error: function (e) {
            console.log("error" + e);
            resetView();
        }

    });
}

function resetView() {
    loadPage("id.html").done(() => {
        setupEnterUserID();
    });
}

function setupEnterUserID() {
    $("#submit-cldbid").click((e) => {
        e.preventDefault();

        obtainTicket($("#input-cldbid").val())
            .done((data) => {
                loadPage("password.html").done(() => setupEnterPassword());
            })
            .fail((data) => {
                console.log("got error " + JSON.stringify(data));
                resetView();
            });
    });
}

function setupEnterPassword() {
    $("#submit-identifier").click((e) => {
        e.preventDefault();

        obtainToken($("#input-identifier").val()).done((data) => {
            if (data.success) {
                window.location = "/index.html";
            }
            else {
                resetView();
            }
        }).fail(() => resetView());
    });
}

resetView();
