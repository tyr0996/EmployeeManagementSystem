document.addEventListener('mousemove', function(event) {
    const sideMenu = document.querySelector('.side-menu');
    const content = document.querySelector('.content-layout');

    if (event.clientX <= 10) {
        sideMenu.classList.add('show');
        content.classList.add('shifted');
    } else if (!sideMenu.matches(':hover')) {
        sideMenu.classList.remove('show');
        content.classList.remove('shifted');
    };

    // Ha az egér elhagyja a menüt, akkor is zárjuk vissza
    // sideMenu.addEventListener('mouseleave', function () {
    //     sideMenu.classList.remove('show');
    //     content.classList.remove('shifted');
    // });
});
