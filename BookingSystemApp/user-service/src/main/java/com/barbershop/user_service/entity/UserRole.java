package com.barbershop.user_service.entity;

/**
 * User Roles for the booking system
 */
public enum UserRole {

    /**
     * Users who book appointments are customers
     */
    CUSTOMER("Customer"),

    /**
     * Users that provide services are staff
     */
    STAFF("Staff"),

    /**
     * User who own a Shop are shop owners
     */
    SHOP_OWNER("Shop Owner");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Check if role has management permissions
     */
    public boolean hasManagementPermissions(){
        return this == SHOP_OWNER;
    }

    /**
     * Check if role can provide services
     */
    public boolean canProvideServices() {
        return this == STAFF || this == SHOP_OWNER;
    }

    /**
     * Check if role can book appointments
     */
    public boolean canBookAppointments() {
        return this == CUSTOMER;
    }

}
