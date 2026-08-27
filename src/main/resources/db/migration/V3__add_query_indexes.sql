CREATE INDEX idx_student_name ON student (last_name, first_name);
CREATE INDEX idx_company_name ON company (company_name);
CREATE INDEX idx_interview_round_name ON interview_round (round_name);
CREATE INDEX idx_company_custom_round_company ON company_custom_round (company_id);
CREATE INDEX idx_student_application_student ON student_application (student_id);
CREATE INDEX idx_student_application_company ON student_application (company_id);
CREATE INDEX idx_student_round_selection_student ON student_round_selection (student_id);
CREATE INDEX idx_student_round_selection_round ON student_round_selection (company_round_id);
