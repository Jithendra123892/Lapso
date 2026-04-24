package com.example.demo.views;

import com.example.demo.config.MapsConfig;
import com.example.demo.service.PerfectAuthService;
import com.example.demo.service.DeviceService;
import com.example.demo.service.QuickActionsService;
import com.example.demo.model.Device;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
// import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.combobox.ComboBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route("map")
@PageTitle("LAPSO Device Map")
@AnonymousAllowed
public class CleanMapView extends VerticalLayout {

    private final PerfectAuthService authService;
    private final DeviceService deviceService;
    private final QuickActionsService quickActionsService;
    private final MapsConfig mapsConfig;
    private String selectedDeviceId;
    private String deviceIdToUpdate;  // Added for delete functionality
    
    // Check if user is authenticated
    private boolean isAuthenticated() {
        try {
            return authService != null && authService.isLoggedIn();
        } catch (Exception e) {
            return false;
        }
    }
    
    // Get current user email
    private String getCurrentUserEmail() {
        if (isAuthenticated()) {
            try {
                return authService.getLoggedInUser();
            } catch (Exception e) {
                // Fall through to guest user
            }
        }
        return "guest@example.com"; // Guest user for demo mode
    }

    public CleanMapView(PerfectAuthService authService, DeviceService deviceService, QuickActionsService quickActionsService, MapsConfig mapsConfig) {
        this.authService = authService;
        this.deviceService = deviceService;
        this.quickActionsService = quickActionsService;
        this.mapsConfig = mapsConfig;
        
        // Removed authentication check to allow access without login
        System.out.println("Showing map interface without authentication check");
        createCleanMapInterface();
    }

    private void createCleanMapInterface() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        
        // Clean white background
        getStyle()
            .set("background", "#f5f5f5")
            .set("font-family", "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif")
            .set("min-height", "100vh");

        // Header
        add(createMapHeader());
        
        // Map container
        VerticalLayout mapContainer = new VerticalLayout();
        mapContainer.setSizeFull();
        mapContainer.setPadding(true);
        mapContainer.setSpacing(true);
        mapContainer.getStyle()
            .set("background", "rgba(255, 255, 255, 0.95)")
            .set("border-radius", "20px 20px 0 0")
            .set("margin", "0")
            .set("flex", "1");

        // Simple map placeholder for now
        createSimpleMapView(mapContainer);
        
        add(mapContainer);
    }

    private Component createMapHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setSpacing(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle()
            .set("background", "#ffffff")
            .set("color", "#1f2937")
            .set("padding", "1rem 2rem")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        // Back button
        Button backBtn = new Button("Back", VaadinIcon.ARROW_LEFT.create());
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.getStyle()
            .set("color", "#374151")
            .set("border", "1px solid #d1d5db")
            .set("border-radius", "8px");
        backBtn.addClickListener(e -> UI.getCurrent().navigate("dashboard"));

        // Title
        H1 title = new H1("📍 Live Device Tracking");
        title.getStyle()
            .set("color", "#1f2937")
            .set("font-size", "1.5rem")
            .set("font-weight", "600")
            .set("margin", "0")
            .set("flex", "1");

        // Fix Location button
        Button fixLocationBtn = new Button("Fix Location", VaadinIcon.MAP_MARKER.create());
        fixLocationBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        fixLocationBtn.getStyle()
            .set("border-radius", "8px");
        fixLocationBtn.addClickListener(e -> {
            updateDeviceLocationFromBrowser();
        });
        
        // Refresh button
        Button refreshBtn = new Button("Refresh", VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshBtn.getStyle()
            .set("border-radius", "8px");
        refreshBtn.addClickListener(e -> {
            UI.getCurrent().getPage().reload();
        });
        
        // Delete Device button
        Button deleteBtn = new Button("Delete Device", VaadinIcon.TRASH.create());
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteBtn.getStyle()
            .set("border-radius", "8px");
        deleteBtn.addClickListener(e -> {
            confirmAndDeleteDevice();
        });
        
        // Remote Control Buttons
        Button lockBtn = new Button("Lock", VaadinIcon.LOCK.create());
        lockBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        lockBtn.getStyle()
            .set("background", "#f97316")
            .set("color", "#ffffff")
            .set("border-radius", "8px");
        lockBtn.addClickListener(e -> {
            lockDevice();
        });
        
        Button screenshotBtn = new Button("Screenshot", VaadinIcon.CAMERA.create());
        screenshotBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        screenshotBtn.getStyle()
            .set("background", "#3b82f6")
            .set("color", "#ffffff")
            .set("border-radius", "8px");
        screenshotBtn.addClickListener(e -> {
            takeScreenshot();
        });
        
        Button cameraBtn = new Button("Camera", VaadinIcon.PICTURE.create());
        cameraBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cameraBtn.getStyle()
            .set("background", "#8b5cf6")
            .set("color", "#ffffff")
            .set("border-radius", "8px");
        cameraBtn.addClickListener(e -> {
            takeCameraPhoto();
        });
        
        Button wipeBtn = new Button("Wipe", VaadinIcon.WARNING.create());
        wipeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        wipeBtn.getStyle()
            .set("background", "#dc2626")
            .set("color", "#ffffff")
            .set("border-radius", "8px");
        wipeBtn.addClickListener(e -> {
            confirmAndWipeDevice();
        });

        header.add(backBtn, title, fixLocationBtn, refreshBtn, lockBtn, screenshotBtn, cameraBtn, wipeBtn, deleteBtn);
        return header;
    }

    private void createSimpleMapView(VerticalLayout container) {
        // Removed authentication check to allow access without login
        System.out.println("Creating map view without authentication check");
        
        // Get user devices - for anonymous users, show all devices with location data
        List<Device> devices = deviceService.getCurrentUserDevices();
        
        System.out.println("DEBUG: Found " + devices.size() + " devices for map display");
        
        if (devices.isEmpty()) {
            createEmptyMapState(container);
            return;
        }

        // Device selector controls
        selectedDeviceId = devices.get(0).getDeviceId();
        ComboBox<Device> deviceSelect = new ComboBox<>("Select device");
        deviceSelect.setItems(devices);
        deviceSelect.setItemLabelGenerator(d -> d.getDeviceName() != null ? d.getDeviceName() : d.getDeviceId());
        deviceSelect.setValue(devices.get(0));
        deviceSelect.getStyle().set("max-width", "360px");
        deviceSelect.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                selectedDeviceId = e.getValue().getDeviceId();
                Notification.show("Using device: " + (e.getValue().getDeviceName() != null ? e.getValue().getDeviceName() : e.getValue().getDeviceId()), 2000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });

        HorizontalLayout controls = new HorizontalLayout(deviceSelect);
        controls.setWidthFull();
        controls.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        controls.getStyle().set("margin-bottom", "0.5rem");

        // Map title
        H2 mapTitle = new H2("🗺️ Your Devices on the Map");
        mapTitle.getStyle()
            .set("color", "#1f2937")
            .set("text-align", "center")
            .set("margin", "0 0 2rem 0");

        // Real interactive map with Mappls (MapmyIndia)
        Div realMap = new Div();
        realMap.setId("deviceMap");
        realMap.getStyle()
            .set("background", "#f8f9fa")
            .set("border", "1px solid #dee2e6")
            .set("border-radius", "15px")
            .set("height", "500px")
            .set("width", "100%")
            .set("margin-bottom", "2rem")
            .set("position", "relative"); // Add position relative for absolute positioning of controls

        // Use Mappls Maps SDK (MapmyIndia) - requires script loading
        if (mapsConfig.isMapplsEnabled()) {
            UI.getCurrent().getPage().executeJs(
                "const mapplsScript = document.createElement('script');" +
                "mapplsScript.src = 'https://apis.mapmyindia.com/advancedmaps/v1/' + $0 + '/map_load?v=1.5';" +
                "mapplsScript.async = true;" +
                "mapplsScript.onload = function() { console.log('Mappls SDK loaded successfully'); };" +
                "mapplsScript.onerror = function() { console.error('Failed to load Mappls SDK'); };" +
                "document.head.appendChild(mapplsScript);",
                mapsConfig.getMapplsApiKey()
            );
        } else {
            Notification notification = Notification.show(
                "Map view requires MAPPLS_API_KEY or mappls.api.key configuration.",
                5000,
                Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
        
        // Add CSS for map controls
        UI.getCurrent().getPage().executeJs(
            "const style = document.createElement('style');" +
            "style.textContent = `" +
            "  #mapTypeSelector select {" +
            "    padding: 6px 10px;" +
            "    border: 1px solid #ddd;" +
            "    border-radius: 6px;" +
            "    background: white;" +
            "    font-size: 13px;" +
            "    cursor: pointer;" +
            "    outline: none;" +
            "    box-shadow: 0 1px 3px rgba(0,0,0,0.1);" +
            "  }" +
            "  #mapTypeSelector select:hover {" +
            "    border-color: #3b82f6;" +
            "  }" +
            "  #mapTypeSelector select:focus {" +
            "    border-color: #3b82f6;" +
            "    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);" +
            "  }" +
            "`;" +
            "document.head.appendChild(style);"
        );
        
        createMapplsMap(realMap, devices);
        
        container.add(controls, mapTitle, realMap);
    }
    
    private void createMapplsMap(Div mapContainer, List<Device> devices) {
        if (!mapsConfig.isMapplsEnabled()) {
            mapContainer.setText("Map unavailable until MAPPLS_API_KEY is configured.");
            mapContainer.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("color", "#6b7280")
                .set("font-weight", "600");
            return;
        }

        // Build device data for JavaScript
        StringBuilder devicesJson = new StringBuilder("[");
        boolean first = true;
        
        for (Device device : devices) {
            if (device.getLatitude() != null && device.getLongitude() != null) {
                if (!first) devicesJson.append(",");
                first = false;
                
                String safeName = device.getDeviceName() != null ? 
                    device.getDeviceName().replaceAll("[\"'\\\\]", "") : "Unknown";
                boolean isOnline = device.getIsOnline() != null && device.getIsOnline();
                
                devicesJson.append(String.format(
                    "{\"name\":\"%s\",\"lat\":%f,\"lng\":%f,\"online\":%b,\"locationSource\":\"%s\",\"accuracy\":%f}",
                    safeName,
                    device.getLatitude(),
                    device.getLongitude(),
                    isOnline,
                    device.getLocationSource() != null ? device.getLocationSource() : "UNKNOWN",
                    device.getLocationAccuracy() != null ? device.getLocationAccuracy() : 0.0
                ));
            }
        }
        devicesJson.append("]");
        
        // Add distance info panel to the map container
        Div distancePanel = new Div();
        distancePanel.setId("distancePanel");
        distancePanel.addClassName("distance-panel");
        distancePanel.getStyle()
            .set("position", "absolute")
            .set("top", "70px")
            .set("right", "10px")
            .set("background", "white")
            .set("padding", "15px")
            .set("border-radius", "8px")
            .set("box-shadow", "0 2px 10px rgba(0,0,0,0.2)")
            .set("z-index", "1000")
            .set("min-width", "250px")
            .set("display", "none");
        
        distancePanel.getElement().setProperty("innerHTML", 
            "<div style='font-weight:bold;margin-bottom:10px;color:#667eea;'>📍 Location Info</div>" +
            "<div id='distanceInfo' style='color:#666;font-size:14px;'>Calculating...</div>");
        
        mapContainer.add(distancePanel);
        
        // Add map type selector
        Div mapTypeSelector = new Div();
        mapTypeSelector.setId("mapTypeSelector");
        mapTypeSelector.getStyle()
            .set("position", "absolute")
            .set("top", "20px")
            .set("left", "20px")
            .set("background", "white")
            .set("padding", "10px")
            .set("border-radius", "8px")
            .set("box-shadow", "0 2px 10px rgba(0,0,0,0.2)")
            .set("z-index", "1000")
            .set("font-family", "sans-serif");
        
        mapTypeSelector.getElement().setProperty("innerHTML",
            "<div style='font-weight:bold;margin-bottom:8px;color:#333;'>🗺️ Map Type</div>" +
            "<select id='mapTypeSelect' style='padding:5px;border:1px solid #ccc;border-radius:4px;font-size:12px;'>" +
            "<option value='standard-day'>Standard (Day)</option>" +
            "<option value='standard-night'>Standard (Night)</option>" +
            "<option value='satellite'>Satellite</option>" +
            "<option value='hybrid'>Hybrid</option>" +
            "<option value='terrain'>Terrain</option>" +
            "<option value='traffic'>Traffic</option>" +
            "</select>");
        
        mapContainer.add(mapTypeSelector);
        
        // JavaScript to create Mappls map with distance, routing and user location
        String mapScript = """
            (function() {
                var retryCount = 0;
                var maxRetries = 50;
                var userLocation = null;
                var routeLayer = null;
                var currentMap = null;
                var currentMapType = 'standard-day';
                var routingControl = null;
                
                // Haversine formula for distance calculation
                function calculateDistance(lat1, lon1, lat2, lon2) {
                    var R = 6371; // Earth radius in km
                    var dLat = (lat2 - lat1) * Math.PI / 180;
                    var dLon = (lon2 - lon1) * Math.PI / 180;
                    var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                            Math.sin(dLon/2) * Math.sin(dLon/2);
                    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
                    return R * c;
                }
                
                function updateDistancePanel(device, userLat, userLng) {
                    var dist = calculateDistance(userLat, userLng, device.lat, device.lng);
                    var distText = dist < 1 ? (dist * 1000).toFixed(0) + ' m' : dist.toFixed(2) + ' km';
                    var timeEstimate = (dist / 50 * 60).toFixed(0); // Assuming 50 km/h average speed
                    
                    document.getElementById('distanceInfo').innerHTML = 
                        '<div><b>' + device.name + '</b></div>' +
                        '<div style="margin:8px 0;color:#667eea;font-weight:bold;">📍 Distance: ' + distText + '</div>' +
                        '<div style="color:#666;font-size:13px;">🚗 Estimated travel time: ' + timeEstimate + ' min</div>' +
                        '<div style="margin-top:8px;font-size:12px;color:#888;">Last updated: ' + new Date().toLocaleTimeString() + '</div>';
                    document.getElementById('distancePanel').style.display = 'block';
                }
                
                function getMapStyle(mapType) {
                    switch(mapType) {
                        case 'standard-night':
                            return 'standard-night';
                        case 'satellite':
                            return 'satellite';
                        case 'hybrid':
                            return 'hybrid';
                        case 'terrain':
                            return 'terrain';
                        case 'traffic':
                            return 'traffic';
                        default:
                            return 'standard-day';
                    }
                }
                
                function changeMapType(mapType) {
                    if (currentMap) {
                        currentMapType = mapType;
                        var style = getMapStyle(mapType);
                        // Use the correct Mappls API method to change map type
                        // Mappls uses setStyle or similar method instead of setMapType
                        try {
                            if (typeof currentMap.setMapType === 'function') {
                                currentMap.setMapType(style);
                            } else if (typeof currentMap.setStyle === 'function') {
                                currentMap.setStyle(style);
                            } else {
                                // Fallback: Try to update the map options
                                console.log('Map type changed to: ' + style);
                                // For Mappls, we might need to recreate the map or use a different approach
                                // This is a safer fallback that doesn't cause errors
                            }
                        } catch (error) {
                            console.warn('Failed to change map type:', error);
                            // Silently fail to avoid breaking the UI
                        }
                    }
                }
                
                // Function to draw route between two points using Mappls routing with street-level accuracy
                function drawRoute(startLat, startLng, endLat, endLng) {
                    // Clear previous route if exists
                    if (routeLayer) {
                        currentMap.removeLayer(routeLayer);
                        routeLayer = null;
                    }
                    
                    // Use Mappls advanced routing API for street-level accuracy
                    if (typeof MapmyIndia !== 'undefined' && MapmyIndia.direction) {
                        MapmyIndia.direction({
                            map: currentMap,
                            start: { lat: startLat, lng: startLng },
                            end: { lat: endLat, lng: endLng },
                            callback: function(data) {
                                console.log('Routing data received:', data);
                                // Route is automatically drawn by Mappls with street-level accuracy
                            },
                            resource: 'route_adv', // Advanced routing for better accuracy
                            profile: 'driving',
                            steps: true, // Include turn-by-turn directions
                            overview: 'full',
                            alternatives: true,
                            geometries: 'polyline6',
                            annotations: true
                        });
                    } else {
                        // Fallback to simple line if routing not available
                        var routePoints = [
                            [startLat, startLng],
                            [endLat, endLng]
                        ];
                        
                        routeLayer = L.polyline(routePoints, {
                            color: '#667eea',
                            weight: 4,
                            opacity: 0.8,
                            dashArray: '10, 15'
                        }).addTo(currentMap);
                        
                        // Add arrow markers to show direction
                        var arrowIcon = L.divIcon({
                            className: 'route-arrow',
                            html: '<div style="color:#667eea;font-size:20px;">→</div>',
                            iconSize: [20, 20]
                        });
                        
                        var midPoint = [(startLat + endLat) / 2, (startLng + endLng) / 2];
                        L.marker(midPoint, { icon: arrowIcon }).addTo(currentMap);
                    }
                }
                
                function initMap() {
                    retryCount++;
                    
                    if (typeof MapmyIndia === 'undefined' || typeof L === 'undefined') {
                        if (retryCount < maxRetries) {
                            console.log('Mappls/Leaflet not loaded yet, retrying... (' + retryCount + '/' + maxRetries + ')');
                            setTimeout(initMap, 200);
                        } else {
                            console.error('Failed to load Mappls library after ' + maxRetries + ' attempts');
                            document.getElementById('deviceMap').innerHTML = 
                                '<div style="padding:20px;text-align:center;color:#666;">' +
                                '<h3>Map failed to load</h3>' +
                                '<p>Could not load Mappls Maps SDK. Please refresh the page.</p>' +
                                '<button onclick="location.reload()" style="margin-top:10px;padding:8px 16px;background:#667eea;color:white;border:none;border-radius:4px;cursor:pointer;">Refresh</button>' +
                                '</div>';
                        }
                        return;
                    }
                    
                    if (window.mapDevicesData.length === 0) {
                        document.getElementById('deviceMap').innerHTML = 
                            '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#666;">' +
                            '<h3>No devices with location data</h3></div>';
                        return;
                    }
                    
                    var devices = window.mapDevicesData;
                    
                    try {
                        var avgLat = devices.reduce(function(sum, d) { return sum + d.lat; }, 0) / devices.length;
                        var avgLng = devices.reduce(function(sum, d) { return sum + d.lng; }, 0) / devices.length;
                        
                        var mapOptions = {
                            center: [avgLat, avgLng],
                            zoom: 13,
                            zoomControl: true,
                            location: true
                        };
                        
                        // Set initial map style
                        mapOptions.mapType = getMapStyle(currentMapType);
                        
                        var map = new MapmyIndia.Map('deviceMap', mapOptions);
                        currentMap = map;
                        
                        // Add map type change listener with proper event handling
                        document.getElementById('mapTypeSelect').addEventListener('change', function(e) {
                            changeMapType(e.target.value);
                        });
                        
                        var userMarker = null;
                        
                        // Get user's current location with high accuracy
                        if (navigator.geolocation) {
                            navigator.geolocation.getCurrentPosition(
                                function(position) {
                                    userLocation = {
                                        lat: position.coords.latitude,
                                        lng: position.coords.longitude
                                    };
                                    
                                    // Add user location marker with accuracy circle
                                    var userIcon = L.divIcon({
                                        className: 'user-location-marker',
                                        html: '<div style="background:#2196F3;color:white;padding:8px 12px;' +
                                              'border-radius:20px;font-weight:bold;box-shadow:0 2px 10px rgba(33,150,243,0.5);' +
                                              'white-space:nowrap;font-size:13px;">📍 You are here</div>',
                                        iconSize: [null, null],
                                        iconAnchor: [0, 0]
                                    });
                                    
                                    userMarker = L.marker([userLocation.lat, userLocation.lng], {
                                        icon: userIcon,
                                        title: 'Your Location'
                                    }).addTo(map);
                                    
                                    // Add accuracy circle around user location
                                    if (position.coords.accuracy) {
                                        L.circle([userLocation.lat, userLocation.lng], {
                                            color: '#2196F3',
                                            fillColor: '#2196F3',
                                            fillOpacity: 0.1,
                                            radius: position.coords.accuracy
                                        }).addTo(map);
                                    }
                                    
                                    userMarker.bindPopup(
                                        '<div style="padding:8px;">' +
                                        '<b style="font-size:14px;color:#2196F3;">Your Current Location</b><br>' +
                                        '<small style="color:#666;">Lat: ' + userLocation.lat.toFixed(6) + 
                                        ', Lng: ' + userLocation.lng.toFixed(6) + '</small><br>' +
                                        '<small style="color:#888;">Accuracy: ' + (position.coords.accuracy ? position.coords.accuracy.toFixed(0) + 'm' : 'Unknown') + '</small>' +
                                        '</div>'
                                    );
                                    
                                    // Calculate and show distance to nearest device
                                    if (devices.length > 0) {
                                        var nearestDevice = devices[0];
                                        updateDistancePanel(nearestDevice, userLocation.lat, userLocation.lng);
                                        
                                        // Draw route between user and device with street-level accuracy
                                        drawRoute(userLocation.lat, userLocation.lng, nearestDevice.lat, nearestDevice.lng);
                                        
                                        // Fit bounds to show both locations
                                        var bounds = L.latLngBounds([
                                            [userLocation.lat, userLocation.lng],
                                            [nearestDevice.lat, nearestDevice.lng]
                                        ]);
                                        map.fitBounds(bounds, { padding: [80, 80] });
                                    }
                                },
                                function(error) {
                                    console.warn('Geolocation error:', error.message);
                                    // Still show devices even if user location is not available
                                    showDevicesOnMap(map, devices);
                                },
                                {
                                    enableHighAccuracy: true, // Enable high accuracy for better location precision
                                    timeout: 10000,
                                    maximumAge: 0 // Don't use cached location
                                }
                            );
                        } else {
                            console.warn('Geolocation not supported');
                            // Show devices without user location
                            showDevicesOnMap(map, devices);
                        }
                        
                        // Show devices on map function with enhanced accuracy visualization
                        function showDevicesOnMap(map, devices) {
                            // Add device markers with accuracy indicators
                            devices.forEach(function(device) {
                                var statusIcon = device.online ? '🟢' : '🔴';
                                var markerColor = device.online ? '#4caf50' : '#f44336';
                                
                                // Determine accuracy color based on device accuracy
                                var accuracyColor = '#4caf50'; // Green - Excellent
                                if (device.accuracy > 50) {
                                    accuracyColor = '#ff9800'; // Orange - Good
                                }
                                if (device.accuracy > 100) {
                                    accuracyColor = '#f44336'; // Red - Poor
                                }
                                
                                var customIcon = L.divIcon({
                                    className: 'custom-device-marker',
                                    html: '<div style="background:' + markerColor + 
                                          ';color:white;padding:6px 10px;border-radius:15px;font-weight:bold;' +
                                          'box-shadow:0 2px 8px rgba(0,0,0,0.3);white-space:nowrap;font-size:12px;">' +
                                          statusIcon + ' ' + device.name + '</div>',
                                    iconSize: [null, null],
                                    iconAnchor: [0, 0]
                                });
                                
                                var marker = L.marker([device.lat, device.lng], {
                                    icon: customIcon,
                                    title: device.name
                                }).addTo(map);
                                
                                // Add accuracy circle around device location
                                if (device.accuracy && device.accuracy > 0) {
                                    L.circle([device.lat, device.lng], {
                                        color: accuracyColor,
                                        fillColor: accuracyColor,
                                        fillOpacity: 0.1,
                                        radius: device.accuracy
                                    }).addTo(map);
                                }
                                
                                var popupContent = '<div style="padding:8px;">' +
                                    '<b style="font-size:14px;">' + device.name + '</b><br>' + 
                                    '<span style="color:' + markerColor + ';font-weight:bold;">Status: ' + 
                                    (device.online ? 'Online ✓' : 'Offline ✗') + '</span><br>' +
                                    '<small style="color:#666;">Lat: ' + device.lat.toFixed(6) + 
                                    ', Lng: ' + device.lng.toFixed(6) + '</small><br>' +
                                    '<span style="background:' + 
                                    (device.locationSource === 'AGENT' ? '#4caf50' : 
                                     device.locationSource === 'BROWSER' ? '#ff9800' : '#2196f3') + 
                                    ';color:white;padding:2px 8px;border-radius:10px;font-size:11px;font-weight:bold;margin-right:4px;">' +
                                    (device.locationSource === 'AGENT' ? '🤖 Agent GPS' : 
                                     device.locationSource === 'BROWSER' ? '🌐 Browser GPS' : '📍 IP Location') + 
                                    '</span>' +
                                    '<span style="background:' + 
                                    (device.accuracy && device.accuracy <= 10 ? '#4caf50' : 
                                     device.accuracy && device.accuracy <= 50 ? '#ff9800' : '#f44336') + 
                                    ';color:white;padding:2px 8px;border-radius:10px;font-size:11px;font-weight:bold;">' +
                                    (device.accuracy && device.accuracy <= 10 ? '🟢 Excellent (' + device.accuracy.toFixed(0) + 'm)' : 
                                     device.accuracy && device.accuracy <= 50 ? '🟡 Good (' + device.accuracy.toFixed(0) + 'm)' : 
                                     device.accuracy ? '🔴 Poor (' + device.accuracy.toFixed(0) + 'm)' : '⚪ Unknown') + 
                                    '</span>';
                                
                                if (userLocation) {
                                    var dist = calculateDistance(userLocation.lat, userLocation.lng, device.lat, device.lng);
                                    var distText = dist < 1 ? (dist * 1000).toFixed(0) + ' m' : dist.toFixed(2) + ' km';
                                    popupContent += '<br><b style="color:#667eea;">Distance: ' + distText + '</b>';
                                }
                                
                                popupContent += '</div>';
                                marker.bindPopup(popupContent);
                                
                                // Update distance panel and draw route on marker click
                                marker.on('click', function() {
                                    if (userLocation) {
                                        updateDistancePanel(device, userLocation.lat, userLocation.lng);
                                        drawRoute(userLocation.lat, userLocation.lng, device.lat, device.lng);
                                    }
                                });
                            });
                            
                            // Center map on devices if no user location
                            if (!userLocation && devices.length > 0) {
                                var bounds = L.latLngBounds(devices.map(function(d) {
                                    return [d.lat, d.lng];
                                }));
                                map.fitBounds(bounds, { padding: [50, 50] });
                            }
                        }
                        
                        // Show devices initially
                        showDevicesOnMap(map, devices);
                        
                        console.log('Mappls map loaded successfully with ' + devices.length + ' device(s)');
                        
                    } catch (error) {
                        console.error('Error creating Mappls map:', error);
                        document.getElementById('deviceMap').innerHTML = 
                            '<div style="padding:20px;text-align:center;color:#666;">' +
                            '<h3>Map initialization failed</h3>' +
                            '<p>' + error.message + '</p>' +
                            '<button onclick="location.reload()" style="margin-top:10px;padding:8px 16px;background:#667eea;color:white;border:none;border-radius:4px;cursor:pointer;">Refresh</button>' +
                            '</div>';
                    }
                }
                
                setTimeout(initMap, 1000);
            })();
        """;
        
        // Pass device data to JavaScript
        UI.getCurrent().getPage().executeJs("window.mapDevicesData = " + devicesJson.toString() + ";");
        UI.getCurrent().getPage().executeJs(mapScript);
    }
    
    private void updateDeviceLocationFromBrowser() {
        // Get user's devices
        List<Device> devices = deviceService.getCurrentUserDevices();
        
        if (devices.isEmpty()) {
            Notification.show("No devices found", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        
        // Choose device based on selection, fallback to first
        deviceIdToUpdate = (selectedDeviceId != null && !selectedDeviceId.isBlank())
            ? selectedDeviceId
            : devices.get(0).getDeviceId();
        
        // JavaScript to get browser location and update device
        String updateScript = String.format("""
            (function() {
                if (!navigator.geolocation) {
                    alert('Geolocation is not supported by your browser');
                    return;
                }
                
                const notification = document.createElement('div');
                notification.style.cssText = 'position:fixed;top:20px;left:50%%;transform:translateX(-50%%);' +
                    'background:#2196F3;color:white;padding:15px 30px;border-radius:8px;' +
                    'box-shadow:0 4px 12px rgba(0,0,0,0.3);z-index:10000;font-weight:bold;';
                notification.textContent = '📍 Getting your location...';
                document.body.appendChild(notification);
                
                navigator.geolocation.getCurrentPosition(
                    function(position) {
                        const lat = position.coords.latitude;
                        const lng = position.coords.longitude;
                        const accuracy = position.coords.accuracy;
                        
                        notification.textContent = 'Location acquired! Updating device...';
                        notification.style.background = '#4caf50';
                        
                        // Send to server
                        fetch('/api/device-location/update?' + new URLSearchParams({
                            deviceId: '%s',
                            latitude: lat.toString(),
                            longitude: lng.toString()
                        }), {
                            method: 'POST',
                            headers: {'Content-Type': 'application/json'}

                        })
                        .then(response => response.json())
                        .then(data => {
                            if (data.success) {
                                notification.textContent = 'Device location updated! Reloading map...';
                                setTimeout(() => {
                                    location.reload();
                                }, 2000);
                            } else {
                                notification.textContent = 'Error: ' + data.message;
                                notification.style.background = '#f44336';
                                setTimeout(() => notification.remove(), 4000);
                            }
                        })
                        .catch(error => {
                            notification.textContent = 'Error updating location';
                            notification.style.background = '#f44336';
                            setTimeout(() => notification.remove(), 4000);
                        });
                    },
                    function(error) {
                        let errorMsg = '';
                        switch(error.code) {
                            case error.PERMISSION_DENIED:
                                errorMsg = 'Please allow location access';
                                break;
                            case error.POSITION_UNAVAILABLE:
                                errorMsg = 'Location information unavailable';
                                break;
                            case error.TIMEOUT:
                                errorMsg = 'Location request timed out';
                                break;
                            default:
                                errorMsg = 'Unknown error occurred';
                        }
                        notification.textContent = '' + errorMsg;
                        notification.style.background = '#f44336';
                        setTimeout(() => notification.remove(), 4000);
                    },
                    {
                        enableHighAccuracy: true,
                        timeout: 10000,
                        maximumAge: 0
                    }
                );
            })();
    """, deviceIdToUpdate);
        
        UI.getCurrent().getPage().executeJs(updateScript);
    }
    
    /**
     * Lock the current device remotely
     */
    private void lockDevice() {
        if (selectedDeviceId == null) {
            Notification.show("Please select a device first", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        
        try {
            String userEmail = getCurrentUserEmail();
            boolean isGuest = "guest@example.com".equals(userEmail);
            
            System.out.println("🔒 LOCK BUTTON CLICKED:");
            System.out.println("   Device ID: " + selectedDeviceId);
            System.out.println("   User Email: " + userEmail);
            System.out.println("   Is Guest User: " + isGuest);
            
            // Call QuickActionsService directly on server-side
            quickActionsService.lockDevice(selectedDeviceId, userEmail).thenAccept(result -> {
                UI.getCurrent().access(() -> {
                    if ((Boolean) result.get("success")) {
                        String message = isGuest ? 
                            "🔒 Lock command queued! Device will lock within 30 seconds. (Demo Mode)" : 
                            "🔒 Lock command queued! Device will lock within 30 seconds.";
                        Notification.show(message, 
                            4000, Notification.Position.TOP_END)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        System.out.println("✅ Lock command successful for: " + selectedDeviceId);
                    } else {
                        Notification.show("❌ Lock failed: " + result.get("error"), 
                            4000, Notification.Position.TOP_END)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        System.err.println("❌ Lock failed: " + result.get("error"));
                    }
                });
            }).exceptionally(ex -> {
                UI.getCurrent().access(() -> {
                    Notification.show("❌ Lock error: " + ex.getMessage(), 
                        4000, Notification.Position.TOP_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    System.err.println("❌ Lock exception: " + ex.getMessage());
                });
                return null;
            });
            
        } catch (Exception e) {
            Notification.show("❌ Lock error: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            System.err.println("❌ Lock exception in UI: " + e.getMessage());
        }
    }
    
    private void takeScreenshot() {
        if (selectedDeviceId == null) {
            Notification.show("Please select a device first", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        
        try {
            String userEmail = getCurrentUserEmail();
            boolean isGuest = "guest@example.com".equals(userEmail);
            
            System.out.println("📸 SCREENSHOT BUTTON CLICKED:");
            System.out.println("   Device ID: " + selectedDeviceId);
            System.out.println("   User Email: " + userEmail);
            System.out.println("   Is Guest User: " + isGuest);
            
            // Call QuickActionsService directly on server-side
            quickActionsService.takeScreenshot(selectedDeviceId, userEmail).thenAccept(result -> {
                UI.getCurrent().access(() -> {
                    if ((Boolean) result.get("success")) {
                        String message = isGuest ? 
                            "📸 Screenshot command queued! Check back in 30 seconds. (Demo Mode)" : 
                            "📸 Screenshot command queued! Check back in 30 seconds.";
                        Notification.show(message, 
                            5000, Notification.Position.TOP_END)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        System.out.println("✅ Screenshot command successful for: " + selectedDeviceId);
                    } else {
                        Notification.show("❌ Screenshot failed: " + result.get("error"), 
                            4000, Notification.Position.TOP_END)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        System.err.println("❌ Screenshot failed: " + result.get("error"));
                    }
                });
            }).exceptionally(ex -> {
                UI.getCurrent().access(() -> {
                    Notification.show("❌ Screenshot error: " + ex.getMessage(), 
                        4000, Notification.Position.TOP_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    System.err.println("❌ Screenshot exception: " + ex.getMessage());
                });
                return null;
            });
            
        } catch (Exception e) {
            Notification.show("❌ Screenshot error: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            System.err.println("❌ Screenshot exception in UI: " + e.getMessage());
        }
    }
    
    private void takeCameraPhoto() {
        if (selectedDeviceId == null) {
            Notification.show("Please select a device first", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        
        try {
            String userEmail = getCurrentUserEmail();
            boolean isGuest = "guest@example.com".equals(userEmail);
            
            System.out.println("📷 CAMERA PHOTO BUTTON CLICKED:");
            System.out.println("   Device ID: " + selectedDeviceId);
            System.out.println("   User Email: " + userEmail);
            System.out.println("   Is Guest User: " + isGuest);
            
            // Call QuickActionsService directly on server-side
            quickActionsService.takeCameraPhoto(selectedDeviceId, userEmail).thenAccept(result -> {
                UI.getCurrent().access(() -> {
                    if ((Boolean) result.get("success")) {
                        String message = isGuest ? 
                            "📷 Camera photo command queued! Check back in 30 seconds. (Demo Mode)" : 
                            "📷 Camera photo command queued! Check back in 30 seconds.";
                        Notification.show(message, 
                            5000, Notification.Position.TOP_END)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        System.out.println("✅ Camera photo command successful for: " + selectedDeviceId);
                    } else {
                        Notification.show("❌ Camera photo failed: " + result.get("error"), 
                            4000, Notification.Position.TOP_END)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        System.err.println("❌ Camera photo failed: " + result.get("error"));
                    }
                });
            }).exceptionally(ex -> {
                UI.getCurrent().access(() -> {
                    Notification.show("❌ Camera photo error: " + ex.getMessage(), 
                        4000, Notification.Position.TOP_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    System.err.println("❌ Camera photo exception: " + ex.getMessage());
                });
                return null;
            });
            
        } catch (Exception e) {
            Notification.show("❌ Camera photo error: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            System.err.println("❌ Camera photo exception in UI: " + e.getMessage());
        }
    }
    
    private void confirmAndWipeDevice() {
        if (selectedDeviceId == null) {
            Notification.show("Please select a device first", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        
        // Create confirmation dialog
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setDraggable(true);
        
        // Set high z-index to ensure it appears on top
        dialog.getElement().getStyle()
            .set("z-index", "99999")
            .set("background", "rgba(0, 0, 0, 0.75)");
        
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        dialogLayout.setWidth("400px");
        
        H2 title = new H2("💣 EMERGENCY WIPE CONFIRMATION");
        title.getStyle()
            .set("color", "#dc2626")
            .set("margin-top", "0");
        
        Paragraph warning = new Paragraph(
            "⚠️ This will ERASE ALL DATA from the selected device. " +
            "This action is IRREVERSIBLE and CANNOT be undone. " +
            "Only proceed if the device is LOST or STOLEN."
        );
        warning.getStyle()
            .set("color", "#dc2626")
            .set("font-weight", "bold")
            .set("text-align", "center");
        
        Paragraph confirmText = new Paragraph(
            "Type 'WIPE_CONFIRMED' below to proceed:"
        );
        confirmText.getStyle().set("margin-bottom", "0");
        
        com.vaadin.flow.component.textfield.TextField confirmField = 
            new com.vaadin.flow.component.textfield.TextField();
        confirmField.setWidthFull();
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setWidthFull();
        
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        Button wipeButton = new Button("WIPE DEVICE NOW", e -> {
            if ("WIPE_CONFIRMED".equals(confirmField.getValue())) {
                dialog.close();
                
                try {
                    String userEmail = getCurrentUserEmail();
                    boolean isGuest = "guest@example.com".equals(userEmail);
                    
                    System.out.println("💣 WIPE BUTTON CLICKED:");
                    System.out.println("   Device ID: " + selectedDeviceId);
                    System.out.println("   User Email: " + userEmail);
                    System.out.println("   Is Guest User: " + isGuest);
                    
                    // Call QuickActionsService directly on server-side
                    quickActionsService.wipeDevice(selectedDeviceId, userEmail, "WIPE_CONFIRMED").thenAccept(result -> {
                        UI.getCurrent().access(() -> {
                            if ((Boolean) result.get("success")) {
                                String message = isGuest ? 
                                    "💣 Wipe command queued! Device locked for security. (Demo Mode)" : 
                                    "💣 Wipe command queued! Device locked for security.";
                                Notification.show(message, 
                                    6000, Notification.Position.TOP_END)
                                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                                System.out.println("✅ Wipe command successful for: " + selectedDeviceId);
                            } else {
                                Notification.show("❌ Wipe failed: " + result.get("error"), 
                                    4000, Notification.Position.TOP_END)
                                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                                System.err.println("❌ Wipe failed: " + result.get("error"));
                            }
                        });
                    }).exceptionally(ex -> {
                        UI.getCurrent().access(() -> {
                            Notification.show("❌ Wipe error: " + ex.getMessage(), 
                                4000, Notification.Position.TOP_END)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                            System.err.println("❌ Wipe exception: " + ex.getMessage());
                        });
                        return null;
                    });
                    
                } catch (Exception ex) {
                    Notification.show("❌ Wipe error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    System.err.println("❌ Wipe exception in UI: " + ex.getMessage());
                }
            } else {
                Notification.show("Please type 'WIPE_CONFIRMED' to proceed", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        wipeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        
        buttonLayout.add(cancelButton, wipeButton);
        
        dialogLayout.add(title, warning, confirmText, confirmField, buttonLayout);
        dialog.add(dialogLayout);
        dialog.open();
    }
    
    private void confirmAndDeleteDevice() {
        if (selectedDeviceId == null) {
            Notification.show("Please select a device first", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        
        // Create confirmation dialog
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setDraggable(true);
        
        // Set high z-index to ensure it appears on top
        dialog.getElement().getStyle()
            .set("z-index", "99999")
            .set("background", "rgba(0, 0, 0, 0.75)");
        
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        dialogLayout.setWidth("400px");
        
        H2 title = new H2("🗑️ DELETE DEVICE CONFIRMATION");
        title.getStyle().set("margin-top", "0");
        
        Paragraph warning = new Paragraph(
            "⚠️ This will remove the selected device from your dashboard. " +
            "The agent will continue running on the device. " +
            "You can re-add the device later if needed."
        );
        warning.getStyle()
            .set("color", "#dc2626")
            .set("font-weight", "bold")
            .set("text-align", "center");
        
        Paragraph confirmText = new Paragraph(
            "Type 'DELETE_CONFIRMED' below to proceed:"
        );
        confirmText.getStyle().set("margin-bottom", "0");
        
        com.vaadin.flow.component.textfield.TextField confirmField = 
            new com.vaadin.flow.component.textfield.TextField();
        confirmField.setWidthFull();
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setWidthFull();
        
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        Button deleteButton = new Button("DELETE DEVICE", e -> {
            if ("DELETE_CONFIRMED".equals(confirmField.getValue())) {
                dialog.close();
                
                try {
                    String userEmail = getCurrentUserEmail();
                    boolean isGuest = "guest@example.com".equals(userEmail);
                    
                    System.out.println("🗑️ DELETE BUTTON CLICKED:");
                    System.out.println("   Device ID: " + selectedDeviceId);
                    System.out.println("   User Email: " + userEmail);
                    System.out.println("   Is Guest User: " + isGuest);
                    
                    // For guest users in demo mode, don't actually delete devices
                    if (isGuest) {
                        Notification.show("🗑️ Device deletion simulated in demo mode. No actual deletion occurred.", 
                            5000, Notification.Position.TOP_END)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        System.out.println("🗑️ Device deletion simulated for: " + selectedDeviceId + " (Demo Mode)");
                        return;
                    }
                    
                    // Call device service to delete device
                    com.example.demo.model.Device device = deviceService.getDeviceByDeviceId(selectedDeviceId);
                    boolean deleted = false;
                    if (device != null) {
                        deviceService.deleteDevice(device.getId());
                        deleted = true;
                    }
                    if (deleted) {
                        Notification.show("✅ Device deleted successfully!", 
                            4000, Notification.Position.TOP_END)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        System.out.println("✅ Device deleted: " + selectedDeviceId);
                        
                        // Refresh the page to show updated device list
                        UI.getCurrent().getPage().reload();
                    } else {
                        Notification.show("❌ Failed to delete device", 
                            4000, Notification.Position.TOP_END)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        System.err.println("❌ Failed to delete device: " + selectedDeviceId);
                    }
                } catch (Exception ex) {
                    Notification.show("❌ Delete error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    System.err.println("❌ Delete exception: " + ex.getMessage());
                }
            } else {
                Notification.show("Please type 'DELETE_CONFIRMED' to proceed", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        
        buttonLayout.add(cancelButton, deleteButton);
        
        dialogLayout.add(title, warning, confirmText, confirmField, buttonLayout);
        dialog.add(dialogLayout);
        dialog.open();
    }
    
    private void createMapComponents(Div mapContainer, List<Device> devices) {
        // Legacy method - kept for compatibility
        // Map background
        mapContainer.getStyle()
            .set("background", "linear-gradient(45deg, #e3f2fd 0%, #bbdefb 100%)")
            .set("position", "relative");
        
        // Map header
        Div mapHeader = new Div();
        mapHeader.getStyle()
            .set("position", "absolute")
            .set("top", "10px")
            .set("left", "10px")
            .set("background", "rgba(255,255,255,0.9)")
            .set("padding", "10px")
            .set("border-radius", "8px")
            .set("z-index", "100");
        
        H4 mapTitle = new H4("🗺️ Live Device Map");
        mapTitle.getStyle().set("margin", "0").set("color", "#1976d2");
        
        Paragraph mapDesc = new Paragraph("Real-time device locations");
        mapDesc.getStyle().set("margin", "5px 0 0 0").set("font-size", "12px").set("color", "#666");
        
        mapHeader.add(mapTitle, mapDesc);
        mapContainer.add(mapHeader);
        
        // Device markers using proper components
        int deviceCount = 0;
        for (Device device : devices) {
            if (device.getLatitude() != null && device.getLongitude() != null) {
                createDeviceMarker(mapContainer, device, deviceCount);
                deviceCount++;
            }
        }
        
        // Show message if no devices with location
        if (deviceCount == 0) {
            Div noDevicesMsg = new Div();
            noDevicesMsg.getStyle()
                .set("position", "absolute")
                .set("top", "50%")
                .set("left", "50%")
                .set("transform", "translate(-50%, -50%)")
                .set("text-align", "center")
                .set("color", "#666");
            noDevicesMsg.add(new H3("No devices with location data"));
            mapContainer.add(noDevicesMsg);
        }
        
        // Map legend
        createMapLegend(mapContainer);
    }
    
    private void createDeviceMarker(Div mapContainer, Device device, int index) {
        // Calculate position (better algorithm than before)
        double x = 50 + (index * 120) % 350; // Spread devices across map
        double y = 80 + (index * 90) % 300;
        
        Div marker = new Div();
        boolean isOnline = device.getIsOnline() != null && device.getIsOnline();
        String status = isOnline ? "🟢" : "🔴";
        String color = isOnline ? "#4caf50" : "#f44336";
        
        marker.getStyle()
            .set("position", "absolute")
            .set("left", x + "px")
            .set("top", y + "px")
            .set("background", color)
            .set("color", "white")
            .set("padding", "8px 12px")
            .set("border-radius", "20px")
            .set("font-size", "12px")
            .set("font-weight", "bold")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.2)")
            .set("cursor", "pointer");
        
        // Sanitize device name to prevent XSS
        String safeName = device.getDeviceName() != null ? 
            device.getDeviceName().replaceAll("[<>\"'&]", "") : "Unknown";
        
        marker.setText(status + " " + safeName);
        marker.getElement().setAttribute("title", 
            safeName + " - Lat: " + device.getLatitude() + ", Lng: " + device.getLongitude());
        
        mapContainer.add(marker);
    }
    
    private void createMapLegend(Div mapContainer) {
        Div legend = new Div();
        legend.getStyle()
            .set("position", "absolute")
            .set("bottom", "10px")
            .set("right", "10px")
            .set("background", "rgba(255,255,255,0.9)")
            .set("padding", "10px")
            .set("border-radius", "8px");
        
        Div onlineItem = new Div();
        onlineItem.getStyle().set("font-size", "12px").set("margin-bottom", "5px");
        onlineItem.add(new Span("🟢 Online"));
        
        Div offlineItem = new Div();
        offlineItem.getStyle().set("font-size", "12px");
        offlineItem.add(new Span("🔴 Offline"));
        
        legend.add(onlineItem, offlineItem);
        mapContainer.add(legend);
    }

    private void createLoginPromptState(VerticalLayout container) {
        VerticalLayout loginPrompt = new VerticalLayout();
        loginPrompt.setAlignItems(FlexComponent.Alignment.CENTER);
        loginPrompt.setPadding(true);
        loginPrompt.getStyle()
            .set("text-align", "center")
            .set("padding", "4rem 2rem");

        Span icon = new Span("🔐");
        icon.getStyle().set("font-size", "4rem");

        H2 title = new H2("Login Required");
        title.getStyle()
            .set("color", "#1f2937")
            .set("margin", "1rem 0 0.5rem 0");

        Paragraph description = new Paragraph("Please log in to view your device locations on the map");
        description.getStyle()
            .set("color", "#6b7280")
            .set("margin", "0 0 2rem 0");

        Button loginButton = new Button("Login", VaadinIcon.SIGN_IN.create());
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        loginButton.getStyle()
            .set("background", "#3b82f6")
            .set("border-radius", "25px");
        loginButton.addClickListener(e -> UI.getCurrent().navigate("login"));

        loginPrompt.add(icon, title, description, loginButton);
        container.add(loginPrompt);
    }

    private void createEmptyMapState(VerticalLayout container) {
        VerticalLayout emptyState = new VerticalLayout();
        emptyState.setAlignItems(FlexComponent.Alignment.CENTER);
        emptyState.setPadding(true);
        emptyState.getStyle()
            .set("text-align", "center")
            .set("padding", "4rem 2rem");

        Span icon = new Span("📍");
        icon.getStyle().set("font-size", "4rem");

        H2 title = new H2("No devices to track yet");
        title.getStyle()
            .set("color", "#1f2937")
            .set("margin", "1rem 0 0.5rem 0");

        Paragraph description = new Paragraph("Add your first device to see it on the map");
        description.getStyle()
            .set("color", "#6b7280")
            .set("margin", "0 0 2rem 0");

        Button addDevice = new Button("Add Device", VaadinIcon.PLUS.create());
        addDevice.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        addDevice.getStyle()
            .set("background", "#10b981")
            .set("border-radius", "25px");
        addDevice.addClickListener(e -> UI.getCurrent().navigate("download-agent"));

        emptyState.add(icon, title, description, addDevice);
        container.add(emptyState);
    }

    private Component createMapDeviceCard(Device device) {
        HorizontalLayout card = new HorizontalLayout();
        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(true);
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.getStyle()
            .set("background", "#ffffff")
            .set("border-radius", "15px")
            .set("border", "1px solid #e5e7eb")
            .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.1)")
            .set("margin-bottom", "1rem");

        // Device info
        VerticalLayout deviceInfo = new VerticalLayout();
        deviceInfo.setPadding(false);
        deviceInfo.setSpacing(false);
        deviceInfo.getStyle().set("flex", "1");

        String deviceName = device.getDeviceName() != null ? device.getDeviceName() : "Unknown Device";
        H3 nameH3 = new H3("💻 " + deviceName);
        nameH3.getStyle()
            .set("color", "#1f2937")
            .set("font-size", "1.1rem")
            .set("font-weight", "600")
            .set("margin", "0 0 0.5rem 0");

        // Location info
        String locationText = "📍 ";
        if (device.getLatitude() != null && device.getLongitude() != null) {
            locationText += String.format("Lat: %.4f, Lng: %.4f", device.getLatitude(), device.getLongitude());
            if (device.getAddress() != null) {
                locationText = "📍 " + device.getAddress();
            }
        } else {
            locationText += "Location not available";
        }

        Paragraph locationP = new Paragraph(locationText);
        locationP.getStyle()
            .set("color", "#6b7280")
            .set("font-size", "0.9rem")
            .set("margin", "0 0 0.5rem 0");

        // Last seen
        String lastSeenText = "⏰ ";
        if (device.getLastSeen() != null) {
            lastSeenText += "Last seen: " + device.getLastSeen().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
        } else {
            lastSeenText += "Never seen";
        }

        Paragraph lastSeenP = new Paragraph(lastSeenText);
        lastSeenP.getStyle()
            .set("color", "#6b7280")
            .set("font-size", "0.875rem")
            .set("margin", "0");

        deviceInfo.add(nameH3, locationP, lastSeenP);

        // Status badge
        Span statusBadge = new Span();
        boolean isOnline = device.getIsOnline() != null ? device.getIsOnline() : false;
        statusBadge.setText(isOnline ? "🟢 Online" : "🔴 Offline");
        statusBadge.getStyle()
            .set("background", isOnline ? "#dcfce7" : "#fef2f2")
            .set("color", isOnline ? "#166534" : "#991b1b")
            .set("border-radius", "20px")
            .set("font-size", "0.875rem")
            .set("font-weight", "600");

        card.add(deviceInfo, statusBadge);
        return card;
    }
}
