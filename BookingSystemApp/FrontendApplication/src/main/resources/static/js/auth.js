// Complete auth.js file - Replace your entire auth.js with this

// Configuration
const CONFIG = {
    API_BASE_URL: 'http://localhost:8081',
    TOKEN_KEY: 'barberbook_token',
    USER_KEY: 'barberbook_user'
};

// Utility function for API calls
async function apiCall(endpoint, options = {}) {
    const token = localStorage.getItem(CONFIG.TOKEN_KEY);

    const config = {
        headers: {
            'Content-Type': 'application/json',
            ...(token && { 'Authorization': `Bearer ${token}` }),
            ...options.headers
        },
        ...options
    };

    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}${endpoint}`, config);

        const contentType = response.headers.get('content-type');
        let data;

        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
        } else {
            data = { message: await response.text() };
        }

        if (!response.ok) {
            throw new Error(data.message || `HTTP ${response.status}: ${response.statusText}`);
        }

        return data;

    } catch (error) {
        console.error('API Call Error:', error);

        if (error instanceof TypeError) {
            throw new Error('Network error: Please check if the backend server is running.');
        }

        throw error;
    }
}

// Helper function for button loading states
function setButtonLoading(buttonId, loading = true) {
    const button = document.getElementById(buttonId);
    if (!button) {
        console.error('Button not found:', buttonId);
        return;
    }

    if (loading) {
        button.disabled = true;
        button.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Processing...';
    } else {
        button.disabled = false;
        const originalText = button.id === 'loginBtn' ? 'Login' : 'Create Account';
        button.innerHTML = originalText;
    }
}

// Helper function to show messages
function showMessage(elementId, message, type = 'danger') {
    const messageDiv = document.getElementById(elementId);
    if (messageDiv) {
        messageDiv.innerHTML = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
    } else {
        console.error('Message div not found:', elementId);
    }
}

// Handle Login - FIXED AND SIMPLIFIED
async function handleLogin(event) {
    event.preventDefault();
    console.log('Login form submitted');

    // Get form elements with error checking
    const emailElement = document.getElementById('loginEmail');
    const passwordElement = document.getElementById('loginPassword');

    if (!emailElement) {
        console.error('Email field not found with ID: loginEmail');
        alert('Login form error: Email field not found');
        return;
    }

    if (!passwordElement) {
        console.error('Password field not found with ID: loginPassword');
        alert('Login form error: Password field not found');
        return;
    }

    const email = emailElement.value.trim();
    const password = passwordElement.value;

    console.log('Login attempt with email:', email);

    if (!email || !password) {
        showMessage('loginMessage', 'Please fill in all fields.');
        return;
    }

    setButtonLoading('loginBtn', true);

    try {
        const data = await apiCall('/api/users/auth/login', {
            method: 'POST',
            body: JSON.stringify({
                email: email,
                password: password
            })
        });

        console.log('Login successful:', data);

        localStorage.setItem(CONFIG.TOKEN_KEY, data.accessToken);
        localStorage.setItem(CONFIG.USER_KEY, JSON.stringify(data.user));

        showMessage('loginMessage', 'Login successful! Redirecting...', 'success');

        setTimeout(() => {
            window.location.href = 'dashboard.html';
        }, 1500);

    } catch (error) {
        console.error('Login error:', error);
        showMessage('loginMessage', `Login failed: ${error.message}`);
    } finally {
        setButtonLoading('loginBtn', false);
    }
}

// Handle Registration - FIXED WITH PHONE FIELD
async function handleRegistration(event) {
    event.preventDefault();
    console.log('Registration form submitted');

    // Get form elements with error checking
    const firstNameElement = document.getElementById('registerFirstName');
    const lastNameElement = document.getElementById('registerLastName');
    const emailElement = document.getElementById('registerEmail');
    const phoneElement = document.getElementById('registerPhone');
    const passwordElement = document.getElementById('registerPassword');
    const confirmPasswordElement = document.getElementById('registerConfirmPassword');

    // Check if all required fields exist
    if (!firstNameElement) {
        console.error('First name field not found');
        alert('Registration form error: First name field not found');
        return;
    }

    if (!lastNameElement) {
        console.error('Last name field not found');
        alert('Registration form error: Last name field not found');
        return;
    }

    if (!emailElement) {
        console.error('Email field not found');
        alert('Registration form error: Email field not found');
        return;
    }

    if (!phoneElement) {
        console.error('Phone field not found');
        alert('Registration form error: Phone field not found');
        return;
    }

    if (!passwordElement) {
        console.error('Password field not found');
        alert('Registration form error: Password field not found');
        return;
    }

    if (!confirmPasswordElement) {
        console.error('Confirm password field not found');
        alert('Registration form error: Confirm password field not found');
        return;
    }

    const formData = {
        firstName: firstNameElement.value.trim(),
        lastName: lastNameElement.value.trim(),
        email: emailElement.value.trim(),
        phone: phoneElement.value.trim(),
        password: passwordElement.value,
        role: 'CUSTOMER' // Default to CUSTOMER since you removed the role field
    };

    const confirmPassword = confirmPasswordElement.value;

    console.log('Registration attempt with:', { ...formData, password: '[HIDDEN]' });

    // Basic validation
    if (!formData.firstName || !formData.lastName || !formData.email || !formData.phone || !formData.password) {
        showMessage('registerMessage', 'Please fill in all required fields.');
        return;
    }

    if (formData.password !== confirmPassword) {
        showMessage('registerMessage', 'Passwords do not match.');
        return;
    }

    if (formData.password.length < 8) {
        showMessage('registerMessage', 'Password must be at least 8 characters long.');
        return;
    }

    // Phone validation (basic)
    const phonePattern = /^[+]?[0-9]{10,15}$/;
    if (!phonePattern.test(formData.phone)) {
        showMessage('registerMessage', 'Please enter a valid phone number (10-15 digits, optionally starting with +).');
        return;
    }

    setButtonLoading('registerBtn', true);

    try {
        const data = await apiCall('/api/users/register', {
            method: 'POST',
            body: JSON.stringify(formData)
        });

        console.log('Registration successful:', data);

        showMessage('registerMessage',
            'Account created successfully! Redirecting to login page...',
            'success'
        );

        setTimeout(() => {
            window.location.href = 'login.html';
        }, 2000);

    } catch (error) {
        console.error('Registration error:', error);
        showMessage('registerMessage', `Registration failed: ${error.message}`);
    } finally {
        setButtonLoading('registerBtn', false);
    }
}

// Initialize when page loads
document.addEventListener('DOMContentLoaded', function() {
    console.log('Auth script loaded');

    // Attach event listeners if forms exist
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');

    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
        console.log('Login form listener attached');
    }

    if (registerForm) {
        registerForm.addEventListener('submit', handleRegistration);
        console.log('Register form listener attached');
    }

    // Debug: Log which page we're on and what fields are available
    console.log('Current page fields found:');
    console.log('- loginEmail:', !!document.getElementById('loginEmail'));
    console.log('- registerFirstName:', !!document.getElementById('registerFirstName'));
    console.log('- registerPhone:', !!document.getElementById('registerPhone'));
});
