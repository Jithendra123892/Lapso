# 🗺️ Mappls (MapmyIndia) Integration Guide for LAPSO

## Why Mappls Instead of OpenStreetMap?

**Mappls (formerly MapmyIndia)** is the **best choice for India** because:

✅ **Accurate Indian Locations** - Precise street-level data for India  
✅ **Better Address Recognition** - Understands Indian addresses, localities, and landmarks  
✅ **Fast Loading** - Optimized servers in India  
✅ **Free Tier Available** - 25,000 API calls/month free  
✅ **Rich Features** - Geocoding, routing, traffic data  

OpenStreetMap shows **incorrect or missing** data for many Indian locations.

---

## 📝 Step 1: Get Your FREE Mappls API Key

### 1. Create Account
Go to: **https://apis.mappls.com/console/**

Click **"Sign Up"** and create a free account with:
- Your email
- Company name (can be "Personal" or "LAPSO Project")
- Phone number

### 2. Verify Email
Check your email and verify your account.

### 3. Create API Key
1. Login to **https://apis.mappls.com/console/**
2. Go to **"My Keys"** or **"API Keys"** section
3. Click **"Create New Key"**
4. Select **"Maps SDK"** or **"JavaScript API"**
5. Give it a name like **"LAPSO Device Tracker"**
6. **Copy your API Key** (looks like: `abc123xyz456...`)

### 4. Enable Required APIs
In the console, make sure these APIs are enabled:
- ✅ **Maps JavaScript API**
- ✅ **Markers & InfoWindows**

---

## 🔧 Step 2: Add API Key to Your Project

### Option A: Direct in Code (Quick Test)

Open: `src/main/java/com/example/demo/views/CleanMapView.java`

Find this line (around line 217):
```java
mappls.initialize('YOUR_MAPPLS_API_KEY', function() {
```

Replace `YOUR_MAPPLS_API_KEY` with your actual key:
```java
mappls.initialize('abc123xyz456...', function() {
```

### Option B: Environment Variable (Recommended for Production)

**1. Add to `application.properties`:**
```properties
# Mappls Configuration
mappls.api.key=YOUR_ACTUAL_API_KEY_HERE
```

**2. Update CleanMapView.java:**

Add this field to the class:
```java
@Value("${mappls.api.key}")
private String mapplsApiKey;
```

And update the JavaScript to use it:
```java
String mapScript = String.format("""
    ...
    mappls.initialize('%s', function() {
    ...
""", mapplsApiKey, devicesJson.toString());
```

**3. For security, use environment variable:**

Instead of hardcoding in `application.properties`, set it as environment variable:

**Windows PowerShell:**
```powershell
$env:MAPPLS_API_KEY="your_actual_key_here"
mvn spring-boot:run
```

**Linux/Mac:**
```bash
export MAPPLS_API_KEY="your_actual_key_here"
./mvnw spring-boot:run
```

Then in `application.properties`:
```properties
mappls.api.key=${MAPPLS_API_KEY}
```

---

## 🚀 Step 3: Test the Map

1. **Start your application:**
   ```powershell
   mvn spring-boot:run
   ```

2. **Open your browser:**
   ```
   http://localhost:8080/map
   ```

3. **You should see:**
   - ✅ Interactive Mappls map with Indian street details
   - ✅ Green marker for online device (SUHASINI)
   - ✅ Accurate location on Indian map
   - ✅ Click marker to see device details

4. **Check browser console (F12):**
   - Should see: `✅ Mappls map loaded successfully with 1 devices`
   - If error, check API key is correct

---

## 🔍 Troubleshooting

### Map Not Loading?

**Check 1: API Key Valid**
- Login to https://apis.mappls.com/console/
- Verify key is active and not expired
- Check daily limit not exceeded (25,000 free)

**Check 2: Browser Console Errors**
- Press F12 in browser
- Look for JavaScript errors
- Common issue: Invalid API key

**Check 3: Internet Connection**
- Mappls requires internet to load map tiles
- Check firewall not blocking `apis.mappls.com`

### Shows "Map initialization failed"?

This means:
1. API key is incorrect or missing
2. API key doesn't have Maps JavaScript API enabled
3. Daily API limit exceeded (check console)

**Solution:**
- Go to https://apis.mappls.com/console/
- Check your key status
- Enable "Maps JavaScript API"
- Try creating a new key

### Invalid Location / Map Not Centered?

**Check your device coordinates:**

Run this query to see what coordinates are stored:
```sql
SELECT device_name, latitude, longitude 
FROM devices 
WHERE device_id = '0B7ABA31-AD7B-4AE3-8301-C4C6E6EFCE32';
```

**Example valid coordinates for India:**
- Hyderabad: `17.385044, 78.486671`
- Bangalore: `12.971599, 77.594566`
- Delhi: `28.613939, 77.209021`

If latitude/longitude are `null` or `0.0`, your agent isn't sending GPS data.

---

## 📱 Alternative: Fix Agent Location

If the agent is sending wrong location, check:

**1. Windows Agent Location Permission:**
- Go to: Settings → Privacy → Location
- Enable "Location services"
- Allow "Location" for your app

**2. Agent Code:**
Check `agent.ps1` has:
```powershell
Add-Type -AssemblyName System.Device
$geoWatcher = New-Object System.Device.Location.GeoCoordinateWatcher
$geoWatcher.Start()
```

**3. Test GPS Manually:**
Open PowerShell and run:
```powershell
Add-Type -AssemblyName System.Device
$geo = New-Object System.Device.Location.GeoCoordinateWatcher
$geo.Start()
Start-Sleep -Seconds 3
$geo.Position.Location
```

Should show your actual latitude/longitude.

---

## 💰 Mappls Pricing (as of 2025)

**Free Tier:**
- ✅ 25,000 API calls/month
- ✅ All map features
- ✅ Good for personal/testing

**For production LAPSO:**
- Each map load = 1 API call
- Each device marker = 0 extra calls
- 25,000 calls = ~833 map views/day (plenty!)

**If you need more:**
- Paid plans start at ₹999/month for 100,000 calls
- See: https://apis.mappls.com/console/pricing

---

## 🎯 Next Steps

1. ✅ Get Mappls API key from https://apis.mappls.com/console/
2. ✅ Add key to `CleanMapView.java` or `application.properties`
3. ✅ Restart application: `mvn spring-boot:run`
4. ✅ Open http://localhost:8080/map
5. ✅ Verify device shows on correct Indian location

**Need help?** Check:
- Mappls Documentation: https://developer.mappls.com/docs/
- LAPSO Issues: https://github.com/Jithendra123892/LAPSO/issues

---

## 📚 Advanced Mappls Features (Future)

Once basic map works, you can add:

- 🔍 **Search** - Search for places on map
- 🛣️ **Routes** - Show path between devices
- 📍 **Geocoding** - Convert addresses to coordinates
- 🚦 **Traffic** - Show real-time traffic
- 📊 **Heatmaps** - Density of devices

All available in Mappls JavaScript API!

---

**Status:** ✅ Mappls integration code added to LAPSO  
**Next:** Get API key and test the map!
