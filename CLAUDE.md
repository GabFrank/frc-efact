# FRC E-Fact (Facturación Electrónica)

## Stack
- **Backend**: Java / Spring Boot / Maven / PostgreSQL
- **Frontend**: Angular / TypeScript

## Build & Run
### Backend (`frc-efact-backend/`)
- Build: `./mvnw clean package`
- Run: `./mvnw spring-boot:run`
- Dev: `./dev.sh`

### Frontend (`frc-efact-frontend/`)
- Start: `npm start`
- Build prod: `npm run build:prod`
- Test: `npm run test:ci`
- Lint: `npm run lint`

## Project Structure
- `frc-efact-backend/` - Spring Boot API
- `frc-efact-frontend/` - Angular web frontend
- `certificates/` - SSL/signing certificates
- `deployment-data/` - Deployment configuration

## Conventions
- Follow existing code patterns and naming conventions
- Use Spanish for business domain terms as established in the codebase
