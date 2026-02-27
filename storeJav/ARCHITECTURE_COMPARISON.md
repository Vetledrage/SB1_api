# Architecture Comparison: Before vs After

## BEFORE (Original Implementation)

```
┌─────────────────────────────────────┐
│      AuthController                 │
│ ┌───────────────────────────────┐   │
│ │ handleCallback()              │   │
│ │ ├─ ProcessBuilder curl POST   │   │
│ │ │  (hardcoded credentials!)   │   │
│ │ ├─ Parse JSON response        │   │
│ │ ├─ ProcessBuilder curl GET    │   │
│ │ │  (get accounts)             │   │
│ │ └─ Return response            │   │
│ └───────────────────────────────┘   │
│                                     │
│ Problems:                           │
│ ❌ Hardcoded client ID/secret      │
│ ❌ Shell commands (ProcessBuilder)  │
│ ❌ Mixed concerns (auth + API)      │
│ ❌ Hard to test                     │
│ ❌ No proper error handling         │
│ ❌ Manual JSON parsing              │
└─────────────────────────────────────┘
        ↓ via curl ↓
    Sparebank API
```

## AFTER (Refactored Implementation)

```
┌──────────────────────────────────────────────────────────────┐
│                    AuthController                            │
│              (Orchestration/Routing)                         │
│  ├─ Receives OAuth callback                                  │
│  ├─ Delegates to services                                    │
│  └─ Returns response                                         │
└──────────────────┬──────────────────┬───────────────────────┘
                   │                  │
        ┌──────────▼──────────┐   ┌──▼──────────────────┐
        │   OAuthService      │   │ BankingApiService   │
        │ ┌────────────────┐  │   │ ┌────────────────┐  │
        │ │exchangeCode()  │  │   │ │ getAccounts()  │  │
        │ │ ├─ RestTemplate│  │   │ │ ├─RestTemplate │  │
        │ │ ├─ Config      │  │   │ │ ├─Authorization│  │
        │ │ └─ Parse response  │   │ │ └─Error handle │  │
        │ └────────────────┘  │   │ └────────────────┘  │
        └────────────────────┘   └────────────────────┘
                                          ↓
                                   ┌─────────────────┐
                                   │ RestTemplate    │
                                   │ (Spring HTTP)   │
                                   └────────┬────────┘
                                            │
                                            ↓
                                      Sparebank API
                                            ↑
                                            │
                        ┌───────────────────┴────────────────┐
                        │    GlobalExceptionHandler          │
                        │  • OAuthException (401)            │
                        │  • BankingApiException (502)       │
                        │  • General Exception (500)         │
                        │  • Consistent JSON responses       │
                        └────────────────────────────────────┘
```

## Benefits Summary

| Aspect | Before | After |
|--------|--------|-------|
| **Credentials** | Hardcoded in code ❌ | Injected from config ✅ |
| **HTTP Calls** | ProcessBuilder + curl ❌ | RestTemplate ✅ |
| **Testing** | Very difficult ❌ | Easy with mocks ✅ |
| **Error Handling** | Basic try-catch ❌ | Centralized handler ✅ |
| **Code Organization** | Everything in controller ❌ | Separated concerns ✅ |
| **Maintainability** | Low ❌ | High ✅ |
| **Security** | Hardcoded secrets ❌ | Environment vars ✅ |
| **Scalability** | Tight coupling ❌ | Loosely coupled ✅ |

## Flow Comparison

### Before
```
User → /callback → AuthController → ProcessBuilder curl → JSON parsing 
                        ↓
                    (mixed logic)
                        ↓
                   Exception handling
                        ↓
                    Return Response
```

### After
```
User → /callback → AuthController → OAuthService → RestTemplate
                         ↓                              ↓
                    (orchestration)            SparebankAPI (auth)
                         ↓
                   BankingApiService → RestTemplate
                         ↓                  ↓
                    (API calls)         SparebankAPI (data)
                         ↓
                  GlobalExceptionHandler
                         ↓
                    Return Response
```

## Dependency Injection Flow

```
┌────────────────────────────────────┐
│  StoreApplication                  │
│  @SpringBootApplication            │
└────────────────────────────────────┘
        ↓ component scan ↓
┌────────────────────────────────────┐
│  @Configuration Classes            │
├──────────────────────────────────┐ │
│  RestTemplateConfig              │ │
│  @Bean RestTemplate              │ │
└──────────────────────────────────┘ │
└────────────────────────────────────┘
        ↓ bean management ↓
┌────────────────────────────────────────────────────────────┐
│  @Service Classes                                          │
├──────────────────────┬──────────────────────────────────┐  │
│ OAuthService         │ BankingApiService               │  │
│ @Autowired           │ @Autowired                      │  │
│  - RestTemplate      │  - RestTemplate                 │  │
│  - OAuthConfig       │                                 │  │
└──────────────────────┴──────────────────────────────────┘  │
└────────────────────────────────────────────────────────────┘
        ↓ dependency injection ↓
┌────────────────────────────────────┐
│  AuthController                    │
│  @Autowired                        │
│  - OAuthService                    │
│  - BankingApiService               │
└────────────────────────────────────┘
```

This clean architecture makes the code:
- **Testable**: Each component can be mocked
- **Maintainable**: Changes are isolated
- **Scalable**: Easy to add new features
- **Secure**: Credentials properly managed

