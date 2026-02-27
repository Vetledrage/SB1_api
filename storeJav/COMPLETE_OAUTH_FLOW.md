# 🔐 Complete OAuth Flow - Step by Step

Your application is fully configured to handle the complete OAuth 2.0 flow automatically.

---

## Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         YOUR APPLICATION                        │
│                 (Already Configured & Ready!)                   │
└─────────────────────────────────────────────────────────────────┘

Your Client Credentials:
├─ Client ID: c3594748-3359-42ad-910f-7f31317900d8
├─ Client Secret: 42ebd1e8-0aec-4247-8a60-d7dae8cb46c9
└─ Redirect URL: http://localhost:8080/callback
```

---

## Complete Flow (5 Steps)

### Step 1️⃣: User Initiates Login
```
User Browser
    │
    └─→ Opens: http://localhost:8080
        │
        └─→ Sees login page with "Login with BankID" button
```

### Step 2️⃣: Redirect to Sparebank OAuth
```
User clicks "Login with BankID"
    │
    └─→ Browser redirects to:
        https://api.sparebank1.no/oauth/authorize
        ?client_id=c3594748-3359-42ad-910f-7f31317900d8
        &state=4641555
        &redirect_uri=http://localhost:8080/callback
        &finInst=fid-sor-norge
        &response_type=code
```

### Step 3️⃣: BankID Authentication
```
Sparebank OAuth Page
    │
    └─→ User authenticates with BankID
        │
        └─→ User authorizes client access
            │
            └─→ Sparebank generates authorization code
```

### Step 4️⃣: Return to Your App with Code
```
Sparebank redirects browser to:
    http://localhost:8080/callback?code=ABC123&state=4641555
        │
        └─→ Your AuthController captures the code
```

### Step 5️⃣: Your App Exchanges Code for Token
```
AuthController.handleCallback()
    │
    ├─→ OAuthService.exchangeCodeForToken()
    │   ├─ Prepares POST request
    │   ├─ Sends to: https://api.sparebank1.no/oauth/token
    │   ├─ Includes:
    │   │   ├─ client_id
    │   │   ├─ client_secret
    │   │   ├─ code (from redirect)
    │   │   ├─ grant_type: authorization_code
    │   │   └─ redirect_uri
    │   │
    │   └─ Receives TokenResponse:
    │       ├─ access_token (valid 10 minutes)
    │       ├─ refresh_token (valid 365 days)
    │       ├─ token_type: Bearer
    │       └─ expires_in: 15552000
    │
    └─→ BankingApiService.getAccounts()
        ├─ Prepares GET request
        ├─ Sends to: https://api.sparebank1.no/personal/banking/accounts
        ├─ Includes header:
        │   └─ Authorization: Bearer [access_token]
        │
        └─ Receives account data:
            ├─ accounts[]
            │   ├─ accountNumber
            │   ├─ accountName
            │   ├─ balance
            │   └─ ...
```

### Step 6️⃣: Display Results
```
Browser displays account data JSON
    │
    └─→ User sees their account information
```

---

## Timeline

```
T+0s    User clicks "Login with BankID"
        ↓
T+5s    BankID authentication page loads
        ↓
T+30s   User authenticates and authorizes
        ↓
T+35s   Sparebank redirects with authorization code
        ↓
T+40s   Your app exchanges code for access token
        ↓
T+45s   Your app fetches account data
        ↓
T+50s   Browser displays account information
```

---

## What Happens Behind the Scenes

### Your AuthController
```java
@GetMapping("/callback")
public ResponseEntity<String> handleCallback(
    @RequestParam("code") String code,
    @RequestParam("state") String state
) {
    // Step 1: Exchange code for token
    TokenResponse tokenResponse = 
        oauthService.exchangeCodeForToken(code, state);
    
    // Step 2: Fetch account data using token
    String accountData = 
        bankingApiService.getAccounts(
            tokenResponse.getAccessToken()
        );
    
    // Step 3: Return account data
    return ResponseEntity.ok(accountData);
}
```

### OAuthService Doing Step 4️⃣
```java
public TokenResponse exchangeCodeForToken(String code, String state) {
    // Build request with your credentials
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("client_id", oauthConfig.getClientId());
    body.add("client_secret", oauthConfig.getClientSecret());
    body.add("code", code);
    body.add("grant_type", "authorization_code");
    body.add("state", state);
    body.add("redirect_uri", oauthConfig.getRedirectUri());
    
    // POST to Sparebank
    String response = restTemplate.postForObject(
        oauthConfig.getTokenUri(),
        new HttpEntity<>(body, headers),
        String.class
    );
    
    // Parse and return token
    return objectMapper.readValue(response, TokenResponse.class);
}
```

### BankingApiService Doing Step 5️⃣
```java
public String getAccounts(String accessToken) {
    // Build request with token
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);
    headers.set("Accept", "application/vnd.sparebank1.v1+json");
    
    // GET from Sparebank
    ResponseEntity<String> response = restTemplate.exchange(
        "https://api.sparebank1.no/personal/banking/accounts",
        HttpMethod.GET,
        new HttpEntity<>(headers),
        String.class
    );
    
    // Return account data
    return response.getBody();
}
```

---

## Security at Each Step

```
Step 1: User initiates
   └─ ✅ No credentials sent

Step 2: Redirect to OAuth
   └─ ✅ Uses HTTPS
   └─ ✅ Code is temporary (2 minutes)

Step 3: BankID Authentication
   └─ ✅ Bank handles authentication
   └─ ✅ User authorizes explicitly

Step 4: Return with Code
   └─ ✅ Code only (not token)
   └─ ✅ Can only be used once
   └─ ✅ Tied to redirect_uri

Step 5: Exchange Code for Token
   └─ ✅ Only server-to-server (not browser)
   └─ ✅ Requires client secret
   └─ ✅ Token valid 10 minutes only

Step 6: Use Token for API
   └─ ✅ Token sent over HTTPS
   └─ ✅ Scoped to specific permissions
   └─ ✅ Can be refreshed with refresh_token
```

---

## Token Lifecycle

```
Access Token
├─ Obtained: Step 5
├─ Valid for: 10 minutes
├─ Used for: API calls
└─ Expires: Automatically
   └─ Use refresh_token to get new one

Refresh Token
├─ Obtained: Step 5
├─ Valid for: 365 days
├─ Used for: Getting new access_tokens
└─ Never expires: Until manually revoked
```

---

## Your Application is Already Doing This! ✅

```
✅ Step 1: AuthController ready
✅ Step 2: Redirect URL configured
✅ Step 3: Sparebank handles this
✅ Step 4: Automatic with your service
✅ Step 5: OAuthService handles this
✅ Step 6: BankingApiService handles this
```

---

## How to Test

1. **Start app**: `mvn spring-boot:run`
2. **Open browser**: http://localhost:8080
3. **Click button**: "Login with BankID"
4. **Authenticate**: Complete BankID login
5. **See result**: Account data displayed

That's it! Your app handles everything else automatically! 🎉

---

## Expected Result

Browser shows:
```json
{
  "accounts": [
    {
      "accountNumber": "1234.56.78901",
      "accountName": "Your Account Name",
      "balance": 100000.00,
      "currency": "NOK",
      "type": "Savings Account",
      ...
    }
  ]
}
```

---

## What's Stored Internally

After successful authentication, your app has:

```
TokenResponse {
  access_token: "abc123...", (valid 10 min)
  refresh_token: "xyz789...", (valid 365 days)
  token_type: "Bearer",
  expires_in: 15552000,
  ...
}

User's Account Data {
  accounts: [
    {
      accountNumber: "...",
      balance: 100000.00,
      ...
    }
  ]
}
```

---

**Your application is fully configured and ready to perform this entire OAuth flow!** 🚀

Just run `mvn spring-boot:run` and click the login button!

