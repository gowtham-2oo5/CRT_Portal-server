# CRT Eligibility Feature

## Overview
This feature adds the ability to manage student eligibility for the CRT (Corporate Readiness Training) program. Students can be removed from or added back to the CRT program with proper reasoning and feedback tracking.

## Database Changes

### Student Model Updates
- **`crtEligibility`**: Boolean field (default: `true`)
  - `true`: Student is eligible and active in CRT
  - `false`: Student is not eligible/removed from CRT
- **`feedback`**: String field (max 500 characters)
  - Stores the reason for eligibility status changes

## API Endpoints

### Remove Student from CRT
```http
POST /api/students/{studentId}/remove-from-crt?reason={reason}
Authorization: Bearer <admin_token>
```

**Description**: Removes a student from CRT program
**Access**: Admin only
**Parameters**:
- `studentId` (path): UUID of the student
- `reason` (query): Reason for removal (required)

**Response**: Updated StudentDTO with `crtEligibility: false`

### Add Student to CRT
```http
POST /api/students/{studentId}/add-to-crt?reason={reason}
Authorization: Bearer <admin_token>
```

**Description**: Adds a student back to CRT program
**Access**: Admin only
**Parameters**:
- `studentId` (path): UUID of the student
- `reason` (query): Reason for addition (required)

**Response**: Updated StudentDTO with `crtEligibility: true`

## Updated DTOs

### StudentDTO
```json
{
  "id": "uuid",
  "name": "string",
  "email": "string",
  "phone": "string",
  "regNum": "string",
  "department": "string",
  "batch": "enum",
  "crtEligibility": "boolean",
  "feedback": "string",
  "attendancePercentage": "double"
}
```

## Business Logic

### Default Behavior
- New students are created with `crtEligibility: true` by default
- Bulk imports also set `crtEligibility: true` for all students

### Attendance Impact
- Students with `crtEligibility: false` should not have their attendance calculated
- Frontend validation should prevent attendance marking for ineligible students
- Backend attendance services can check this field for additional validation

### Audit Trail
- Each eligibility change is logged with:
  - Student registration number
  - New eligibility status
  - Reason for change
  - Timestamp (automatic via logging)

## Usage Examples

### Remove Student from CRT
```bash
curl -X POST "http://localhost:8080/api/students/123e4567-e89b-12d3-a456-426614174000/remove-from-crt?reason=Poor%20attendance%20record" \
  -H "Authorization: Bearer <admin_token>"
```

### Add Student Back to CRT
```bash
curl -X POST "http://localhost:8080/api/students/123e4567-e89b-12d3-a456-426614174000/add-to-crt?reason=Improved%20performance%20and%20commitment" \
  -H "Authorization: Bearer <admin_token>"
```

## Security
- Only users with `ADMIN` role can modify CRT eligibility
- All changes are logged for audit purposes
- Input validation ensures reason is provided and not empty

## Database Migration
When deploying this feature, the following SQL will be executed automatically by Hibernate:

```sql
ALTER TABLE students 
ADD COLUMN crt_eligibility BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN feedback VARCHAR(500);
```

## Testing
- Unit tests cover the service layer logic
- Integration tests verify the API endpoints
- Security tests ensure proper role-based access control
