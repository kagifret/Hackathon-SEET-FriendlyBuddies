# Database Setup Guide for Railway

## Steps to Add PostgreSQL Database:

### 1. Add Database to Railway Project
1. Go to your Railway dashboard: https://railway.app/dashboard
2. Open your backend project
3. Click **"New"** → **"Database"** → **"Add PostgreSQL"**
4. Railway will provision a PostgreSQL database

### 2. Get Database Connection Details
After the database is created, go to the PostgreSQL service and find these connection details in the **"Connect"** tab:
- **DATABASE_URL**: Complete connection string
- **Host**: Database host
- **Port**: Database port (usually 5432)
- **Database**: Database name
- **Username**: Database username
- **Password**: Database password

### 3. Set Environment Variables in Railway
In your **backend service** (not the database), go to **"Variables"** tab and add:

```
SPRING_PROFILES_ACTIVE=production
DATABASE_URL=postgresql://[username]:[password]@[host]:[port]/[database]
DATABASE_USERNAME=[your-db-username]
DATABASE_PASSWORD=[your-db-password]
JWT_SECRET=your-secure-jwt-secret-key-here
CORS_ORIGINS=http://localhost:3000,https://matching-tan.vercel.app
```

### 4. Deploy Updated Backend
1. Commit and push your changes:
   ```bash
   git add .
   git commit -m "Add PostgreSQL support and production configuration"
   git push origin main
   ```

2. Railway will automatically redeploy with the new configuration

### 5. Initialize Database
After deployment, your backend will:
- Automatically create database tables (using `spring.jpa.hibernate.ddl-auto=update`)
- You can initialize test data by calling: `POST https://mentoring-backend-production.up.railway.app/api/admin/init-test-data`

## Testing the Database Connection
Once deployed, test the connection:
```bash
curl -s "https://mentoring-backend-production.up.railway.app/api/admin/dashboard"
```

The data should now persist between deployments!

## Environment Variables Summary
Make sure these are set in Railway backend service:
- `SPRING_PROFILES_ACTIVE=production`
- `DATABASE_URL=[postgresql-connection-string]`
- `DATABASE_USERNAME=[db-username]`
- `DATABASE_PASSWORD=[db-password]`
- `JWT_SECRET=[secure-secret]`
- `CORS_ORIGINS=http://localhost:3000,https://matching-tan.vercel.app`