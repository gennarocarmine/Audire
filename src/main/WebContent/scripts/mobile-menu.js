document.addEventListener('DOMContentLoaded', () => {
    const menuToggleBtn = document.querySelector('.mobile-menu-toggle');
    const mainNav = document.querySelector('.main-nav');
    const navOverlay = document.querySelector('.nav-overlay');
    const body = document.body;

    if (!menuToggleBtn || !mainNav || !navOverlay) {
        console.error('Mobile menu elements not found. Please check your HTML structure.');
        return;
    }

    const toggleIcon = menuToggleBtn.querySelector('i');

    const toggleMenu = () => {
        const isOpen = mainNav.classList.toggle('is-open');

        navOverlay.classList.toggle('is-visible', isOpen);

        body.classList.toggle('no-scroll', isOpen);

        if (isOpen) {
            toggleIcon.classList.remove('fa-bars');
            toggleIcon.classList.add('fa-times');
            menuToggleBtn.setAttribute('aria-label', 'Chiudi menu');
        } else {
            toggleIcon.classList.remove('fa-times');
            toggleIcon.classList.add('fa-bars');
            menuToggleBtn.setAttribute('aria-label', 'Apri menu');
        }
    };

    menuToggleBtn.addEventListener('click', toggleMenu);

    navOverlay.addEventListener('click', toggleMenu);
});
