package com.dccn.connect.models;

/**
 * User model class representing a user in the DCCN Connect system
 */
public class User {
    
    // User type constants
    public static final int USER_TYPE_STUDENT = 1;
    public static final int USER_TYPE_RESCUE_TEAM = 2;
    
    // User properties
    private String id;
    private String username;
    private String registerNumber;
    private int userType;
    private String deviceId;
    private String deviceName;
    private String deviceAddress;
    private long lastSeen;
    private boolean isOnline;
    private int signalStrength;
    
    // Default constructor
    public User() {
        this.lastSeen = System.currentTimeMillis();
        this.isOnline = false;
        this.signalStrength = 0;
    }
    
    // Constructor with basic parameters
    public User(String username, int userType, String deviceId) {
        this();
        this.username = username;
        this.userType = userType;
        this.deviceId = deviceId;
        this.id = deviceId; // Use device ID as user ID for now
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getRegisterNumber() {
        return registerNumber;
    }
    
    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }
    
    public int getUserType() {
        return userType;
    }
    
    public void setUserType(int userType) {
        this.userType = userType;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getDeviceName() {
        return deviceName;
    }
    
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    
    public String getDeviceAddress() {
        return deviceAddress;
    }
    
    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }
    
    public long getLastSeen() {
        return lastSeen;
    }
    
    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }
    
    public boolean isOnline() {
        return isOnline;
    }
    
    public void setOnline(boolean online) {
        isOnline = online;
    }
    
    public int getSignalStrength() {
        return signalStrength;
    }
    
    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }
    
    /**
     * Get user type as string
     */
    public String getUserTypeString() {
        switch (userType) {
            case USER_TYPE_STUDENT:
                return "Student";
            case USER_TYPE_RESCUE_TEAM:
                return "Rescue Team";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Check if user is a student
     */
    public boolean isStudent() {
        return userType == USER_TYPE_STUDENT;
    }
    
    /**
     * Check if user is rescue team
     */
    public boolean isRescueTeam() {
        return userType == USER_TYPE_RESCUE_TEAM;
    }
    
    /**
     * Update last seen timestamp
     */
    public void updateLastSeen() {
        this.lastSeen = System.currentTimeMillis();
    }
    
    /**
     * Get signal strength description
     */
    public String getSignalStrengthDescription() {
        if (signalStrength >= -50) {
            return "Excellent";
        } else if (signalStrength >= -60) {
            return "Good";
        } else if (signalStrength >= -70) {
            return "Fair";
        } else {
            return "Poor";
        }
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", userType=" + getUserTypeString() +
                ", deviceId='" + deviceId + '\'' +
                ", isOnline=" + isOnline +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        User user = (User) o;
        return deviceId != null ? deviceId.equals(user.deviceId) : user.deviceId == null;
    }
    
    @Override
    public int hashCode() {
        return deviceId != null ? deviceId.hashCode() : 0;
    }
}

