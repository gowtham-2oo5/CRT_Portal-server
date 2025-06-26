-- Add faculty-specific fields to users table
ALTER TABLE users ADD COLUMN department VARCHAR(255);
ALTER TABLE users ADD COLUMN employee_id VARCHAR(50) UNIQUE;

-- Create attendance_sessions table
CREATE TABLE attendance_sessions (
    id CHAR(36) PRIMARY KEY,
    faculty_id CHAR(36) NOT NULL,
    section_id CHAR(36) NOT NULL,
    time_slot_id INT NOT NULL,
    date DATE NOT NULL,
    topic_taught TEXT NOT NULL,
    total_students INT NOT NULL,
    present_count INT NOT NULL,
    absent_count INT NOT NULL,
    attendance_percentage DECIMAL(5,2) NOT NULL,
    submitted_at TIMESTAMP NOT NULL,
    FOREIGN KEY (faculty_id) REFERENCES users(id),
    FOREIGN KEY (section_id) REFERENCES sections(id),
    FOREIGN KEY (time_slot_id) REFERENCES time_slots(id),
    UNIQUE KEY unique_faculty_timeslot_date (faculty_id, time_slot_id, date)
);

-- Add attendance_session_id to attendances table
ALTER TABLE attendances ADD COLUMN attendance_session_id CHAR(36);
ALTER TABLE attendances ADD FOREIGN KEY (attendance_session_id) REFERENCES attendance_sessions(id);

-- Create index for better performance
CREATE INDEX idx_attendance_sessions_faculty_date ON attendance_sessions(faculty_id, date);
CREATE INDEX idx_attendance_sessions_section ON attendance_sessions(section_id);
