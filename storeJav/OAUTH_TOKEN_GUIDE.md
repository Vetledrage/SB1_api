# 🔐 OAuth Token Acquisition Guide

Your application is set up and ready to test the complete OAuth flow to get a real access token from Sparebank.

---

## Your Client Information ✅

```
Client Name: Vetle First Client
Client ID: c3594748-3359-42ad-910f-7f31317900d8
Client Secret: 42ebd1e8-0aec-4247-8a60-d7dae8cb46c9
Financial Institution: fid-sor-norge
Redirect URL: http://localhost:8080/callback
```

---

## Step 1: Start Your Application

Open a terminal and run:

```bash
cd C:\Users\Vetle\Documents\GitHub\SB1_api\storeJav\storeJav
mvn spring-boot:run
```

Wait for: "Started StoreApplication in X.XXX seconds"

---

## Step 2: Initiate OAuth Flow

1. Open browser: **http://localhost:8080**
2. Click **"Login with BankID"** button
3. You'll be redirected to Sparebank OAuth

---

## Step 3: Authenticate with BankID

1. Complete BankID authentication
2. Authorize the client (click approve)
3. Browser will redirect back to: **http://localhost:8080/callback?code=XXXXX&state=XXXXX**

---

## Step 4: Automatic Token Exchange

Your application automatically:
1. **Captures** the authorization code from the redirect
2. **Exchanges** the code for an OAuth access token (using OAuthService)
3. **Fetches** your account data (using BankingApiService)
4. **Displays** your account information

---

## What Your App Does Behind the Scenes

### Capture Authorization Code
```
Step 2: User clicks "Login with BankID"
Step 3: Sparebank redirects to /callback?code=ABC123
```

### Exchange Code for Token (OAuthService)
```
POST https://api.sparebank1.no/oauth/token
Headers:
  Content-Type: application/x-www-form-urlencoded

Body:
  client_id: c3594748-3359-42ad-910f-7f31317900d8
  client_secret: 42ebd1e8-0aec-4247-8a60-d7dae8cb46c9
  code: ABC123 (from redirect)
  grant_type: authorization_code
  state: (from redirect)
  redirect_uri: http://localhost:8080/callback
```

### Get Access Token
```
Response:
{
  "access_token": "YOUR_ACCESS_TOKEN",
  "token_type": "Bearer",
  "expires_in": 15552000,
  "refresh_token": "YOUR_REFRESH_TOKEN",
  ...
}
```

### Fetch Account Data (BankingApiService)
```
GET https://api.sparebank1.no/personal/banking/accounts
Headers:
  Authorization: Bearer YOUR_ACCESS_TOKEN
  Accept: application/vnd.sparebank1.v1+json; charset=utf-8
```

---

## Expected Result in Browser

After successful authentication, you'll see JSON like:

```json
{
  "accounts": [
    {
      "accountNumber": "1234.56.78901",
      "accountName": "Your Account Name",
      "balance": 100000.00,
      ...
    }
  ]
}
```

---

## Testing Manually (Optional)

If you want to test without the browser OAuth flow, you can:

### 1. Get Authorization Code
Open this URL in browser:
```
https://api.sparebank1.no/oauth/authorize
?client_id=c3594748-3359-42ad-910f-7f31317900d8
&state=4641555
&redirect_uri=http://localhost:8080/callback
&finInst=fid-sor-norge
&response_type=code
```

1. Authenticate with BankID
2. Copy the `code` parameter from redirect URL
3. Save it

### 2. Exchange Code for Token
```bash
curl --location --request POST \
  https://api.sparebank1.no/oauth/token \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'client_id=c3594748-3359-42ad-910f-7f31317900d8' \
  --data-urlencode 'client_secret=42ebd1e8-0aec-4247-8a60-d7dae8cb46c9' \
  --data-urlencode 'code=PASTE_YOUR_CODE_HERE' \
  --data-urlencode 'grant_type=authorization_code' \
  --data-urlencode 'state=4641555' \
  --data-urlencode 'redirect_uri=http://localhost:8080/callback'
```

### 3. Test with Access Token
```bash
curl https://api.sparebank1.no/personal/banking/accounts \
  --header 'Authorization: Bearer YOUR_ACCESS_TOKEN' \
  --header 'Accept: application/vnd.sparebank1.v1+json; charset=utf-8'
```

---

## Your Application Flow

```
User Browser
    ↓
http://localhost:8080
    ↓
Click "Login with BankID"
    ↓
Redirect to Sparebank OAuth
    ↓
User authenticates with BankID
    ↓
Sparebank redirects: http://localhost:8080/callback?code=ABC123
    ↓
Your AuthController captures code
    ↓
OAuthService exchanges code for token
    (using your client credentials)
    ↓
BankingApiService fetches account data
    (using access token)
    ↓
Browser displays account JSON
```

---

## Success Indicators

✅ Application starts without errors
✅ Login page loads at http://localhost:8080
✅ Click "Login with BankID" button
✅ BankID login flow works
✅ Redirects back to your app
✅ OAuthService exchanges code for token
✅ BankingApiService fetches accounts
✅ Browser displays account data JSON

---

## Security Notes

⚠️ **Access Token is Valid for**: 10 minutes
⚠️ **Refresh Token is Valid for**: 365 days
⚠️ **Keep Tokens Safe**: Don't share or expose them
⚠️ **If Compromised**: Delete the client in developer portal immediately

---

## Troubleshooting

### Issue: "Invalid Client"
**Cause**: Client ID or secret incorrect
**Solution**: Check your environment variables match your client

### Issue: "Invalid Code"
**Cause**: Authorization code expired (valid for 2 minutes)
**Solution**: Generate new code and use immediately

### Issue: "Redirect URI Mismatch"
**Cause**: Your redirect URI doesn't match configured one
**Solution**: Ensure it's exactly: http://localhost:8080/callback

### Issue: "Port 8080 Already in Use"
**Cause**: Another application using port 8080
**Solution**: Stop other apps or change port in application.properties

---

## Next Steps

1. **Start your application**: `mvn spring-boot:run`
2. **Open browser**: http://localhost:8080
3. **Click login button**: "Login with BankID"
4. **Complete authentication**: Use BankID
5. **View results**: See your account data!

---

**Your application is fully ready to perform the complete OAuth flow!** 🚀

