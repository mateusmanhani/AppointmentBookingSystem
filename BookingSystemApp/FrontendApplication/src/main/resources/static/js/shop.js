// js/shop.js - Shop page specific functionality

document.addEventListener('DOMContentLoaded', function() {
    console.log('Shop page loaded');
    initializeShopPage();
});

function initializeShopPage() {
    // Add any shop-specific initialization here
    setupServiceCards();
}

function setupServiceCards() {
    // Add hover effects to service cards
    const serviceCards = document.querySelectorAll('.service-card');
    serviceCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
            this.style.boxShadow = '0 4px 8px rgba(0,0,0,0.1)';
        });

        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = 'none';
        });
    });
}

// Helper functions for shop actions
function callShop() {
    window.open('tel:+15551234567', '_self');
}

function getDirections() {
    const address = encodeURIComponent('123 Main Street, Downtown, City');
    window.open(`https://www.google.com/maps/search/${address}`, '_blank');
}

function scrollToServices() {
    document.querySelector('.services-section').scrollIntoView({
        behavior: 'smooth'
    });
}

// Export function for global access
window.shopFunctions = {
    callShop,
    getDirections,
    scrollToServices
};
