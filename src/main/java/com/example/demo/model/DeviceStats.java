package com.example.demo.model;

public class DeviceStats {
    private long total;
    private long online;
    private long offline;
    private long stolen;
    
    public DeviceStats() {}
    
    public DeviceStats(long total, long online, long offline, long stolen) {
        this.total = total;
        this.online = online;
        this.offline = offline;
        this.stolen = stolen;
    }
    
    // Getters and setters
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    
    public long getOnline() { return online; }
    public void setOnline(long online) { this.online = online; }
    
    public long getOffline() { return offline; }
    public void setOffline(long offline) { this.offline = offline; }
    
    public long getStolen() { return stolen; }
    public void setStolen(long stolen) { this.stolen = stolen; }
}
