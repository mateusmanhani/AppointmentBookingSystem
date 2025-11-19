// js/navbar.js - Dynamic Navigation Manager
class NavbarManager {
    static init() {
        const navbarContainer = document.getElementById('navbar');
        if (!navbarContainer) {
            // Fallback to old method for pages still using .navbar-nav
            const navbar = document.querySelector('.navbar-nav');
            if (navbar) {
                const auth = AuthGuard.checkAuth();
                this.updateNavigationItems(navbar, auth);
            }
            return;
        }

        const auth = AuthGuard.checkAuth();
        this.renderFullNavbar(navbarContainer, auth);
    }

    static renderFullNavbar(container, auth) {
        const navItems = this.getNavigationItems(auth);
        
        container.innerHTML = `
            <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
                <div class="container">
                    <a class="navbar-brand fw-bold" href="index.html">
                        <i class="fas fa-cut text-primary me-2"></i>BarberBook
                    </a>

                    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                        <span class="navbar-toggler-icon"></span>
                    </button>

                    <div class="collapse navbar-collapse" id="navbarNav">
                        <div class="navbar-nav ms-auto">
                            ${navItems}
                        </div>
                    </div>
                </div>
            </nav>
        `;
    }

    static getNavigationItems(auth) {
        if (auth) {
            // Check if user has SHOP_OWNER role - handle both 'role' (string) and 'roles' (array)
            const userRole = auth.user?.role || auth.user?.roles;
            const isShopOwner = userRole === 'SHOP_OWNER' || 
                               (Array.isArray(userRole) && userRole.includes('SHOP_OWNER'));
            
            console.log('Navbar - User role:', userRole, 'Is shop owner:', isShopOwner); // Debug log
            
            // Logged-in navigation
            let navContent = `
                <a class="nav-link text-muted" href="index.html">
                    <i class="fas fa-home me-1"></i>Home
                </a>
                <a class="nav-link text-muted" href="shops.html">
                    <i class="fas fa-store-alt me-1"></i>Browse Shops
                </a>
                <a class="nav-link text-muted" href="map.html">
                    <i class="fas fa-map-marked-alt me-1"></i>Shop Map
                </a>
                <a class="nav-link" href="dashboard.html">
                    <i class="fas fa-tachometer-alt me-1"></i>Dashboard
                </a>`;
            
            // Add Owner Dashboard link if user is a shop owner
            if (isShopOwner) {
                navContent += `
                <a class="nav-link" href="owner-dashboard.html">
                    <i class="fas fa-store me-1"></i>Owner Dashboard
                </a>`;
            }
            
            navContent += `
                <div class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown">
                        <i class="fas fa-user me-1"></i>${auth.user.firstName || 'User'}
                    </a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="dashboard.html">
                            <i class="fas fa-calendar me-2"></i>My Appointments
                        </a></li>`;
            
            // Add Owner Dashboard to dropdown as well
            if (isShopOwner) {
                navContent += `
                        <li><a class="dropdown-item" href="owner-dashboard.html">
                            <i class="fas fa-store me-2"></i>Owner Dashboard
                        </a></li>`;
            }
            
            navContent += `
                        <li><a class="dropdown-item" href="#" onclick="AuthGuard.logout()">
                            <i class="fas fa-sign-out-alt me-2"></i>Logout
                        </a></li>
                    </ul>
                </div>
            `;
            
            return navContent;
        } else {
            // Guest navigation
            return `
                <a class="nav-link text-muted" href="index.html">
                    <i class="fas fa-home me-1"></i>Home
                </a>
                <a class="nav-link text-muted" href="shops.html">
                    <i class="fas fa-store-alt me-1"></i>Browse Shops
                </a>
                <a class="nav-link text-muted" href="map.html">
                    <i class="fas fa-map-marked-alt me-1"></i>Shop Map
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

    // Legacy method for backward compatibility
    static updateNavigationItems(navbar, auth) {
        // Clear existing navigation
        navbar.innerHTML = '';
        navbar.innerHTML = this.getNavigationItems(auth);
    }

    static refresh() {
        this.init();
    }
}

// Auto-initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    NavbarManager.init();
});
