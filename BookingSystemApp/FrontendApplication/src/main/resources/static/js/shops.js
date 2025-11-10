// js/shops.js - Public shop listing functionality
class ShopsManager {
    constructor() {
        this.shops = [];
        this.filteredShops = [];
        this.SHOP_SERVICE_URL = 'http://localhost:8082';
    }

    async init() {
        // Setup event listeners
        this.setupEventListeners();
        
        // Load shops
        await this.loadShops();
    }

    setupEventListeners() {
        // Search input with debounce
        const searchInput = document.getElementById('searchInput');
        let searchTimeout;
        
        searchInput.addEventListener('input', (e) => {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                this.filterShops(e.target.value);
            }, 300); // Debounce 300ms
        });

        // Clear search button
        document.getElementById('clearSearchBtn').addEventListener('click', () => {
            searchInput.value = '';
            this.filterShops('');
        });
    }

    async loadShops() {
        const loadingEl = document.getElementById('loadingState');
        const errorEl = document.getElementById('errorState');
        const emptyEl = document.getElementById('emptyState');
        const gridEl = document.getElementById('shopsGrid');

        loadingEl.classList.remove('d-none');
        errorEl.classList.add('d-none');
        emptyEl.classList.add('d-none');
        gridEl.innerHTML = '';

        try {
            console.log('Fetching shops - GET /api/shops');
            
            const response = await fetch(`${this.SHOP_SERVICE_URL}/api/shops?page=0&size=100`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: Failed to fetch shops`);
            }

            const data = await response.json();
            
            // Handle paginated response
            if (data.content) {
                this.shops = data.content;
            } else if (Array.isArray(data)) {
                this.shops = data;
            } else {
                this.shops = [];
            }

            console.log('Shops loaded:', this.shops.length);

            loadingEl.classList.add('d-none');

            if (this.shops.length === 0) {
                emptyEl.classList.remove('d-none');
            } else {
                this.filteredShops = [...this.shops];
                this.renderShops();
            }

        } catch (error) {
            console.error('Error loading shops:', error);
            loadingEl.classList.add('d-none');
            errorEl.classList.remove('d-none');
            document.getElementById('errorMessage').textContent = error.message || 'Failed to load shops';
        }
    }

    filterShops(searchTerm) {
        const term = searchTerm.toLowerCase().trim();

        if (!term) {
            this.filteredShops = [...this.shops];
        } else {
            this.filteredShops = this.shops.filter(shop => {
                const name = (shop.name || '').toLowerCase();
                const city = (shop.city || '').toLowerCase();
                const state = (shop.state || '').toLowerCase();
                const address = (shop.address || '').toLowerCase();
                const description = (shop.description || '').toLowerCase();

                return name.includes(term) || 
                       city.includes(term) || 
                       state.includes(term) || 
                       address.includes(term) || 
                       description.includes(term);
            });
        }

        this.renderShops();
    }

    renderShops() {
        const gridEl = document.getElementById('shopsGrid');
        const emptyEl = document.getElementById('emptyState');
        const countEl = document.getElementById('shopCount');

        gridEl.innerHTML = '';
        countEl.textContent = this.filteredShops.length;

        if (this.filteredShops.length === 0) {
            emptyEl.classList.remove('d-none');
        } else {
            emptyEl.classList.add('d-none');
            this.filteredShops.forEach(shop => {
                const shopCard = this.createShopCard(shop);
                gridEl.appendChild(shopCard);
            });
        }
    }

    createShopCard(shop) {
        const col = document.createElement('div');
        col.className = 'col-md-6 col-lg-4';

        const hasHours = shop.openingTime && shop.closingTime;
        const hoursText = hasHours ? `${shop.openingTime} - ${shop.closingTime}` : 'Hours not available';
        
        const cityState = [shop.city, shop.state].filter(Boolean).join(', ') || 'Location not specified';
        const phone = shop.phone || 'No phone listed';
        const description = shop.description || 'No description available';

        col.innerHTML = `
            <div class="shop-card card h-100 shadow-sm" data-shop-id="${shop.id}">
                <div class="card-body d-flex flex-column">
                    <!-- Shop Header -->
                    <div class="d-flex align-items-start mb-3">
                        <div class="shop-icon bg-primary text-white rounded-circle me-3 d-flex align-items-center justify-content-center">
                            <i class="fas fa-store-alt"></i>
                        </div>
                        <div class="flex-grow-1">
                            <h5 class="card-title fw-bold mb-1">${this.escapeHtml(shop.name)}</h5>
                            <p class="text-muted small mb-0">
                                <i class="fas fa-map-marker-alt me-1"></i>${this.escapeHtml(cityState)}
                            </p>
                        </div>
                    </div>

                    <!-- Shop Details -->
                    <div class="shop-details mb-3 flex-grow-1">
                        <p class="text-muted small mb-2">
                            <i class="fas fa-location-dot me-2"></i>${this.escapeHtml(shop.address)}
                        </p>
                        <p class="text-muted small mb-2">
                            <i class="fas fa-clock me-2"></i>${this.escapeHtml(hoursText)}
                        </p>
                        <p class="text-muted small mb-2">
                            <i class="fas fa-phone me-2"></i>${this.escapeHtml(phone)}
                        </p>
                        <p class="text-muted small mb-0" style="max-height: 3em; overflow: hidden;">
                            ${this.escapeHtml(description)}
                        </p>
                    </div>

                    <!-- Shop Actions -->
                    <div class="d-grid">
                        <button class="btn btn-primary" onclick="shopsManager.viewShop(${shop.id})">
                            <i class="fas fa-calendar-check me-2"></i>View Shop & Book
                        </button>
                    </div>
                </div>

                <!-- Hover Effect Border -->
                <div class="shop-card-border"></div>
            </div>
        `;

        return col;
    }

    viewShop(shopId) {
        console.log('Navigating to shop:', shopId);
        window.location.href = `shop.html?shopId=${shopId}`;
    }

    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Initialize when DOM is loaded
let shopsManager;
document.addEventListener('DOMContentLoaded', () => {
    shopsManager = new ShopsManager();
    shopsManager.init();
});
