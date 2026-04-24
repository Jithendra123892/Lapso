package com.example.demo.client;

/**
 * Standalone Java Client Application - Superior to Microsoft Find My Device
 * Pure Java implementation for enterprise laptop tracking
 */
public class JavaClientApplication {
    
    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("🖥️  LaptopTracker Pro Enterprise - Pure Java Client");
        System.out.println("🏆 SUPERIOR TO MICROSOFT FIND MY DEVICE");
        System.out.println("================================================================================");
        System.out.println();
        System.out.println("✨ Enterprise Features:");
        System.out.println("   • Advanced hardware fingerprinting");
        System.out.println("   • Real-time theft detection");
        System.out.println("   • Comprehensive system monitoring");
        System.out.println("   • Network and security analysis");
        System.out.println("   • Performance metrics tracking");
        System.out.println("   • Enhanced location services");
        System.out.println();
        
        if (args.length < 1) {
            System.out.println("Usage: java -jar client.jar <owner_email> [server_url] [update_interval_seconds]");
            System.out.println();
            System.out.println("Examples:");
            System.out.println("  java -jar client.jar user@company.com");
            System.out.println("  java -jar client.jar user@company.com http://tracker.company.com:8086 20");
            System.out.println();
            System.out.println("🏆 Why Superior to Microsoft Find My Device:");
            System.out.println("   • 20-second updates (vs Microsoft's 5-15 minutes)");
            System.out.println("   • Advanced theft detection algorithms");
            System.out.println("   • Comprehensive system performance monitoring");
            System.out.println("   • Network security analysis");
            System.out.println("   • Cross-platform support (Windows/Mac/Linux)");
            System.out.println("   • Enterprise-grade security features");
            System.out.println("   • Real-time hardware fingerprinting");
            System.out.println("   • Advanced location tracking");
            System.exit(1);
        }
        
        String ownerEmail = args[0];
        String serverUrl = args.length > 1 ? args[1] : "http://localhost:8086";
        int intervalSeconds = args.length > 2 ? Integer.parseInt(args[2]) : 20;
        
        // Validate email format
        if (!ownerEmail.contains("@") || !ownerEmail.contains(".")) {
            System.err.println("❌ Invalid email format: " + ownerEmail);
            System.exit(1);
        }
        
        System.out.println("📧 Owner Email: " + ownerEmail);
        System.out.println("🌐 Server URL: " + serverUrl);
        System.out.println("⏱️ Update Interval: " + intervalSeconds + " seconds");
        System.out.println();
        
        // Create and start enterprise client
        EnterpriseJavaClient client = new EnterpriseJavaClient();
        client.startEnterpriseTracking(ownerEmail, serverUrl, intervalSeconds);
    }
}
