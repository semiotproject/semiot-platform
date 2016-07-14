(function() {

    $.get('/config/system').then(function() {
        console.info("looks like platform is configured");
        $(".save-platform-settings-button").attr({
            disabled: "disabled",
            title: "System is already configured"
        });
        // $(".platform-configured-helper").show();
    }, function() {
        console.info("looks like platform is not configured");
        $(".install-button, .upload-button").attr({
            disabled: "disabled",
            title: "You should configure system at first"
        });
        // $(".platform-not-configured-helper").show();
    });

})();