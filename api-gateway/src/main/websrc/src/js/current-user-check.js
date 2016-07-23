(function() {

    $.get('/user').then(function(res) {
        if (res.role === "user") {
            if (location.pathname === "/index") {
                location.href = "/systems";
            } else {
                $('.config-menu').hide();
            }
        }
    });

})();