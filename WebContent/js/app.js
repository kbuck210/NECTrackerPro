$(document).foundation();

$(function () {
    $('a.disabled').on("click", function (e) {
        e.preventDefault();
    });
});
