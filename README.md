# Student Forum MVP

A minimal working version of the "Student Forum" service built with `Spring Boot` and `React`.

## Implemented Features

### Backend
- registration and authentication with `JWT`
- roles: `ROLE_USER`, `ROLE_MODERATOR`, `ROLE_ADMIN`
- forum:
  - topic creation
  - replies in topics
  - file upload when creating a topic
- news feed:
  - news publishing by moderator/admin
  - comments and threaded replies
- embedded `H2` database
- automatic creation of demo users

### Frontend
- login and registration forms
- topic list and topic details
- replies in forum topics
- topic creation with file upload
- news feed and news publishing
- threaded comments for news items
- role display and demo account hints

## Demo Users
- `admin / admin123`
- `moderator / mod12345`
- `student / student123`

## Run Backend

`JDK 21` is required.

### PowerShell
```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.4.7-hotspot'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
cmd /c mvnw.cmd spring-boot:run
```

Backend runs at `http://localhost:8080`.

`H2 console`: `http://localhost:8080/h2-console`

Connection settings:
- JDBC URL: `jdbc:h2:file:./data/forumdb;AUTO_SERVER=TRUE`
- User: `sa`
- Password: empty

## Run Frontend
```powershell
cd frontend
cmd /c npm install
cmd /c npm run dev
```

Frontend runs at `http://localhost:5173`.

## Build Verification

### Backend
```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.4.7-hotspot'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
cmd /c mvnw.cmd test
```

### Frontend
```powershell
cd frontend
cmd /c npm run build
```
