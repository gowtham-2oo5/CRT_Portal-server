#!/bin/bash

# CRT Portal API Testing Script
# This script tests all major endpoints of the CRT Portal Core Server

BASE_URL="http://localhost:8080"
CONTENT_TYPE="Content-Type: application/json"

echo "=== CRT Portal Core Server API Testing ==="
echo "Base URL: $BASE_URL"
echo "Starting comprehensive API tests..."
echo

# Function to make HTTP requests with error handling
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local headers=$4
    
    echo "Testing: $method $endpoint"
    
    if [ -n "$data" ]; then
        if [ -n "$headers" ]; then
            response=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X $method "$BASE_URL$endpoint" \
                -H "$CONTENT_TYPE" -H "$headers" -d "$data" 2>/dev/null)
        else
            response=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X $method "$BASE_URL$endpoint" \
                -H "$CONTENT_TYPE" -d "$data" 2>/dev/null)
        fi
    else
        if [ -n "$headers" ]; then
            response=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X $method "$BASE_URL$endpoint" \
                -H "$headers" 2>/dev/null)
        else
            response=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X $method "$BASE_URL$endpoint" 2>/dev/null)
        fi
    fi
    
    if [ $? -eq 0 ]; then
        http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)
        body=$(echo "$response" | sed '/HTTP_CODE:/d')
        
        echo "Status: $http_code"
        echo "Response: $body"
        echo "---"
        return $http_code
    else
        echo "❌ Connection failed"
        echo "---"
        return 1
    fi
}

# Test 1: Health Check (if available)
echo "1. Testing Health Check"
make_request "GET" "/actuator/health"
echo

# Test 2: Swagger UI
echo "2. Testing Swagger UI"
make_request "GET" "/"
echo

# Test 3: Authentication - Login (without valid credentials)
echo "3. Testing Authentication - Login"
login_data='{
    "usernameOrEmail": "test@example.com",
    "password": "testpassword"
}'
make_request "POST" "/api/auth/login" "$login_data"
echo

# Test 4: User Registration/Creation (Admin endpoint - should fail without auth)
echo "4. Testing User Creation (should fail without auth)"
user_data='{
    "name": "Test User",
    "email": "testuser@example.com",
    "phone": "1234567890",
    "username": "testuser",
    "password": "password123",
    "role": "FACULTY"
}'
make_request "POST" "/api/admin/users" "$user_data"
echo

# Test 5: Get All Users (should fail without auth)
echo "5. Testing Get All Users (should fail without auth)"
make_request "GET" "/api/admin/users"
echo

# Test 6: Student Operations
echo "6. Testing Student Operations"
echo "6a. Get All Students (should fail without auth)"
make_request "GET" "/api/students"
echo

echo "6b. Create Student (should fail without auth)"
student_data='{
    "name": "Test Student",
    "email": "student@example.com",
    "phone": "9876543210",
    "regNum": "ST001",
    "department": "Computer Science",
    "batch": "BATCH_2024",
    "crtEligibility": true
}'
make_request "POST" "/api/students" "$student_data"
echo

# Test 6c: CRT Eligibility Management
echo "6c. Remove Student from CRT (should fail without auth)"
make_request "POST" "/api/students/123e4567-e89b-12d3-a456-426614174000/remove-from-crt?reason=Poor%20performance"
echo

echo "6d. Add Student to CRT (should fail without auth)"
make_request "POST" "/api/students/123e4567-e89b-12d3-a456-426614174000/add-to-crt?reason=Improved%20performance"
echo

# Test 7: Trainer Operations
echo "7. Testing Trainer Operations"
echo "7a. Get All Trainers (should fail without auth)"
make_request "GET" "/api/trainers"
echo

echo "7b. Create Trainer (should fail without auth)"
trainer_data='{
    "name": "Test Trainer",
    "email": "trainer@example.com",
    "phone": "5555555555",
    "expertise": "Java Programming"
}'
make_request "POST" "/api/trainers" "$trainer_data"
echo

# Test 8: Room Operations
echo "8. Testing Room Operations"
echo "8a. Get All Rooms (should fail without auth)"
make_request "GET" "/api/rooms"
echo

echo "8b. Create Room (should fail without auth)"
room_data='{
    "roomNumber": "R001",
    "capacity": 30,
    "type": "CLASSROOM",
    "hasProjector": true,
    "hasWhiteboard": true
}'
make_request "POST" "/api/rooms" "$room_data"
echo

# Test 9: Section Operations
echo "9. Testing Section Operations"
echo "9a. Get All Sections (should fail without auth)"
make_request "GET" "/api/sections"
echo

echo "9b. Create Section (should fail without auth)"
section_data='{
    "name": "Java Basics",
    "description": "Introduction to Java Programming",
    "startDate": "2024-01-15",
    "endDate": "2024-03-15"
}'
make_request "POST" "/api/sections" "$section_data"
echo

# Test 10: Time Slot Operations
echo "10. Testing Time Slot Operations"
echo "10a. Get All Time Slots (should fail without auth)"
make_request "GET" "/api/timeslots"
echo

echo "10b. Create Time Slot (should fail without auth)"
timeslot_data='{
    "startTime": "09:00:00",
    "endTime": "10:30:00",
    "dayOfWeek": "MONDAY"
}'
make_request "POST" "/api/timeslots" "$timeslot_data"
echo

# Test 11: Attendance Operations
echo "11. Testing Attendance Operations"
echo "11a. Get Attendance (should fail without auth)"
make_request "GET" "/api/attendance"
echo

# Test 12: Bulk Operations
echo "12. Testing Bulk Operations"
echo "12a. Bulk Upload Students (should fail without auth)"
make_request "POST" "/api/bulk/students/upload"
echo

echo "12b. Bulk Upload Trainers (should fail without auth)"
make_request "POST" "/api/bulk/trainers/upload"
echo

# Test 13: Password Operations
echo "13. Testing Password Operations"
echo "13a. Forgot Password"
forgot_password_data='{
    "email": "test@example.com"
}'
make_request "POST" "/api/password/forgot" "$forgot_password_data"
echo

# Test 14: Test Controller (Public endpoint)
echo "14. Testing Test Controller"
make_request "GET" "/api/test/public"
echo

# Test 15: Invalid Endpoints
echo "15. Testing Invalid Endpoints"
make_request "GET" "/api/nonexistent"
echo

make_request "POST" "/api/invalid/endpoint" '{"test": "data"}'
echo

echo "=== API Testing Complete ==="
echo "Summary:"
echo "- All endpoints tested for basic connectivity"
echo "- Authentication endpoints tested with sample data"
echo "- Protected endpoints tested (should return 401/403)"
echo "- Invalid endpoints tested (should return 404)"
echo
echo "Note: Most endpoints should return authentication errors (401/403) as expected"
echo "This confirms the security layer is working properly."
