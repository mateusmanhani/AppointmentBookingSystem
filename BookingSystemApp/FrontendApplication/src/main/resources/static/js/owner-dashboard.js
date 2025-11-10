// js/owner-dashboard.js - Owner dashboard functionality
class OwnerDashboard {
    constructor() {
        this.shops = [];
        this.createShopModal = null;
        this.SHOP_SERVICE_URL = 'http://localhost:8082'; // shop-service URL
    }

    async init() {
        // Ensure user is authenticated and has SHOP_OWNER role
        const auth = AuthGuard.requireAuth();
        if (!auth) return;

        // Check if user has SHOP_OWNER role
        const user = this.getCurrentUser();
        console.log('Current user:', user); // Debug log
        
        // Check both 'role' (singular) and 'roles' (plural) for compatibility
        const userRole = user?.role || user?.roles;
        const isShopOwner = userRole === 'SHOP_OWNER' || 
                           (Array.isArray(userRole) && userRole.includes('SHOP_OWNER'));
        
        if (!user || !isShopOwner) {
            this.showError('Access denied. You must be a shop owner to access this page.');
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 2000);
            return;
        }

        // Initialize modal
        this.createShopModal = new bootstrap.Modal(document.getElementById('createShopModal'));

        // Load shops
        await this.loadShops();

        // Setup event listeners
        this.setupEventListeners();
    }

    getCurrentUser() {
        try {
            const userStr = localStorage.getItem('barberbook_user');
            return userStr ? JSON.parse(userStr) : null;
        } catch (e) {
            console.error('Error parsing user data:', e);
            return null;
        }
    }

    getAuthToken() {
        return localStorage.getItem('barberbook_token');
    }

    async loadShops() {
        const loadingEl = document.getElementById('shopsLoading');
        const noShopsEl = document.getElementById('noShops');
        const shopsListEl = document.getElementById('shopsList');

        loadingEl.classList.remove('d-none');
        noShopsEl.classList.add('d-none');
        shopsListEl.innerHTML = '';

        try {
            // TODO: Replace with actual API call when backend is ready
            const shops = await this.fetchOwnerShops();
            
            this.shops = shops;
            loadingEl.classList.add('d-none');

            if (shops.length === 0) {
                noShopsEl.classList.remove('d-none');
            } else {
                this.renderShops(shops);
                this.updateStats(shops);
            }
        } catch (error) {
            console.error('Error loading shops:', error);
            loadingEl.classList.add('d-none');
            this.showError('Failed to load shops. Please try again later.');
        }
    }

    async fetchOwnerShops() {
        // Fetch shops from shop-service API with JWT authentication
        console.log('Fetching shops from API - GET /api/shops/my-shops');
        
        try {
            const token = this.getAuthToken();
            if (!token) {
                throw new Error('No authentication token found');
            }

            const response = await fetch(`${this.SHOP_SERVICE_URL}/api/shops/my-shops`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `HTTP ${response.status}: Failed to fetch shops`);
            }

            const shops = await response.json();
            console.log('Successfully fetched shops:', shops);
            return shops;
            
        } catch (error) {
            console.error('Error fetching shops:', error);
            
            // If backend not ready, use mock data
            console.warn('API call failed. Using mock data for testing.');
            return [
                {
                    id: 1,
                    name: 'Premium Cuts Downtown',
                    address: '123 Main St',
                    city: 'New York',
                    state: 'NY',
                    zipCode: '10001',
                    phone: '+1234567890',
                    description: 'High-end barbershop in downtown',
                    openingTime: '09:00',
                    closingTime: '18:00',
                    servicesCount: 0,
                    employeesCount: 0
                }
            ];
        }
    }

    renderShops(shops) {
        const container = document.getElementById('shopsList');
        container.innerHTML = '';

        shops.forEach(shop => {
            const shopCard = this.createShopCard(shop);
            container.appendChild(shopCard);
        });
    }

    createShopCard(shop) {
        const col = document.createElement('div');
        col.className = 'col-md-6 col-lg-4';

        const fullAddress = `${shop.address}, ${shop.city}, ${shop.state} ${shop.zipCode}`;

        col.innerHTML = `
            <div class="shop-card border rounded-3 p-3 h-100">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div>
                        <h5 class="fw-bold mb-1">${this.escapeHtml(shop.name)}</h5>
                        <p class="text-muted small mb-0">
                            <i class="fas fa-map-marker-alt me-1"></i>${this.escapeHtml(shop.city)}, ${this.escapeHtml(shop.state)}
                        </p>
                    </div>
                    <span class="badge bg-success">Active</span>
                </div>
                
                <div class="shop-details mb-3">
                    <p class="small mb-2">
                        <i class="fas fa-phone text-muted me-2"></i>${this.escapeHtml(shop.phone)}
                    </p>
                    <p class="small mb-2">
                        <i class="fas fa-clock text-muted me-2"></i>${shop.openingTime} - ${shop.closingTime}
                    </p>
                </div>

                <div class="shop-stats mb-3">
                    <div class="row g-2 text-center">
                        <div class="col-4">
                            <div class="stat-box bg-light rounded p-2">
                                <div class="fw-bold text-primary">${shop.servicesCount || 0}</div>
                                <div class="small text-muted">Services</div>
                            </div>
                        </div>
                        <div class="col-4">
                            <div class="stat-box bg-light rounded p-2">
                                <div class="fw-bold text-info">${shop.employeesCount || 0}</div>
                                <div class="small text-muted">Employees</div>
                            </div>
                        </div>
                        <div class="col-4">
                            <div class="stat-box bg-light rounded p-2">
                                <div class="fw-bold text-warning">0</div>
                                <div class="small text-muted">Bookings</div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="d-grid gap-2">
                    <button class="btn btn-primary" onclick="ownerDashboard.manageShop(${shop.id})">
                        <i class="fas fa-cog me-2"></i>Manage Shop
                    </button>
                </div>
            </div>
        `;

        return col;
    }

    updateStats(shops) {
        const totalShops = shops.length;
        const totalServices = shops.reduce((sum, shop) => sum + (shop.servicesCount || 0), 0);
        const totalEmployees = shops.reduce((sum, shop) => sum + (shop.employeesCount || 0), 0);
        const totalBookings = 0; // TODO: Implement when bookings API is ready

        document.getElementById('totalShops').textContent = totalShops;
        document.getElementById('totalServices').textContent = totalServices;
        document.getElementById('totalEmployees').textContent = totalEmployees;
        document.getElementById('totalBookings').textContent = totalBookings;
    }

    showCreateShopModal() {
        // Clear form
        document.getElementById('createShopForm').reset();
        document.getElementById('createShopMessage').innerHTML = '';
        
        // Show modal
        this.createShopModal.show();
    }

    async createShop() {
        const btn = document.getElementById('createShopBtn');
        const originalContent = btn.innerHTML;

        // Get form data
        const formData = {
            name: document.getElementById('shopName').value.trim(),
            phone: document.getElementById('shopPhone').value.trim(),
            address: document.getElementById('shopAddress').value.trim(),
            city: document.getElementById('shopCity').value.trim(),
            state: document.getElementById('shopState').value.trim(),
            zipCode: document.getElementById('shopZip').value.trim(),
            description: document.getElementById('shopDescription').value.trim(),
            openingTime: document.getElementById('shopOpenTime').value,
            closingTime: document.getElementById('shopCloseTime').value
        };

        // Validate
        if (!this.validateShopForm(formData)) {
            return;
        }

        // Show loading
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Creating...';

        try {
            const token = this.getAuthToken();
            if (!token) {
                throw new Error('No authentication token found');
            }

            console.log('Creating shop via API - POST /api/shops', formData);
            
            const response = await fetch(`${this.SHOP_SERVICE_URL}/api/shops`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            if (!response.ok) {
                const error = await response.json().catch(() => ({}));
                throw new Error(error.message || `HTTP ${response.status}: Failed to create shop`);
            }

            const newShop = await response.json();
            console.log('Shop created successfully:', newShop);

            this.showMessage('createShopMessage', 'Shop created successfully!', 'success');
            
            // Reload shops after a short delay
            setTimeout(() => {
                this.createShopModal.hide();
                this.loadShops();
            }, 1500);

        } catch (error) {
            console.error('Error creating shop:', error);
            this.showMessage('createShopMessage', error.message || 'Failed to create shop', 'danger');
        } finally {
            btn.disabled = false;
            btn.innerHTML = originalContent;
        }
    }

    validateShopForm(data) {
        if (!data.name || !data.phone || !data.address || !data.city || !data.state || !data.zipCode) {
            this.showMessage('createShopMessage', 'Please fill in all required fields', 'danger');
            return false;
        }

        // Validate phone format
        const phonePattern = /^[+]?[0-9]{10,15}$/;
        if (!phonePattern.test(data.phone)) {
            this.showMessage('createShopMessage', 'Please enter a valid phone number', 'danger');
            return false;
        }

        // Validate times
        if (data.openingTime >= data.closingTime) {
            this.showMessage('createShopMessage', 'Closing time must be after opening time', 'danger');
            return false;
        }

        return true;
    }

    showMessage(elementId, message, type = 'info') {
        const element = document.getElementById(elementId);
        element.innerHTML = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
    }

    showError(message) {
        const container = document.getElementById('shopsList');
        container.innerHTML = `
            <div class="col-12">
                <div class="alert alert-danger" role="alert">
                    <i class="fas fa-exclamation-triangle me-2"></i>${message}
                </div>
            </div>
        `;
    }

    manageShop(shopId) {
        // Navigate to shop management page
        window.location.href = `owner-shop-detail.html?shopId=${shopId}`;
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    setupEventListeners() {
        // Create shop button
        document.getElementById('createShopBtn').addEventListener('click', () => {
            this.createShop();
        });

        // Form enter key handler
        document.getElementById('createShopForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.createShop();
        });
    }
}

// Initialize dashboard when page loads
let ownerDashboard;
document.addEventListener('DOMContentLoaded', async function() {
    ownerDashboard = new OwnerDashboard();
    await ownerDashboard.init();
});

// Global function for button onclick
function showCreateShopModal() {
    if (ownerDashboard) {
        ownerDashboard.showCreateShopModal();
    }
}
