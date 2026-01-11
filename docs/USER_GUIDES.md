# User Guides - IuranKomplek

## Overview

This guide provides step-by-step instructions for common user workflows in the IuranKomplek Android application.

## 📋 Table of Contents

- [Getting Started](#getting-started)
- [Viewing User Directory](#viewing-user-directory)
- [Managing Monthly Dues](#managing-monthly-dues)
- [Creating Financial Reports](#creating-financial-reports)
- [Processing Payments](#processing-payments)
- [Viewing Transaction History](#viewing-transaction-history)
- [Managing Vendors](#managing-vendors)
- [Community Communication](#community-communication)
- [Viewing Announcements](#viewing-announcements)

---

## Getting Started

### First Launch

1. **Install the Application**
   - Download the APK from your distribution source
   - Enable "Unknown Sources" in device settings if installing from outside Play Store
   - Tap the APK file to install

2. **Initial Setup**
   - Open the IuranKomplek app
   - You will see the main menu with 4 options
   - No account setup required (app works offline after initial sync)

3. **Automatic Data Sync**
   - On first launch, the app automatically fetches data from the server
   - Swipe down on any list to refresh data
   - All data is cached locally for offline use

### Navigation

The app uses a main menu with 4 sections:

| Menu Item | Purpose | Icon |
|-----------|---------|-------|
| **User Management** | View and manage resident profiles | Users |
| **Financial Reports** | View dues collection and expenses | Chart |
| **Communication** | Messages and community posts | Chat |
| **Payments** | Process resident payments | Credit Card |

---

## Viewing User Directory

### Access User List

1. **From Main Menu**
   - Tap "User Management" card
   - Wait for user list to load

2. **User Information Displayed**
   - **Name**: First and last name of resident
   - **Email**: Contact email address
   - **Address**: Residential address
   - **Monthly Dues**: Amount paid per month
   - **Avatar**: Profile picture (circular display)

### Refresh User List

- **Pull to Refresh**: Swipe down from the top of the list
- Loading indicator appears briefly during refresh
- Data updates automatically if newer version available

### User Details

Each user card shows:
- Resident photo (avatar)
- Full name
- Email address
- Current monthly dues status

---

## Managing Monthly Dues

### Viewing Individual Dues

1. **Open User Management**
   - Tap "User Management" from main menu

2. **Review Dues Information**
   - Each user card displays their monthly dues amount
   - Total iuran rekap (annual summary) shown
   - Individual payment totals visible

### Updating Dues Information

Dues information is automatically calculated from payment records:
- Monthly dues = Payment amount per month
- Annual total = Monthly dues × 12
- Individual total = Sum of all payments by resident

**Note**: Dues are updated automatically when payments are processed. Manual editing is not required.

---

## Creating Financial Reports

### Access Financial Reports

1. **Open Reports**
   - Tap "Financial Reports" from main menu
   - Wait for financial data to load

### Understanding the Report

The financial report displays:

| Section | Description |
|---------|-------------|
| **Total Monthly Dues** | Sum of all residents' monthly payments |
| **Total Expenses** | Sum of all fund expenditures |
| **Individual Totals** | Payment totals per resident |
| **Balance** | Remaining funds after expenses |
| **Usage Summary** | Breakdown of fund utilization |

### Refreshing Reports

- **Pull to Refresh**: Swipe down to update financial data
- Data syncs from server automatically
- Offline mode shows cached report

### Export Options

Current version displays reports in-app:
- Future updates may include PDF/Excel export
- Share options via email or messaging apps

---

## Processing Payments

### Processing a New Payment

1. **Open Payment Screen**
   - Tap "Payments" from main menu
   - Payment form appears

2. **Enter Payment Details**

   **Required Fields:**
   - **Amount**: Payment amount (numeric only)
   - **Payment Method**: Select from dropdown:
     - Cash
     - Bank Transfer
     - E-Wallet
     - Credit Card

3. **Validation**
   - Amount must be greater than 0
   - Amount must not exceed maximum limit (1,000,000)
   - Payment method is required

4. **Submit Payment**
   - Tap "Process Payment" button
   - Progress indicator shows during processing
   - Success message displays upon completion

5. **Receipt Generation**
   - Receipt number generated automatically
   - Receipt shows:
     - Receipt number (format: YYYYMMDD-XXXX)
     - Payment amount
     - Payment method
     - Date and time
   - Receipt saved to transaction history

### Handling Payment Errors

**Validation Errors:**
- **Invalid Amount**: Non-numeric or negative values
  - Fix: Enter a positive number
- **Amount Exceeds Limit**: Amount > 1,000,000
  - Fix: Enter a smaller amount or contact support
- **Missing Payment Method**: No method selected
  - Fix: Select a payment method from dropdown

**Network Errors:**
- **No Internet Connection**: Cannot reach server
  - Fix: Connect to internet or try offline mode
- **Server Error**: Server returned error
  - Fix: Try again later or check server status

**Inline Error Feedback:**
- Validation errors display directly on the input field
- Error messages remain visible until corrected
- Error clears automatically when you start typing

### Payment Processing States

| State | Description | User Action |
|-------|-------------|-------------|
| **Idle** | Ready to accept payment | Enter payment details |
| **Processing** | Payment being processed | Wait for completion |
| **Success** | Payment completed successfully | View receipt or process next payment |
| **Error** | Payment failed | Correct error and retry |

### Processing Multiple Payments

1. After successful payment, button re-enables automatically
2. Enter next payment details
3. Submit as described above
4. Repeat for all payments

---

## Viewing Transaction History

### Access Transaction History

1. **Open Payment Screen**
   - Tap "Payments" from main menu

2. **View History Tab** (if available)
   - Transaction list displays all completed payments
   - Each transaction shows:
     - Receipt number
     - Amount
     - Payment method
     - Date and time

### Transaction Details

Each transaction entry displays:

| Field | Description |
|-------|-------------|
| **Receipt #** | Unique transaction identifier |
| **Amount** | Payment amount |
| **Method** | Payment method (Cash, Transfer, etc.) |
| **Date/Time** | When payment was processed |

### Filtering Transactions

Current version shows all transactions in chronological order:
- **Most Recent**: First in list
- **Oldest**: Last in list

Future updates may include:
- Date range filtering
- Payment method filtering
- Search by receipt number

### Refreshing History

- **Pull to Refresh**: Swipe down to update transaction list
- New payments appear automatically after processing

---

## Managing Vendors

### Viewing Vendor List

1. **Access Vendor Management**
   - From main menu, navigate to Communication or dedicated vendor section
   - Vendor list displays all registered vendors

### Vendor Information

Each vendor entry shows:
- **Vendor Name**: Business or service provider name
- **Contact Information**: Phone number and/or email
- **Services Provided**: Type of services offered
- **Rating**: Vendor rating (if available)

### Adding a New Vendor

1. **Open Vendor Form**
   - Tap "Add Vendor" button
   - Fill in vendor details

2. **Required Information**
   - Vendor name
   - Contact phone/email
   - Service category
   - Address (optional)

3. **Save Vendor**
   - Tap "Save" button
   - Vendor added to database

### Updating Vendor Information

1. **Select Vendor**
   - Tap on vendor entry in list

2. **Edit Details**
   - Modify vendor information as needed

3. **Save Changes**
   - Tap "Update" button
   - Changes saved to database

### Removing a Vendor

1. **Select Vendor**
   - Tap on vendor entry

2. **Delete Vendor**
   - Tap "Delete" or "Remove" button
   - Confirm deletion
   - Vendor removed from list

---

## Community Communication

### Sending Messages

1. **Open Communication**
   - Tap "Communication" from main menu
   - Select "Messages" tab

2. **Compose Message**
   - Tap "New Message" button
   - Select recipient from list
   - Type your message
   - Tap "Send"

3. **Message History**
   - View previous conversations
   - Messages show timestamp
   - Read/unread indicators

### Managing Community Posts

1. **Open Community Section**
   - Tap "Communication" from main menu
   - Select "Community Posts" tab

2. **View Posts**
   - All community posts display in feed
   - Each post shows:
     - Author
     - Post content
     - Date and time
     - Comments count

3. **Create New Post**
   - Tap "Create Post" button
   - Write your post content
   - Tap "Post"
   - Post appears in community feed

4. **Interact with Posts**
   - Like posts (tap heart icon)
   - Comment on posts (tap comment icon)
   - Share posts (tap share icon)

---

## Viewing Announcements

### Access Announcements

1. **Open Communication**
   - Tap "Communication" from main menu
   - Select "Announcements" tab

2. **Announcement List**
   - All announcements display in chronological order
   - Newest announcements appear first

### Announcement Details

Each announcement shows:
- **Title**: Announcement heading
- **Content**: Full announcement text
- **Date/Time**: When announcement was posted
- **Priority**: High, Medium, or Low importance
- **Author**: Person who posted announcement

### Managing Announcements

**For Administrators:**
1. **Create Announcement**
   - Tap "New Announcement"
   - Enter title and content
   - Set priority level
   - Tap "Publish"

2. **Edit Announcement**
   - Select announcement from list
   - Modify content
   - Tap "Update"

3. **Delete Announcement**
   - Select announcement
   - Tap "Delete"
   - Confirm deletion

---

## Troubleshooting Common Issues

### App Not Loading Data

**Problem**: App shows empty lists or loading spinner

**Solutions**:
1. **Check Internet Connection**
   - Ensure device has active internet connection
   - Try opening a browser to verify connectivity

2. **Refresh Data**
   - Swipe down on any list to refresh
   - Wait for loading indicator to complete

3. **Check Server Status**
   - Verify API server is online
   - Contact support if server is down

### App Crashes

**Problem**: App unexpectedly closes

**Solutions**:
1. **Restart App**
   - Close app completely
   - Reopen app
   - Try again

2. **Clear App Cache**
   - Go to device Settings → Apps → IuranKomplek
   - Tap "Clear Cache"
   - Restart app

3. **Update App**
   - Check for app updates
   - Install latest version
   - Restart app

### Payment Processing Errors

**Problem**: Payment fails to process

**Solutions**:
1. **Check Payment Details**
   - Verify amount is correct
   - Ensure payment method is selected
   - Check for validation error messages

2. **Check Network Connection**
   - Verify internet connection is active
   - Try again when connection improves

3. **Contact Support**
   - Note the error message
   - Record payment details
   - Contact technical support

### Offline Mode

**Problem**: No internet connection, app shows offline

**Solution**:
- App automatically switches to offline mode
- View cached data from previous sync
- Payment processing may be limited in offline mode

---

## Tips and Best Practices

### For Efficient Usage

1. **Regular Data Sync**
   - Sync data daily for latest updates
   - Use pull-to-refresh before critical tasks

2. **Backup Important Data**
   - Take screenshots of important reports
   - Export data when export features available

3. **Monitor Payment History**
   - Review transaction history regularly
   - Verify all payments recorded correctly

### For Data Accuracy

1. **Verify Payments**
   - Double-check payment amounts before submission
   - Confirm payment method is correct

2. **Review Reports**
   - Cross-check financial reports with payment records
   - Report discrepancies immediately

3. **Keep Contact Information Updated**
   - Maintain accurate user profiles
   - Update email addresses and phone numbers

---

## Getting Help

### In-App Support

1. **Check Troubleshooting**
   - Review this user guide for common issues
   - Try suggested solutions

2. **Contact Support**
   - Use in-app support or contact options
   - Provide detailed issue description
   - Include screenshots if applicable

### External Resources

- **Documentation**: See `docs/` folder for technical guides
- **GitHub Issues**: Report bugs and feature requests
- **Development Team**: Contact for urgent issues

---

## Frequently Asked Questions

### General Questions

**Q: Do I need an account to use the app?**
A: No, the app works without account creation after initial installation.

**Q: Can I use the app offline?**
A: Yes, the app caches data for offline use. Sync occurs when internet is available.

**Q: How often should I sync data?**
A: Daily syncing is recommended for the most up-to-date information.

### Payment Questions

**Q: What payment methods are accepted?**
A: Cash, Bank Transfer, E-Wallet, and Credit Card are supported.

**Q: Is there a maximum payment amount?**
A: Yes, maximum payment amount is 1,000,000 per transaction.

**Q: Can I process multiple payments at once?**
A: Currently, payments are processed one at a time. Batch processing may be added in future updates.

### Data Questions

**Q: How is my data stored?**
A: Data is stored locally on your device and synced with a secure server.

**Q: Can I export my data?**
A: Current version displays data in-app. Export features may be added in future updates.

**Q: How do I update user information?**
A: Contact the administrator to update user profile information.

---

*Last Updated: 2026-01-10*
*Version: 1.0*
*For Technical Support: Contact development team*
