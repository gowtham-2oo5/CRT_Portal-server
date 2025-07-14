# CRT Portal API Testing Guide

This directory contains comprehensive HTTP test files for all controllers in the CRT Portal API. These files are designed to work with REST clients like IntelliJ IDEA's HTTP Client, VS Code REST Client, or Postman.

## 📁 File Structure

```
http-calls/
├── auth-controller.http                    # Authentication endpoints
├── user-controller.http                    # User management
├── password-controller.http                # Password operations
├── room-controller.http                    # Room management
├── Training-controller.http                 # Training operations
├── student-controller.http                 # Student management
├── section-controller.http                 # Section operations
├── time-slot-controller.http              # Time slot management
├── section-schedule-controller.http        # Schedule management
├── attendance-controller.http              # Attendance tracking
├── faculty-dashboard-controller.http       # Faculty dashboard
├── faculty-reports-controller.http         # Faculty reports
├── faculty-analytics-controller.http       # Faculty analytics
├── bulk-operations-controller.http         # Bulk operations
├── admin-dashboard-controller.http         # Admin dashboard
└── test-controller.http                   # Test endpoints
```

## 🚀 Getting Started

### 1. Authentication First
Start with `auth-controller.http`:
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "admin@crt.com",
  "password": "admin123"
}
```

### 2. Get JWT Token
After login, verify OTP and get your JWT token:
```http
POST http://localhost:8080/api/auth/verify-otp
Content-Type: application/json

{
  "usernameOrEmail": "admin@crt.com",
  "otp": "123456"
}
```

### 3. Update Authorization Token
Replace `YOUR_JWT_TOKEN_HERE` in all files with your actual JWT token.

## 📋 Testing Workflow

### Phase 1: Setup Base Data
1. **Users** → Create admin and faculty users
2. **Rooms** → Create rooms for scheduling
3. **Trainings** → Create Training profiles
4. **Students** → Create student records

### Phase 2: Academic Structure
1. **Sections** → Create sections and assign Trainings
2. **Time Slots** → Create time slots for classes
3. **Section Schedules** → Link sections with time slots and rooms

### Phase 3: Operations
1. **Attendance** → Mark attendance for sessions
2. **Faculty Dashboard** → Test faculty operations
3. **Reports & Analytics** → Generate reports

### Phase 4: Bulk Operations
1. **CSV Uploads** → Test bulk data import
2. **Bulk Attendance** → Test bulk attendance marking

## 🔧 Configuration

### Environment Variables
Update the base URL if needed:
```http
@baseUrl = http://localhost:8080
```

### Authentication Token
Update this in each file after login:
```http
@authToken = Bearer YOUR_ACTUAL_JWT_TOKEN_HERE
```

## 📊 Test Data

### CSV Files Location
```
test-data/
├── students.csv              # 15 sample students
├── Trainings.csv              # 15 sample Trainings
├── rooms.csv                 # 20 sample rooms
├── section-students.csv      # Students for section registration
├── attendance.csv            # Morning attendance data
└── attendance-afternoon.csv  # Afternoon attendance data
```

### Sample UUIDs for Testing
Replace these placeholder UUIDs with actual ones from your responses:
- User ID: `550e8400-e29b-41d4-a716-446655440000`
- Room ID: `550e8400-e29b-41d4-a716-446655440001`
- Training ID: `550e8400-e29b-41d4-a716-446655440002`
- Student ID: `550e8400-e29b-41d4-a716-446655440003`
- Section ID: `550e8400-e29b-41d4-a716-446655440004`

## 🎯 Testing Scenarios

### Scenario 1: Complete Faculty Workflow
1. Login as faculty
2. Get dashboard data
3. View today's timetable
4. Get students for a section
5. Submit attendance
6. View reports

### Scenario 2: Admin Management
1. Login as admin
2. Create users, rooms, Trainings
3. Set up sections and schedules
4. View dashboard metrics
5. Check recent activities

### Scenario 3: Bulk Operations
1. Upload student CSV
2. Upload Training CSV
3. Create rooms from CSV
4. Register students to sections
5. Mark bulk attendance

## 🔍 Common Issues & Solutions

### Issue: 401 Unauthorized
**Solution**: Update JWT token in `@authToken` variable

### Issue: 404 Not Found
**Solution**: Replace placeholder UUIDs with actual IDs from POST responses

### Issue: 400 Bad Request
**Solution**: Check JSON format and required fields

### Issue: CSV Upload Fails
**Solution**: Ensure CSV files exist in `test-data/` directory

## 📝 Best Practices

1. **Sequential Testing**: Follow the workflow phases in order
2. **ID Management**: Save UUIDs from POST responses for GET/PUT/DELETE operations
3. **Token Refresh**: Refresh JWT tokens when they expire
4. **Data Cleanup**: Use DELETE endpoints to clean up test data
5. **Error Handling**: Check response status and error messages

## 🛠️ IDE Setup

### IntelliJ IDEA
1. Install HTTP Client plugin (built-in)
2. Open `.http` files
3. Click the green arrow to execute requests

### VS Code
1. Install "REST Client" extension
2. Open `.http` files
3. Click "Send Request" above each request

### Postman
1. Import the HTTP files as raw text
2. Set up environment variables
3. Execute requests in collections

## 📈 Monitoring

### Response Codes
- `200`: Success
- `201`: Created
- `400`: Bad Request
- `401`: Unauthorized
- `404`: Not Found
- `500`: Server Error

### Performance Testing
Use the bulk operations to test system performance with large datasets.

## 🔄 Continuous Testing

1. Set up automated testing with these files
2. Use in CI/CD pipelines
3. Regular regression testing
4. Performance benchmarking

---

**Happy Testing! 🚀**

For issues or questions, refer to the API documentation or contact the development team.
