# List Processing Application

## Stack
- **Backend**: Spring Boot 3.4 · Java 21 · Spring Security JWT · WebSocket STOMP · Apache POI
- **Frontend**: Angular 19 · AG Grid · SockJS/STOMP
- **Database**: PostgreSQL 16

## Start
```bash
docker-compose up -d          # postgres
cd backend && mvn spring-boot:run
cd frontend && npm install && ng serve
```

## Default accounts
| user  | password | role  |
|-------|----------|-------|
| admin | admin123 | ADMIN |
| user1 | user123  | USER  |
| user2 | user123  | USER  |
| user3 | user123  | USER  |

## Feature matrix
| Feature                     | ADMIN | USER |
|-----------------------------|:-----:|:----:|
| Upload Excel list           | ✅ | ❌ |
| View full list              | ✅ | ❌ |
| View assigned rows          | ✅ | ✅ |
| Add row manually            | ✅ | ❌ |
| Delete row                  | ✅ | ❌ |
| Assign rows to user         | ✅ | ❌ |
| Download full Excel         | ✅ | ❌ |
| Configure column rights     | ✅ | ❌ |
| Edit authorized columns     | ✅ | ✅ |
| Per-column filters          | ✅ | ✅ |
| Real-time sync (WebSocket)  | ✅ | ✅ |
