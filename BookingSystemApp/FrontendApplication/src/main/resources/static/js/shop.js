// js/shop.js - Shop page specific functionality

class ShopPageManager {
    constructor() {
        this.shopId = null;
        this.shop = null;
        this.services = [];
        this.SHOP_SERVICE_URL = 'http://localhost:8082';
    }

    async init() {
        // Get shopId from URL parameter
        const urlParams = new URLSearchParams(window.location.search);
        this.shopId = urlParams.get('shopId');

        if (!this.shopId) {
            console.error('No shopId provided in URL');
            this.showError('Shop not found. Please select a shop from the listing page.');
            return;
        }

        // Load shop data
        await this.loadShop();
        await this.loadServices();
        await this.loadEmployees();
        
        // Setup interactions
        this.setupServiceCards();
    }

    async loadShop() {
        try {
            console.log(`Fetching shop data - GET /api/shops/${this.shopId}`);
            
            const response = await fetch(`${this.SHOP_SERVICE_URL}/api/shops/${this.shopId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: Failed to fetch shop`);
            }

            this.shop = await response.json();
            console.log('Shop loaded:', this.shop);
            
            this.renderShopHeader();
            this.loadMapEmbed();
            
        } catch (error) {
            console.error('Error loading shop:', error);
            this.showError('Unable to load shop details. Please try again later.');
        }
    }

    async loadServices() {
        try {
            console.log(`Fetching services - GET /api/shops/${this.shopId}/services`);
            
            const response = await fetch(`${this.SHOP_SERVICE_URL}/api/shops/${this.shopId}/services`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: Failed to fetch services`);
            }

            this.services = await response.json();
            console.log('Services loaded:', this.services.length);
            
            this.renderServices();
            
        } catch (error) {
            console.error('Error loading services:', error);
            // Services are optional, don't show error
        }
    }

    renderShopHeader() {
        if (!this.shop) return;

        // Update page title
        document.title = `${this.shop.name} - BarberBook`;

        // Update shop name
        const nameEl = document.querySelector('.shop-header h1');
        if (nameEl) {
            nameEl.textContent = this.shop.name;
        }

        // Update shop details
        const detailsContainer = document.querySelector('.shop-details .d-flex');
        if (detailsContainer) {
            const cityState = [this.shop.city, this.shop.state].filter(Boolean).join(', ');
            const fullAddress = [this.shop.address, cityState].filter(Boolean).join(', ');
            const hours = this.shop.openingTime && this.shop.closingTime 
                ? `${this.shop.openingTime} - ${this.shop.closingTime}`
                : 'Hours not available';

            detailsContainer.innerHTML = `
                <span class="text-muted">
                    <i class="fas fa-map-marker-alt me-2"></i>${this.escapeHtml(fullAddress)}
                </span>
                <span class="text-success fw-bold">
                    <i class="fas fa-clock me-2"></i>Open ${this.escapeHtml(hours)}
                </span>
            `;
        }

        // Update phone and directions buttons
        const phoneBtn = document.querySelector('button[onclick*="callShop"]');
        if (phoneBtn && this.shop.phone) {
            phoneBtn.onclick = () => window.open(`tel:${this.shop.phone}`, '_self');
        }

        const directionsBtn = document.querySelector('button[onclick*="getDirections"]');
        if (directionsBtn) {
            const address = [this.shop.address, this.shop.city, this.shop.state, this.shop.zipCode]
                .filter(Boolean).join(', ');
            directionsBtn.onclick = () => {
                const encoded = encodeURIComponent(address);
                window.open(`https://www.google.com/maps/search/${encoded}`, '_blank');
            };
        }

        // Add description if available
        if (this.shop.description) {
            const headerEl = document.querySelector('.shop-header .row');
            const descriptionEl = document.createElement('div');
            descriptionEl.className = 'col-12 mt-3';
            descriptionEl.innerHTML = `
                <p class="text-muted mb-0">${this.escapeHtml(this.shop.description)}</p>
            `;
            headerEl.appendChild(descriptionEl);
        }
    }

    loadMapEmbed() {
        if (!this.shop || !this.shop.latitude || !this.shop.longitude) {
            console.log('No coordinates available for map');
            return;
        }

        const mapContainer = document.getElementById('shopMapEmbed');
        if (!mapContainer) return;

        // Create Google Maps embed URL
        const mapUrl = `https://www.google.com/maps?q=${this.shop.latitude},${this.shop.longitude}&z=15&output=embed`;

        // Create iframe element
        mapContainer.innerHTML = `
            <iframe
                src="${mapUrl}"
                loading="lazy"
                referrerpolicy="no-referrer-when-downgrade"
                allowfullscreen>
            </iframe>
        `;

        console.log('Map embed loaded for shop at:', this.shop.latitude, this.shop.longitude);
    }

    renderServices() {
        const servicesContainer = document.querySelector('.services-section .row');
        if (!servicesContainer) return;

        if (this.services.length === 0) {
            servicesContainer.innerHTML = `
                <div class="col-12 text-center py-5">
                    <i class="fas fa-scissors fa-3x text-muted mb-3"></i>
                    <p class="text-muted">No services available at this time.</p>
                </div>
            `;
            return;
        }

        servicesContainer.innerHTML = '';
        
        this.services.forEach(service => {
            const serviceCard = this.createServiceCard(service);
            servicesContainer.appendChild(serviceCard);
        });
    }

    createServiceCard(service) {
        const col = document.createElement('div');
        col.className = 'col-md-6 col-lg-4';

        const price = service.price ? `$${service.price.toFixed(2)}` : 'Price not set';
        const duration = service.duration ? `${service.duration} min` : 'Duration varies';
        const description = service.description || 'No description available';

        col.innerHTML = `
            <div class="service-card border rounded-3 p-3 h-100">
                <div class="service-header d-flex justify-content-between align-items-start mb-3">
                    <div>
                        <h4 class="fw-bold mb-1">${this.escapeHtml(service.name)}</h4>
                        <p class="text-muted small mb-0">${this.escapeHtml(description)}</p>
                    </div>
                    <div class="service-icon bg-primary text-white rounded-circle d-flex align-items-center justify-content-center" style="width: 40px; height: 40px;">
                        <i class="fas fa-cut"></i>
                    </div>
                </div>
                <div class="service-details mb-3">
                    <div class="d-flex justify-content-between align-items-center">
                        <span class="price fw-bold text-primary fs-5">${price}</span>
                        <span class="duration text-muted">
                            <i class="fas fa-clock me-1"></i>${duration}
                        </span>
                    </div>
                </div>
                <button class="btn btn-primary w-100" onclick="shopPageManager.handleBookNow(${service.id})">
                    <i class="fas fa-calendar-plus me-2"></i>Book Now
                </button>
            </div>
        `;

        return col;
    }

    async loadEmployees() {
        const loadingEl = document.getElementById('employeesLoading');
        const noEmployeesEl = document.getElementById('noEmployees');
        const gridEl = document.getElementById('employeesGrid');

        if (!loadingEl || !noEmployeesEl || !gridEl) return;

        loadingEl.classList.remove('d-none');
        noEmployeesEl.classList.add('d-none');
        gridEl.innerHTML = '';

        try {
            console.log(`Fetching employees - GET /api/shops/${this.shopId}/employees`);
            
            const response = await fetch(`${this.SHOP_SERVICE_URL}/api/shops/${this.shopId}/employees`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: Failed to fetch employees`);
            }

            this.employees = await response.json();
            console.log('Employees loaded:', this.employees.length);
            
            loadingEl.classList.add('d-none');

            if (this.employees.length === 0) {
                noEmployeesEl.classList.remove('d-none');
            } else {
                this.renderEmployees();
            }
            
        } catch (error) {
            console.error('Error loading employees:', error);
            loadingEl.classList.add('d-none');
            // Show empty state on error
            noEmployeesEl.classList.remove('d-none');
        }
    }

    renderEmployees() {
        const gridEl = document.getElementById('employeesGrid');
        if (!gridEl) return;

        gridEl.innerHTML = '';
        
        const colors = ['primary', 'success', 'warning', 'info', 'danger', 'secondary'];
        
        this.employees.forEach((employee, index) => {
            const employeeCard = this.createEmployeeCard(employee, colors[index % colors.length]);
            gridEl.appendChild(employeeCard);
        });
    }

    createEmployeeCard(employee, colorClass) {
        const col = document.createElement('div');
        col.className = 'col-md-4';

        const role = employee.role || 'Team Member';
        const email = employee.email || '';
        const phone = employee.phone || '';

        col.innerHTML = `
            <div class="barber-card text-center">
                <div class="barber-avatar bg-${colorClass} text-white rounded-circle mx-auto mb-3 d-flex align-items-center justify-content-center" style="width: 80px; height: 80px;">
                    <i class="fas fa-user fs-2"></i>
                </div>
                <h4 class="fw-bold mb-2">${this.escapeHtml(employee.name)}</h4>
                <p class="text-${colorClass} fw-bold mb-2">${this.escapeHtml(role)}</p>
                ${email ? `<p class="text-muted small mb-1"><i class="fas fa-envelope me-1"></i>${this.escapeHtml(email)}</p>` : ''}
                ${phone ? `<p class="text-muted small mb-0"><i class="fas fa-phone me-1"></i>${this.escapeHtml(phone)}</p>` : ''}
            </div>
        `;

        return col;
    }

    setupServiceCards() {
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

    handleBookNow(serviceId) {
        // Check if user is logged in
        const token = localStorage.getItem('barberbook_token');
        
        if (!token) {
            alert('Please login to book an appointment');
            window.location.href = `login.html?redirect=shop.html?shopId=${this.shopId}`;
            return;
        }

        // Redirect to booking page with shop and service info
        window.location.href = `booking.html?shopId=${this.shopId}&serviceId=${serviceId}`;
    }

    showError(message) {
        const container = document.querySelector('.container.my-4');
        if (container) {
            container.innerHTML = `
                <div class="alert alert-danger" role="alert">
                    <i class="fas fa-exclamation-triangle me-2"></i>
                    ${this.escapeHtml(message)}
                </div>
                <div class="text-center mt-4">
                    <a href="shops.html" class="btn btn-primary">
                        <i class="fas fa-arrow-left me-2"></i>Back to Shop Listing
                    </a>
                </div>
            `;
        }
    }

    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    scrollToServices() {
        document.querySelector('.services-section')?.scrollIntoView({
            behavior: 'smooth'
        });
    }
}

// Initialize when DOM is loaded
let shopPageManager;
document.addEventListener('DOMContentLoaded', () => {
    shopPageManager = new ShopPageManager();
    shopPageManager.init();
});

// Keep legacy functions for backwards compatibility
function callShop() {
    shopPageManager?.shop?.phone && window.open(`tel:${shopPageManager.shop.phone}`, '_self');
}

function getDirections() {
    if (shopPageManager?.shop) {
        const address = [
            shopPageManager.shop.address,
            shopPageManager.shop.city || 'Dublin',
            'Ireland'
        ].filter(Boolean).join(', ');
        
        // Use Google Maps Directions API for better routing with optional starting point
        const directionsUrl = `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(address)}`;
        
        window.open(directionsUrl, '_blank');
    }
}

function scrollToServices() {
    shopPageManager?.scrollToServices();
}

function handleBookNow(serviceId) {
    shopPageManager?.handleBookNow(serviceId);
}
