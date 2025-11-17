// frontend/src/main/resources/static/js/app.js


window.CONFIG = window.CONFIG || {
    API_BASE_URL: 'http://localhost:8081/api',
    FRONTEND_URL: 'http://localhost:8080',
    TOKEN_KEY: 'barberbook_token',
    USER_KEY: 'barberbook_user'
};

// Utility function for API calls with JWT authentication
async function apiCall(endpoint, options = {}) {
    const token = localStorage.getItem(window.CONFIG.TOKEN_KEY);

    const config = {
        headers: {
            'Content-Type': 'application/json',
            ...(token && { 'Authorization': `Bearer ${token}` }),
            ...options.headers
        },
        ...options
    };

    try {
    const response = await fetch(`${window.CONFIG.API_BASE_URL}${endpoint}`, config);

        // Handle different content types
        const contentType = response.headers.get('content-type');
        let data;

        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
        } else {
            data = { message: await response.text() };
        }

        if (!response.ok) {
            // Log the full response body for easier debugging of server errors
            console.error('API response error body:', data);
            throw new Error(data.message || `HTTP ${response.status}: ${response.statusText}`);
        }

        return data;

    } catch (error) {
        console.error('API Call Error:', error);

        // Handle network errors
        if (error instanceof TypeError) {
            throw new Error('Network error: Please check if the backend server is running.');
        }

        throw error;
    }
}

// Handle Book Now buttons - this is the main function for service booking
function handleBookNow(serviceId) {
    console.log('Book Now clicked for service:', serviceId);

    // Check if user is authenticated
    const auth = AuthGuard.checkAuth();

    if (!auth) {
        console.log('User not authenticated, redirecting to login');
        // Store service selection for after login
        localStorage.setItem('selectedService', serviceId);
        localStorage.setItem('redirectAfterLogin', 'booking.html');
        window.location.href = 'login.html';
        return;
    }

    console.log('User authenticated, proceeding to booking');
    // User is authenticated, proceed to booking
    window.location.href = `booking.html?service=${serviceId}`;
}

// General utility functions
function showMessage(elementId, message, type = 'danger') {
    const messageDiv = document.getElementById(elementId);
    if (messageDiv) {
        messageDiv.innerHTML = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
    }
}

// Get current user info
function getCurrentUser() {
    const userStr = localStorage.getItem(window.CONFIG.USER_KEY);
    return userStr ? JSON.parse(userStr) : null;
}

// Initialize app on page load
document.addEventListener('DOMContentLoaded', function() {
    console.log('App initialized');

    // Update navigation based on auth state
    updateNavigation();

    // Handle any redirects after login
    handlePostLoginRedirect();
});

function updateNavigation() {
    const auth = AuthGuard.checkAuth();
    const navLinks = document.querySelector('.navbar-nav');

    if (auth && navLinks) {
        // User is logged in, update navigation
        const loginLink = navLinks.querySelector('a[href="login.html"]');
        if (loginLink) {
            loginLink.innerHTML = '<i class="fas fa-tachometer-alt me-1"></i>Dashboard';
            loginLink.href = 'dashboard.html';
        }

        // Add logout option
        const logoutLink = document.createElement('a');
        logoutLink.className = 'nav-link';
        logoutLink.href = '#';
        logoutLink.innerHTML = '<i class="fas fa-sign-out-alt me-1"></i>Logout';
        logoutLink.onclick = function(e) {
            e.preventDefault();
            AuthGuard.logout();
        };
        navLinks.appendChild(logoutLink);
    }
}

function handlePostLoginRedirect() {
    const urlParams = new URLSearchParams(window.location.search);
    const selectedService = localStorage.getItem('selectedService');

    // If user just logged in and had selected a service
    if (selectedService && window.location.pathname.includes('booking.html')) {
        console.log('Handling post-login redirect for service:', selectedService);
        // Service selection logic can be added here
        localStorage.removeItem('selectedService');
    }
}

// Search functionality (for future use)
function handleSearch(query) {
    console.log('Searching for:', query);
    // Add search logic here
}

// Load nearby shops (for future use)
async function loadNearbyShops() {
    try {
        const shops = await apiCall('/shops/nearby');
        console.log('Loaded shops:', shops);
        return shops;
    } catch (error) {
        console.error('Failed to load shops:', error);
        return [];
    }
}
