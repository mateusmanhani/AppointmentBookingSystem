// js/navbar.js - Dynamic Navigation Manager
class NavbarManager {
    static init() {
        const navbar = document.querySelector('.navbar-nav');
        if (!navbar) return;

        const auth = AuthGuard.checkAuth();
        this.updateNavigation(navbar, auth);
    }

    static updateNavigation(navbar, auth) {
        // Clear existing navigation
        navbar.innerHTML = '';

        if (auth) {
            // Logged-in navigation
            navbar.innerHTML = `
                <a class="nav-link text-muted" href="index.html">
                    <i class="fas fa-home me-1"></i>Home
                </a>
                <a class="nav-link text-muted" href="shop.html">
                    <i class="fas fa-map-marker-alt me-1"></i>Shops
                </a>
                <a class="nav-link" href="dashboard.html">
                    <i class="fas fa-tachometer-alt me-1"></i>Dashboard
                </a>
                <div class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown">
                        <i class="fas fa-user me-1"></i>${auth.user.firstName || 'User'}
                    </a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="dashboard.html">
                            <i class="fas fa-calendar me-2"></i>My Appointments
                        </a></li>
                        <li><a class="dropdown-item" href="#" onclick="AuthGuard.logout()">
                            <i class="fas fa-sign-out-alt me-2"></i>Logout
                        </a></li>
                    </ul>
                </div>
            `;
        } else {
            // Guest navigation
            navbar.innerHTML = `
                <a class="nav-link text-muted" href="index.html">
                    <i class="fas fa-home me-1"></i>Home
                </a>
                <a class="nav-link text-muted" href="shop.html">
                    <i class="fas fa-map-marker-alt me-1"></i>Shops
                </a>
                <a class="nav-link" href="login.html">
                    <i class="fas fa-sign-in-alt me-1"></i>Login
                </a>
                <a class="nav-link btn btn-primary text-white ms-2 px-3" href="register.html">
                    Register
                </a>
            `;
        }
    }

    static refresh() {
        this.init();
    }
}

// Auto-initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    NavbarManager.init();
});
