// js/owner-shop-detail.js - Shop management functionality
class ShopDetailManager {
    constructor() {
        this.shopId = null;
        this.shop = null;
        this.services = [];
        this.employees = [];
        this.SHOP_SERVICE_URL = 'http://localhost:8082'; // shop-service URL
        
        // Modals
        this.editShopModal = null;
        this.addServiceModal = null;
        this.editServiceModal = null;
        this.addEmployeeModal = null;
        this.editEmployeeModal = null;
    }

    async init() {
        // Ensure user is authenticated
        const auth = AuthGuard.requireAuth();
        if (!auth) return;

        // Get shopId from URL
        const urlParams = new URLSearchParams(window.location.search);
        this.shopId = urlParams.get('shopId');

        if (!this.shopId) {
            alert('Shop ID not provided');
            window.location.href = 'owner-dashboard.html';
            return;
        }

        // Initialize modals
        this.editShopModal = new bootstrap.Modal(document.getElementById('editShopModal'));
        this.addServiceModal = new bootstrap.Modal(document.getElementById('addServiceModal'));
        this.editServiceModal = new bootstrap.Modal(document.getElementById('editServiceModal'));
        this.addEmployeeModal = new bootstrap.Modal(document.getElementById('addEmployeeModal'));
        this.editEmployeeModal = new bootstrap.Modal(document.getElementById('editEmployeeModal'));

        // Load shop data
        await this.loadShopData();
        await this.loadServices();
        await this.loadEmployees();

        // Setup event listeners
        this.setupEventListeners();
    }

    getAuthToken() {
        return localStorage.getItem('barberbook_token');
    }

    async loadShopData() {
        try {
            const token = this.getAuthToken();
            if (!token) {
                throw new Error('No authentication token found');
            }

            console.log(`Fetching shop data - GET /api/shops/${this.shopId}`);
            
            const response = await fetch(`${this.SHOP_SERVICE_URL}/api/shops/${this.shopId}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `HTTP ${response.status}: Failed to fetch shop`);
            }

            this.shop = await response.json();
            console.log('Shop data loaded:', this.shop);
            this.renderShopInfo();
            
        } catch (error) {
            console.error('Error loading shop data:', error);
            
            // Fallback to mock data for testing
            console.warn('Using mock data. Remove this when backend is ready.');
            this.shop = {
                id: this.shopId,
                name: 'Premium Cuts Downtown',
                address: '123 Main St',
                city: 'New York',
                state: 'NY',
                zipCode: '10001',
                phone: '+1234567890',
                description: 'High-end barbershop in downtown Manhattan',
                openingTime: '09:00',
                closingTime: '18:00'
            };
            this.renderShopInfo();
        }
    }

    renderShopInfo() {
        document.getElementById('shopName').textContent = this.shop.name;
        document.getElementById('shopAddress').textContent = 
            `${this.shop.address}, ${this.shop.city}, ${this.shop.state} ${this.shop.zipCode}`;

        // Overview tab
        document.getElementById('overviewPhone').textContent = this.shop.phone;
        document.getElementById('overviewHours').textContent = 
            `${this.shop.openingTime} - ${this.shop.closingTime}`;
        document.getElementById('overviewAddress').textContent = 
            `${this.shop.address}, ${this.shop.city}, ${this.shop.state} ${this.shop.zipCode}`;
        document.getElementById('overviewDescription').textContent = 
            this.shop.description || 'No description provided';
    }

    editShopInfo() {
        // Pre-fill form with current data
        document.getElementById('editShopName').value = this.shop.name;
        document.getElementById('editShopPhone').value = this.shop.phone;
        document.getElementById('editShopAddress').value = this.shop.address;
        document.getElementById('editShopCity').value = this.shop.city;
        document.getElementById('editShopState').value = this.shop.state;
        document.getElementById('editShopZip').value = this.shop.zipCode;
        document.getElementById('editShopDescription').value = this.shop.description || '';
        document.getElementById('editShopOpenTime').value = this.shop.openingTime;
        document.getElementById('editShopCloseTime').value = this.shop.closingTime;

        document.getElementById('editShopMessage').innerHTML = '';
        this.editShopModal.show();
    }

    async saveShopInfo() {
        const btn = document.getElementById('saveShopBtn');
        const originalContent = btn.innerHTML;

        const formData = {
            name: document.getElementById('editShopName').value.trim(),
            phone: document.getElementById('editShopPhone').value.trim(),
            address: document.getElementById('editShopAddress').value.trim(),
            city: document.getElementById('editShopCity').value.trim(),
            state: document.getElementById('editShopState').value.trim(),
            zipCode: document.getElementById('editShopZip').value.trim(),
            description: document.getElementById('editShopDescription').value.trim(),
            openingTime: document.getElementById('editShopOpenTime').value,
            closingTime: document.getElementById('editShopCloseTime').value
        };

        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Saving...';

        try {
            // TODO: Replace with actual API call
            console.log(`TODO: Update shop - PUT /api/shops/${this.shopId}`, formData);
            
            /* Uncomment when API is ready:
            const token = this.getAuthToken();
            const response = await fetch(`${this.API_BASE_URL}/shops/${this.shopId}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            if (!response.ok) {
                throw new Error('Failed to update shop');
            }

            this.shop = await response.json();
            */

            // Mock update
            await new Promise(resolve => setTimeout(resolve, 1000));
            this.shop = { ...this.shop, ...formData };

            this.renderShopInfo();
            this.showMessage('editShopMessage', 'Shop updated successfully!', 'success');
            
            setTimeout(() => {
                this.editShopModal.hide();
            }, 1500);

        } catch (error) {
            console.error('Error updating shop:', error);
            this.showMessage('editShopMessage', 'Failed to update shop', 'danger');
        } finally {
            btn.disabled = false;
            btn.innerHTML = originalContent;
        }
    }

    // ========== SERVICES MANAGEMENT ==========

    async loadServices() {
        const loadingEl = document.getElementById('servicesLoading');
        const noServicesEl = document.getElementById('noServices');
        const servicesListEl = document.getElementById('servicesList');

        loadingEl.classList.remove('d-none');
        noServicesEl.classList.add('d-none');
        servicesListEl.innerHTML = '';

        try {
            const token = this.getAuthToken();
            console.log(`Fetching services - GET /api/shops/${this.shopId}/services`);
            
            const response = await fetch(`${this.SHOP_SERVICE_URL}/api/shops/${this.shopId}/services`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`Failed to fetch services: ${response.status}`);
            }

            this.services = await response.json();
            console.log('Services loaded:', this.services);

            loadingEl.classList.add('d-none');

            if (this.services.length === 0) {
                noServicesEl.classList.remove('d-none');
            } else {
                this.renderServices();
            }

            // Update stats
            document.getElementById('overviewServicesCount').textContent = this.services.length;

        } catch (error) {
            console.error('Error loading services:', error);
            loadingEl.classList.add('d-none');
        }
    }

    renderServices() {
        const container = document.getElementById('servicesList');
        container.innerHTML = '';

        this.services.forEach(service => {
            const serviceCard = this.createServiceCard(service);
            container.appendChild(serviceCard);
        });
    }

    createServiceCard(service) {
        const col = document.createElement('div');
        col.className = 'col-md-6';

        col.innerHTML = `
            <div class="service-card border rounded-3 p-3">
                <div class="d-flex justify-content-between align-items-start mb-2">
                    <h6 class="fw-bold mb-0">${this.escapeHtml(service.name)}</h6>
                    <span class="badge bg-success">$${service.price.toFixed(2)}</span>
                </div>
                <p class="text-muted small mb-2">${this.escapeHtml(service.description || '')}</p>
                <div class="d-flex justify-content-between align-items-center">
                    <span class="small text-muted">
                        <i class="fas fa-clock me-1"></i>${service.duration} min
                    </span>
                    <div class="btn-group btn-group-sm">
                        <button class="btn btn-outline-primary" onclick="shopDetailManager.editService(${service.id})">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-outline-danger" onclick="shopDetailManager.deleteService(${service.id})">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            </div>
        `;

        return col;
    }

    showAddServiceModal() {
        document.getElementById('addServiceForm').reset();
        document.getElementById('addServiceMessage').innerHTML = '';
        this.addServiceModal.show();
    }

    async addService() {
        const btn = document.getElementById('saveServiceBtn');
        const originalContent = btn.innerHTML;

        const formData = {
            name: document.getElementById('serviceName').value.trim(),
            description: document.getElementById('serviceDescription').value.trim(),
            price: parseFloat(document.getElementById('servicePrice').value),
            duration: parseInt(document.getElementById('serviceDuration').value)
        };

        if (!formData.name || !formData.price || !formData.duration) {
            this.showMessage('addServiceMessage', 'Please fill in all required fields', 'danger');
            return;
        }

        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Adding...';

        try {
            const token = this.getAuthToken();
            console.log(`Creating service - POST /api/shops/${this.shopId}/services`, formData);
            
            const response = await fetch(`${this.SHOP_SERVICE_URL}/api/shops/${this.shopId}/services`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to create service: ${response.status}`);
            }

            const createdService = await response.json();
            console.log('Service created:', createdService);

            this.showMessage('addServiceMessage', 'Service added successfully!', 'success');
            
            setTimeout(() => {
                this.addServiceModal.hide();
                document.getElementById('addServiceForm').reset();
                this.loadServices();
            }, 1500);

        } catch (error) {
            console.error('Error adding service:', error);
            this.showMessage('addServiceMessage', error.message || 'Failed to add service', 'danger');
        } finally {
            btn.disabled = false;
            btn.innerHTML = originalContent;
        }
    }

    editService(serviceId) {
        const service = this.services.find(s => s.id === serviceId);
        if (!service) return;

        document.getElementById('editServiceId').value = serviceId;
        document.getElementById('editServiceName').value = service.name;
        document.getElementById('editServiceDescription').value = service.description || '';
        document.getElementById('editServicePrice').value = service.price;
        document.getElementById('editServiceDuration').value = service.duration;
        document.getElementById('editServiceMessage').innerHTML = '';

        this.editServiceModal.show();
    }

    async updateService() {
        const btn = document.getElementById('updateServiceBtn');
        const originalContent = btn.innerHTML;
        const serviceId = document.getElementById('editServiceId').value;

        const formData = {
            name: document.getElementById('editServiceName').value.trim(),
            description: document.getElementById('editServiceDescription').value.trim(),
            price: parseFloat(document.getElementById('editServicePrice').value),
            duration: parseInt(document.getElementById('editServiceDuration').value)
        };

        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Updating...';

        try {
            const token = this.getAuthToken();
            console.log(`Updating service - PUT /api/shops/${this.shopId}/services/${serviceId}`, formData);
            
            const response = await fetch(`${this.SHOP_SERVICE_URL}/api/shops/${this.shopId}/services/${serviceId}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to update service: ${response.status}`);
            }

            const updatedService = await response.json();
            console.log('Service updated:', updatedService);

            this.showMessage('editServiceMessage', 'Service updated successfully!', 'success');
            
            setTimeout(() => {
                this.editServiceModal.hide();
                this.loadServices();
            }, 1500);

        } catch (error) {
            console.error('Error updating service:', error);
            this.showMessage('editServiceMessage', error.message || 'Failed to update service', 'danger');
        } finally {
            btn.disabled = false;
            btn.innerHTML = originalContent;
        }
    }

    async deleteService(serviceId) {
        if (!confirm('Are you sure you want to delete this service?')) {
            return;
        }

        try {
            const token = this.getAuthToken();
            console.log(`Deleting service - DELETE /api/shops/${this.shopId}/services/${serviceId}`);
            
            const response = await fetch(`${this.SHOP_SERVICE_URL}/api/shops/${this.shopId}/services/${serviceId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to delete service: ${response.status}`);
            }

            console.log('Service deleted successfully');
            await this.loadServices();

        } catch (error) {
            console.error('Error deleting service:', error);
            alert(error.message || 'Failed to delete service');
        }
    }

    // ========== EMPLOYEES MANAGEMENT ==========

    async loadEmployees() {
        const loadingEl = document.getElementById('employeesLoading');
        const noEmployeesEl = document.getElementById('noEmployees');
        const employeesListEl = document.getElementById('employeesList');

        loadingEl.classList.remove('d-none');
        noEmployeesEl.classList.add('d-none');
        employeesListEl.innerHTML = '';

        try {
            const token = this.getAuthToken();
            console.log(`Fetching employees - GET /api/shops/${this.shopId}/employees`);
            
            const response = await fetch(`${this.SHOP_SERVICE_URL}/api/shops/${this.shopId}/employees`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`Failed to fetch employees: ${response.status}`);
            }

            this.employees = await response.json();
            console.log('Employees loaded:', this.employees);

            loadingEl.classList.add('d-none');

            if (this.employees.length === 0) {
                noEmployeesEl.classList.remove('d-none');
            } else {
                this.renderEmployees();
            }

            // Update stats
            document.getElementById('overviewEmployeesCount').textContent = this.employees.length;

        } catch (error) {
            console.error('Error loading employees:', error);
            loadingEl.classList.add('d-none');
            // Show empty state on error
            noEmployeesEl.classList.remove('d-none');
        }
    }

    renderEmployees() {
        const container = document.getElementById('employeesList');
        container.innerHTML = '';

        this.employees.forEach(employee => {
            const employeeCard = this.createEmployeeCard(employee);
            container.appendChild(employeeCard);
        });
    }

    createEmployeeCard(employee) {
        const col = document.createElement('div');
        col.className = 'col-md-6';

        col.innerHTML = `
            <div class="employee-card border rounded-3 p-3">
                <div class="d-flex align-items-start mb-2">
                    <div class="employee-avatar bg-primary text-white rounded-circle me-3 d-flex align-items-center justify-content-center" style="width: 50px; height: 50px; min-width: 50px;">
                        <i class="fas fa-user"></i>
                    </div>
                    <div class="flex-grow-1">
                        <h6 class="fw-bold mb-1">${this.escapeHtml(employee.name)}</h6>
                        <p class="text-muted small mb-1">${this.escapeHtml(employee.role)}</p>
                        <p class="text-muted small mb-0">
                            <i class="fas fa-envelope me-1"></i>${this.escapeHtml(employee.email || 'N/A')}
                        </p>
                        <p class="text-muted small mb-0">
                            <i class="fas fa-phone me-1"></i>${this.escapeHtml(employee.phone || 'N/A')}
                        </p>
                    </div>
                    <div class="btn-group-vertical">
                        <button class="btn btn-outline-primary btn-sm mb-1" onclick="shopDetailManager.editEmployee(${employee.id})" title="Edit">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-outline-danger btn-sm" onclick="shopDetailManager.deleteEmployee(${employee.id})" title="Delete">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            </div>
        `;

        return col;
    }

    showAddEmployeeModal() {
        document.getElementById('addEmployeeForm').reset();
        document.getElementById('addEmployeeMessage').innerHTML = '';
        this.addEmployeeModal.show();
    }

    async addEmployee() {
        const btn = document.getElementById('saveEmployeeBtn');
        const originalContent = btn.innerHTML;

        const formData = {
            name: document.getElementById('employeeName').value.trim(),
            role: document.getElementById('employeeRole').value.trim(),
            email: document.getElementById('employeeEmail').value.trim(),
            phone: document.getElementById('employeePhone').value.trim()
        };

        if (!formData.name || !formData.role) {
            this.showMessage('addEmployeeMessage', 'Please fill in all required fields', 'danger');
            return;
        }

        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Adding...';

        try {
            const token = this.getAuthToken();
            console.log(`Creating employee - POST /api/shops/${this.shopId}/employees`, formData);
            
            const response = await fetch(`${this.SHOP_SERVICE_URL}/api/shops/${this.shopId}/employees`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to add employee: ${response.status}`);
            }

            const createdEmployee = await response.json();
            console.log('Employee created:', createdEmployee);

            this.showMessage('addEmployeeMessage', 'Employee added successfully!', 'success');
            
            setTimeout(() => {
                this.addEmployeeModal.hide();
                document.getElementById('addEmployeeForm').reset();
                this.loadEmployees();
            }, 1500);

        } catch (error) {
            console.error('Error adding employee:', error);
            this.showMessage('addEmployeeMessage', error.message || 'Failed to add employee', 'danger');
        } finally {
            btn.disabled = false;
            btn.innerHTML = originalContent;
        }
    }

    editEmployee(employeeId) {
        const employee = this.employees.find(e => e.id === employeeId);
        if (!employee) return;

        document.getElementById('editEmployeeId').value = employeeId;
        document.getElementById('editEmployeeName').value = employee.name;
        document.getElementById('editEmployeeRole').value = employee.role;
        document.getElementById('editEmployeeEmail').value = employee.email || '';
        document.getElementById('editEmployeePhone').value = employee.phone || '';
        document.getElementById('editEmployeeMessage').innerHTML = '';

        this.editEmployeeModal.show();
    }

    async updateEmployee() {
        const btn = document.getElementById('updateEmployeeBtn');
        const originalContent = btn.innerHTML;
        const employeeId = document.getElementById('editEmployeeId').value;

        const formData = {
            name: document.getElementById('editEmployeeName').value.trim(),
            role: document.getElementById('editEmployeeRole').value.trim(),
            email: document.getElementById('editEmployeeEmail').value.trim(),
            phone: document.getElementById('editEmployeePhone').value.trim()
        };

        if (!formData.name || !formData.role) {
            this.showMessage('editEmployeeMessage', 'Please fill in all required fields', 'danger');
            return;
        }

        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Updating...';

        try {
            const token = this.getAuthToken();
            console.log(`Updating employee - PUT /api/shops/${this.shopId}/employees/${employeeId}`, formData);
            
            const response = await fetch(`${this.SHOP_SERVICE_URL}/api/shops/${this.shopId}/employees/${employeeId}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to update employee: ${response.status}`);
            }

            const updatedEmployee = await response.json();
            console.log('Employee updated:', updatedEmployee);

            this.showMessage('editEmployeeMessage', 'Employee updated successfully!', 'success');
            
            setTimeout(() => {
                this.editEmployeeModal.hide();
                this.loadEmployees();
            }, 1500);

        } catch (error) {
            console.error('Error updating employee:', error);
            this.showMessage('editEmployeeMessage', error.message || 'Failed to update employee', 'danger');
        } finally {
            btn.disabled = false;
            btn.innerHTML = originalContent;
        }
    }

    async deleteEmployee(employeeId) {
        if (!confirm('Are you sure you want to remove this employee?')) {
            return;
        }

        try {
            const token = this.getAuthToken();
            console.log(`Deleting employee - DELETE /api/shops/${this.shopId}/employees/${employeeId}`);
            
            const response = await fetch(`${this.SHOP_SERVICE_URL}/api/shops/${this.shopId}/employees/${employeeId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to delete employee: ${response.status}`);
            }

            console.log('Employee deleted successfully');
            await this.loadEmployees();

        } catch (error) {
            console.error('Error deleting employee:', error);
            alert(error.message || 'Failed to remove employee');
        }
    }

    // ========== UTILITY METHODS ==========

    showMessage(elementId, message, type = 'info') {
        const element = document.getElementById(elementId);
        element.innerHTML = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    setupEventListeners() {
        // Edit shop
        document.getElementById('saveShopBtn').addEventListener('click', () => {
            this.saveShopInfo();
        });

        // Add service
        document.getElementById('saveServiceBtn').addEventListener('click', () => {
            this.addService();
        });

        // Update service
        document.getElementById('updateServiceBtn').addEventListener('click', () => {
            this.updateService();
        });

        // Add employee
        document.getElementById('saveEmployeeBtn').addEventListener('click', () => {
            this.addEmployee();
        });

        // Update employee
        document.getElementById('updateEmployeeBtn').addEventListener('click', () => {
            this.updateEmployee();
        });
    }
}

// Initialize when page loads
let shopDetailManager;
document.addEventListener('DOMContentLoaded', async function() {
    shopDetailManager = new ShopDetailManager();
    await shopDetailManager.init();
});

// Global functions for button onclick
function editShopInfo() {
    if (shopDetailManager) {
        shopDetailManager.editShopInfo();
    }
}

function showAddServiceModal() {
    if (shopDetailManager) {
        shopDetailManager.showAddServiceModal();
    }
}

function showAddEmployeeModal() {
    if (shopDetailManager) {
        shopDetailManager.showAddEmployeeModal();
    }
}
