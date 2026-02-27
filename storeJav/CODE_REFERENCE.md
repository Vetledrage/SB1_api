# Key Implementation Details - Code Snippets

## Environment Variables Configuration

### How to Set Up

**Windows PowerShell:**
```powershell
$env:SPAREBANK_CLIENT_ID = "your-actual-client-id"
$env:SPAREBANK_CLIENT_SECRET = "your-actual-client-secret"
```

**Windows Command Prompt:**
```cmd
set SPAREBANK_CLIENT_ID=your-actual-client-id
set SPAREBANK_CLIENT_SECRET=your-actual-client-secret
```

**Or update application.yml directly:**
```yaml
sparebank:
  oauth:
    client-id: your-actual-client-id
    client-secret: your-actual-client-secret
    redirect-uri: http://localhost:8080/callback
    token-uri: https://api.sparebank1.no/oauth/token
```

## API Endpoint Flow

### Request Flow
```
User Browser
    ↓
GET /callback?code=ABC123&state=XYZ789
    ↓
AuthController.handleCallback()
    ├─ calls OAuthService.exchangeCodeForToken(code, state)
    │  ├─ POST to https://api.sparebank1.no/oauth/token
    │  ├─ with client credentials from SparebankOauthConfig
    │  └─ returns TokenResponse with access_token
    │
    └─ calls BankingApiService.getAccounts(accessToken)
       ├─ GET from https://api.sparebank1.no/personal/banking/accounts
       ├─ with Authorization: Bearer {access_token}
       └─ returns account JSON
    ↓
200 OK with account data JSON
    ↓
User Browser displays response
```

## Error Handling Examples

### OAuth Error (Unauthorized - 401)
```json
{
  "error": "authentication_failed",
  "message": "Failed to authenticate with Sparebank",
  "details": "Token exchange failed: Invalid authorization code"
}
```

### Banking API Error (Bad Gateway - 502)
```json
{
  "error": "banking_api_failed",
  "message": "Failed to fetch banking data",
  "details": "Failed to fetch accounts: API returned 500"
}
```

### General Error (Internal Server Error - 500)
```json
{
  "error": "internal_server_error",
  "message": "An unexpected error occurred"
}
```

## Testing Examples

### Running All Tests
```bash
cd c:\Users\Vetle\Documents\GitHub\SB1_api\storeJav\storeJav
mvn clean test
```

### Running Specific Test Class
```bash
# OAuth Service Tests
mvn test -Dtest=OAuthServiceTest

# Banking Service Tests
mvn test -Dtest=BankingApiServiceTest

# Controller Tests
mvn test -Dtest=AuthControllerTest
```

### Running Tests from IDE
1. Open test file in IDE
2. Right-click class name
3. Select "Run 'ClassName'"
4. Or press Ctrl+Shift+F10

## Key Classes Reference

### AuthController
- **Location**: `src/main/java/.../AuthController.java`
- **Responsibility**: HTTP request routing and orchestration
- **Key Method**: `handleCallback(code, state)`
- **Returns**: ResponseEntity with account data or error

### OAuthService
- **Location**: `src/main/java/.../service/OAuthService.java`
- **Responsibility**: OAuth token exchange
- **Key Method**: `exchangeCodeForToken(code, state)`
- **Returns**: TokenResponse with access_token
- **Exception**: Throws OAuthException on failure

### BankingApiService
- **Location**: `src/main/java/.../service/BankingApiService.java`
- **Responsibility**: Banking API calls
- **Key Method**: `getAccounts(accessToken)`
- **Returns**: JSON string with account data
- **Exception**: Throws BankingApiException on failure

### SparebankOauthConfig
- **Location**: `src/main/java/.../config/SparebankOauthConfig.java`
- **Responsibility**: OAuth configuration properties
- **Properties**: clientId, clientSecret, redirectUri, tokenUri
- **Source**: Environment variables or application.yml

### GlobalExceptionHandler
- **Location**: `src/main/java/.../exception/GlobalExceptionHandler.java`
- **Responsibility**: Centralized exception handling
- **Annotation**: @ControllerAdvice
- **Handlers**: OAuth, Banking API, and General exceptions

## Common Issues & Solutions

### Issue: "OAuth credentials not found"
**Cause**: Environment variables not set
**Solution**: 
```powershell
$env:SPAREBANK_CLIENT_ID = "your-id"
$env:SPAREBANK_CLIENT_SECRET = "your-secret"
```

### Issue: "RestTemplate not autowired"
**Cause**: RestTemplateConfig not loaded
**Solution**: Ensure RestTemplateConfig is in the same package or component scanned
```java
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### Issue: "Test mocks not working"
**Cause**: @MockBean not used on service mocks
**Solution**: In controller test, use @MockBean:
```java
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @MockBean
    private OAuthService oauthService;
    
    @MockBean
    private BankingApiService bankingApiService;
}
```

## Migration from Old Code

### Before (Old Code Pattern)
```java
ProcessBuilder tokenPb = new ProcessBuilder(
    "curl",
    "-sS",
    "--request", "POST", "https://api.sparebank1.no/oauth/token",
    "--data-urlencode", "client_id=HARDCODED_ID",
    "--data-urlencode", "client_secret=HARDCODED_SECRET"
);
```

### After (New Code Pattern)
```java
// In OAuthService.java
TokenResponse tokenResponse = oauthService.exchangeCodeForToken(code, state);
// Credentials are injected from SparebankOauthConfig
```

## Building and Running

### Clean Build
```bash
mvn clean build
```

### Run Tests
```bash
mvn test
```

### Run Application
```bash
mvn spring-boot:run
```

### Package for Deployment
```bash
mvn clean package
# Creates target/store-0.0.1-SNAPSHOT.jar
```

## Database / Persistence (Future)

When adding database support, inject repository into services:
```java
@Service
public class OAuthService {
    @Autowired
    private UserTokenRepository tokenRepository;
    
    public TokenResponse exchangeCodeForToken(String code, String state) {
        // ... get token ...
        tokenRepository.save(new UserToken(token));
        return tokenResponse;
    }
}
```

## Monitoring & Logging

Current logging uses SLF4J with Logback (default in Spring Boot):

```java
private static final Logger logger = LoggerFactory.getLogger(OAuthService.class);

logger.info("Exchanging authorization code for access token");
logger.error("Failed to exchange code for token", e);
```

To adjust log levels, add to application.yml:
```yaml
logging:
  level:
    com.vetleDemo.store: DEBUG
    org.springframework.web: WARN
```

