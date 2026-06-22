# points-mall-thirdparty-connector

> Lightweight Node.js aggregation service — the **single integration point** for all external platforms. Business services never talk to third-party APIs directly; every external call is routed through here.

## Integrated Platforms

| Platform | Purpose |
|----------|---------|
| **GitHub OAuth 2.0** | Employee single sign-on: authorization redirect, token exchange, user profile fetch |
| **AWS S3** | Archive file storage: attendance records, monthly reports; presigned URL generation, lifecycle policy config |
| **Amazon Product Advertising API** | Product catalog sourcing for the points mall; signed request auth (AWS Signature V4), response normalization |
| **SendGrid** | Transactional email push: birthday bonus notice, order confirmation, attendance anomaly alert; template rendering, async send, retry logic |

## Responsibilities

- **Unified Auth Encapsulation** — each platform's signing algorithm / OAuth flow is abstracted behind a clean internal interface
- **Response Normalization** — transforms each platform's idiosyncratic response format into a consistent internal schema before returning to BFF
- **Timeout & Retry** — configurable per-platform timeout; exponential backoff retry on transient failures
- **Graceful Degradation** — returns a structured fallback response on third-party unavailability instead of propagating raw errors upstream
- **Secret Centralization** — all API keys, OAuth client secrets, and AWS credentials live only in this service's environment config

## Tech Stack

| Layer | Technology |
|-------|------------|
| Framework | Node.js 20, Express 4, TypeScript |
| AWS | `@aws-sdk/client-s3`, `@aws-sdk/s3-request-presigner` |
| Email | `@sendgrid/mail` |
| HTTP Client | Axios (Amazon API, GitHub API) |
| Auth | Internal HMAC shared-secret (called only by BFF, not exposed to frontend) |

## Local Development

```bash
pnpm install
pnpm run dev
# API: http://localhost:8084
```

## Key Environment Variables

```env
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
AWS_S3_BUCKET=your-bucket-name
AMAZON_AFFILIATE_ACCESS_KEY=your-paapi-key
AMAZON_AFFILIATE_SECRET_KEY=your-paapi-secret
AMAZON_AFFILIATE_PARTNER_TAG=your-tag
SENDGRID_API_KEY=your-sendgrid-key
SENDGRID_FROM_EMAIL=noreply@yourcompany.com
INTERNAL_HMAC_SECRET=shared-secret-with-bff
```

## Why a Separate Connector Service?

Each external platform has unique auth rules, signing algorithms, and response shapes. Scattering these integrations across business services creates tight coupling and maintenance overhead. By consolidating them here:

1. Business services stay focused on their domain logic — no third-party SDK sprawl
2. Timeout/retry/fallback policies are enforced in one place
3. Replacing or adding a third-party provider only touches this service
4. API keys never leak into business service codebases
