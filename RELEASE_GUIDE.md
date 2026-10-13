# Billora — Android Studio Export & Release Guide
**App Name:** Billora — Invoice & Receipt Maker  
**Developer:** SimeonJr Studios  
**Package / Application ID:** `com.simeonjr.billora`  
**Architecture:** Kotlin, Jetpack Compose, Material Design 3, Room Database (Offline-First), Android Native PdfDocument & PdfRenderer.

---

## 1. Exporting to Android Studio

1. **Download the Project:**
   - In AI Studio, open the top-right settings/project menu and click **Export as ZIP** or **Push to GitHub**.
2. **Open in Android Studio:**
   - Launch **Android Studio** (Ladybug / Iguana or newer recommended).
   - Select **Open**, choose the extracted directory.
   - Wait for Gradle sync to complete automatically (`gradle sync`).
3. **Run on Device or Emulator:**
   - Connect your Android device via USB (with USB Debugging enabled) or start an Android Virtual Device (AVD).
   - Click the green **Run ('app')** button.

---

## 2. Generating a Signed Release Android App Bundle (.aab)

Google Play requires the **Android App Bundle (.aab)** format for all new app releases:

### Step A: Generate Keystore (if you do not have one yet)
In Android Studio:
1. Go to **Build** > **Generate Signed Bundle / APK...**
2. Choose **Android App Bundle** and click **Next**.
3. Under **Key store path**, click **Create new...**:
   - Choose a secure file location (e.g. `keystore/billora-release-key.jks`).
   - Enter and record passwords for both keystore and key.
   - Alias: `billora_release`
   - Fill in Certificate details (Name: SimeonJr Studios, Org: SimeonJr Studios, Country: NG or your country).
   - Click **OK**.

### Step B: Build the Signed Bundle
1. In the **Generate Signed Bundle / APK** dialog:
   - Select your newly created `.jks` file.
   - Enter your keystore password, key alias, and key password.
   - Check **Export encrypted key for Play App Signing** if prompted.
2. Select **release** build variant.
3. Click **Create / Finish**.
4. Android Studio will output the signed bundle file to:
   `app/release/app-release.aab`

---

## 3. Google Play Release Checklist

Before submitting to Google Play Console:

### 1. App Details & Store Listing
- [ ] **App Title:** `Billora — Invoice & Receipt Maker` (<= 30 characters).
- [ ] **Short Description:** Professional offline invoice and receipt generator with instant PDF export and multi-currency support.
- [ ] **Full Description:** Detail core capabilities: offline-first, professional templates, Nigerian Naira (₦), USD, GBP, EUR, GHS, KES, customer details, custom business logo, instant sharing via WhatsApp/Email.
- [ ] **App Icon:** High-resolution 512 x 512 px PNG (32-bit color).
- [ ] **Feature Graphic:** 1024 x 500 px JPG or PNG.
- [ ] **Screenshots:** At least 2 phone screenshots (up to 8 recommended) showcasing:
  1. Financial Dashboard & Document Feed
  2. Invoice Creation with Line Items & Currency
  3. Receipt Creation with Payment Methods
  4. Real PDF Document Preview & Share Action
  5. Business Profile Setup with Company Logo

### 2. Permissions & Policy Declarations
- [ ] **Zero Dangerous Permissions:** Billora does not require `READ_EXTERNAL_STORAGE` or `CAMERA`. Photo picking uses the standard zero-permission Android Photo Picker (`ActivityResultContracts.PickVisualMedia`).
- [ ] **Data Safety Form:**
  - Data collection: Select **"No"** (Billora does not collect or transmit user data; all financial records, invoices, and receipts reside purely on the local device).
  - Data sharing: Select **"No"**.
- [ ] **Content Rating:** Complete the IARC questionnaire (Utility / Productivity, General audience rating).
- [ ] **Target Audience:** 18+ (Business / Commercial).

### 3. Future Monetization Setup (When Ready)
- [ ] **Google Play Billing:** When ready to activate lifetime premium upgrades, add `com.android.billingclient:billing` and hook into `LocalBillingManager`.
- [ ] **Google AdMob:** When ready for optional ads, add `play-services-ads` and hook into `LocalAdManager`.
