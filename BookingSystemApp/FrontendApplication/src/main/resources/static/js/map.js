/**
 * Interactive Map for Dublin Barbershops
 * Shows all shops with real-time availability indicators
 */

class ShopMap {
    constructor() {
        this.map = null;
        this.markers = [];
        this.shops = [];
        this.userLocationMarker = null;
        this.API_BASE_URL = 'http://localhost:8082'; // shop-service
        this.AVAILABILITY_API_URL = 'http://localhost:8083'; // appointment-service
    }

    /**
     * Initialize the map
     */
    async init() {
        console.log('Initializing shop map...');
        
        // Show loading overlay
        this.showLoading(true);

        try {
            // Initialize Leaflet map centered on Dublin
            this.initializeMap();

            // Load shops with availability data
            await this.loadShopsWithAvailability();

            // Setup My Location button
            this.setupMyLocationButton();

            // Hide loading overlay
            this.showLoading(false);

            console.log('Map initialized successfully');
        } catch (error) {
            console.error('Error initializing map:', error);
            this.showLoading(false);
            alert('Failed to load shop map. Please refresh the page.');
        }
    }

    /**
     * Initialize Leaflet map
     */
    initializeMap() {
        // Dublin city center coordinates
        const dublinCenter = [53.3498, -6.2603];

        // Create map
        this.map = L.map('map').setView(dublinCenter, 12);

        // Add OpenStreetMap tiles
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors',
            maxZoom: 19
        }).addTo(this.map);

        console.log('Leaflet map initialized');
    }

    /**
     * Load all shops with availability data
     */
    async loadShopsWithAvailability() {
        try {
            // Fetch all shops - handle paginated response
            const response = await fetch(`${this.API_BASE_URL}/api/shops?page=0&size=50`);
            
            if (!response.ok) {
                throw new Error(`Failed to fetch shops: ${response.status}`);
            }

            const data = await response.json();
            
            // Extract shops array from paginated response
            this.shops = data.content || data;
            
            if (!Array.isArray(this.shops)) {
                console.error('Invalid shops data:', data);
                throw new Error('Shops data is not an array');
            }
            
            console.log(`Loaded ${this.shops.length} shops`);

            // Get today's date for availability check
            const today = new Date().toISOString().split('T')[0];

            // Load availability for each shop and add markers
            for (const shop of this.shops) {
                try {
                    const availability = await this.getShopAvailability(shop.id, today);
                    this.addShopMarker(shop, availability);
                } catch (error) {
                    console.error(`Failed to load availability for shop ${shop.id}:`, error);
                    // Add marker with unknown availability
                    this.addShopMarker(shop, { available: 0, total: 0, percentage: 0 });
                }
            }

            console.log(`Added ${this.markers.length} shop markers to map`);
        } catch (error) {
            console.error('Error loading shops:', error);
            throw error;
        }
    }

    /**
     * Get availability data for a specific shop
     */
    async getShopAvailability(shopId, date) {
        try {
            const url = `${this.AVAILABILITY_API_URL}/api/availability/shop/${shopId}/date/${date}`;
            console.log(`Fetching availability from: ${url}`);
            
            const response = await fetch(url);

            if (!response.ok) {
                throw new Error(`Failed to fetch availability: ${response.status}`);
            }

            const slots = await response.json();
            
            console.log(`Shop ${shopId} raw slots data (${slots.length} total):`, slots.slice(0, 3));
            
            // Filter out past slots if this is today
            const today = new Date().toISOString().split('T')[0];
            const isToday = date === today;
            
            let relevantSlots = slots;
            if (isToday) {
                const now = new Date();
                const currentTime = now.getHours() * 60 + now.getMinutes(); // Current time in minutes
                
                relevantSlots = slots.filter(slot => {
                    // Handle both {time: "09:00"} and {startTime: "09:00"} formats
                    const timeStr = slot.time || slot.startTime;
                    if (!timeStr) return false;
                    
                    const [hours, minutes] = timeStr.split(':').map(Number);
                    const slotTime = hours * 60 + minutes;
                    return slotTime > currentTime; // Only future slots
                });
                
                console.log(`Shop ${shopId} filtered out ${slots.length - relevantSlots.length} past slots (current time: ${Math.floor(currentTime/60)}:${currentTime%60})`);
            }
            
            // Count available slots - handle different response formats
            // Slots can be: {available: true/false} or {time: "09:00", available: true}
            const available = relevantSlots.filter(slot => {
                // If available property exists, use it; if undefined, assume available
                return slot.available !== false;
            }).length;
            
            const total = relevantSlots.length;
            const percentage = total > 0 ? Math.round((available / total) * 100) : 0;

            console.log(`Shop ${shopId} availability: ${available}/${total} (${percentage}%) - ${isToday ? 'TODAY (future slots only)' : date}`);

            return { available, total, percentage };
        } catch (error) {
            console.error(`Error fetching availability for shop ${shopId}:`, error);
            return { available: 0, total: 0, percentage: 0 };
        }
    }

    /**
     * Add a marker for a shop
     */
    addShopMarker(shop, availability) {
        // Determine marker color based on availability
        const color = this.getAvailabilityColor(availability.percentage);
        const category = this.getAvailabilityCategory(availability.percentage);

        // Create custom icon
        const icon = L.divIcon({
            className: 'custom-marker',
            html: `
                <div style="
                    background-color: ${color};
                    width: 40px;
                    height: 40px;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    color: white;
                    font-size: 18px;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.3);
                    border: 3px solid white;
                ">
                    <i class="fas fa-cut"></i>
                </div>
            `,
            iconSize: [40, 40],
            iconAnchor: [20, 20],
            popupAnchor: [0, -20]
        });

        // Create marker
        const marker = L.marker([shop.latitude, shop.longitude], { icon })
            .addTo(this.map);

        // Create popup content
        const popupContent = this.createPopupContent(shop, availability, category);
        marker.bindPopup(popupContent, {
            maxWidth: 300,
            className: 'shop-popup'
        });

        // Store marker
        this.markers.push(marker);
    }

    /**
     * Get color based on availability percentage
     */
    getAvailabilityColor(percentage) {
        if (percentage >= 50) return '#28a745'; // Green - High
        if (percentage >= 20) return '#ffc107'; // Yellow - Medium
        return '#dc3545'; // Red - Low
    }

    /**
     * Get availability category
     */
    getAvailabilityCategory(percentage) {
        if (percentage >= 50) return { name: 'High Availability', class: 'high' };
        if (percentage >= 20) return { name: 'Medium Availability', class: 'medium' };
        return { name: 'Low/Full', class: 'low' };
    }

    /**
     * Create popup content for a shop
     */
    createPopupContent(shop, availability, category) {
        // Create Google Maps directions URL
        const directionsUrl = `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(shop.address + ', Dublin, Ireland')}`;
        
        return `
            <div class="shop-popup">
                <div class="shop-popup-header">
                    <h6><i class="fas fa-cut"></i> ${shop.name}</h6>
                </div>
                <div class="shop-popup-body">
                    <div class="availability-badge ${category.class}">
                        ${category.name}
                    </div>
                    <div class="shop-popup-info">
                        <i class="fas fa-calendar-check"></i>
                        <strong>${availability.available}</strong> of <strong>${availability.total}</strong> slots available today
                    </div>
                    <div class="shop-popup-info">
                        <i class="fas fa-map-marker-alt"></i>
                        ${shop.address}
                    </div>
                    <div class="shop-popup-info">
                        <i class="fas fa-phone"></i>
                        ${shop.phone}
                    </div>
                    <div class="shop-popup-info">
                        <i class="fas fa-clock"></i>
                        ${shop.openingTime} - ${shop.closingTime}
                    </div>
                    <button class="btn btn-book-now mb-2" onclick="shopMap.bookShop(${shop.id})">
                        <i class="fas fa-calendar-plus"></i> Book Appointment
                    </button>
                    <a href="${directionsUrl}" target="_blank" class="btn btn-directions">
                        <i class="fas fa-directions"></i> Get Directions
                    </a>
                </div>
            </div>
        `;
    }

    /**
     * Navigate to booking page for a shop
     */
    /**
     * Navigate to shop details page to book
     */
    bookShop(shopId) {
        // Check if user is logged in using the correct token key from auth.js
        const tokenKey = window.CONFIG?.TOKEN_KEY || 'barberbook_token';
        const token = localStorage.getItem(tokenKey);
        
        console.log('Checking authentication for booking...', { tokenKey, hasToken: !!token });
        
        if (!token) {
            // Redirect to login with return URL to shop page
            console.log('No token found, redirecting to login');
            window.location.href = `/login.html?redirect=/shop.html?shopId=${shopId}`;
        } else {
            // Go to shop page where user can select a service
            console.log('Token found, redirecting to shop page');
            window.location.href = `/shop.html?shopId=${shopId}`;
        }
    }

    /**
     * Show/hide loading overlay
     */
    showLoading(show) {
        const overlay = document.getElementById('loadingOverlay');
        if (overlay) {
            overlay.style.display = show ? 'flex' : 'none';
        }
    }

    /**
     * Refresh availability data
     */
    async refresh() {
        console.log('Refreshing shop availability...');
        this.showLoading(true);

        try {
            // Clear existing markers
            this.markers.forEach(marker => marker.remove());
            this.markers = [];

            // Reload shops with fresh availability data
            await this.loadShopsWithAvailability();

            this.showLoading(false);
            console.log('Map refreshed successfully');
        } catch (error) {
            console.error('Error refreshing map:', error);
            this.showLoading(false);
            alert('Failed to refresh map. Please try again.');
        }
    }

    /**
     * Setup My Location button
     */
    setupMyLocationButton() {
        const btn = document.getElementById('myLocationBtn');
        if (btn) {
            btn.addEventListener('click', () => this.goToMyLocation());
        }
    }

    /**
     * Get user's current location and center map on it
     */
    goToMyLocation() {
        if (!navigator.geolocation) {
            alert('Geolocation is not supported by your browser');
            return;
        }

        const btn = document.getElementById('myLocationBtn');
        const originalHtml = btn.innerHTML;
        btn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Locating...';
        btn.disabled = true;

        navigator.geolocation.getCurrentPosition(
            (position) => {
                const lat = position.coords.latitude;
                const lng = position.coords.longitude;

                console.log('User location:', { lat, lng });

                // Center map on user location
                this.map.setView([lat, lng], 14);

                // Remove old user location marker if exists
                if (this.userLocationMarker) {
                    this.userLocationMarker.remove();
                }

                // Add marker for user location
                this.userLocationMarker = L.marker([lat, lng], {
                    icon: L.divIcon({
                        html: `
                            <div style="
                                background-color: #007bff;
                                width: 20px;
                                height: 20px;
                                border-radius: 50%;
                                border: 3px solid white;
                                box-shadow: 0 2px 8px rgba(0,0,0,0.3);
                            "></div>
                        `,
                        className: 'user-location-marker',
                        iconSize: [20, 20]
                    })
                }).addTo(this.map);

                this.userLocationMarker.bindPopup('<b>You are here</b>').openPopup();

                // Reset button
                btn.innerHTML = originalHtml;
                btn.disabled = false;
            },
            (error) => {
                console.error('Geolocation error:', error);
                let message = 'Unable to get your location. ';
                
                switch(error.code) {
                    case error.PERMISSION_DENIED:
                        message += 'Please enable location permissions.';
                        break;
                    case error.POSITION_UNAVAILABLE:
                        message += 'Location information unavailable.';
                        break;
                    case error.TIMEOUT:
                        message += 'Location request timed out.';
                        break;
                    default:
                        message += 'An unknown error occurred.';
                }
                
                alert(message);
                
                // Reset button
                btn.innerHTML = originalHtml;
                btn.disabled = false;
            }
        );
    }
}

// Global instance
let shopMap;

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', async () => {
    console.log('Map page loaded');
    
    // Create and initialize map
    shopMap = new ShopMap();
    await shopMap.init();

    // Optional: Auto-refresh every 5 minutes
    setInterval(() => {
        shopMap.refresh();
    }, 5 * 60 * 1000); // 5 minutes
});
