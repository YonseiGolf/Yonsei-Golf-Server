-- Document and final failures become separate result stages, each mailed once.
-- A past FAIL mail is assigned by the application's current document decision.
UPDATE application_result_log result_log
    LEFT JOIN application ON application.id = result_log.application_id
SET result_log.notification_type = IF(application.document_pass = 1, 'FINAL_FAIL', 'DOCUMENT_FAIL')
WHERE result_log.notification_type = 'FAIL';

-- A saved FAIL wording keeps being sent for both failures.
INSERT INTO mail_template (type, subject, body)
SELECT 'DOCUMENT_FAIL', subject, body FROM mail_template WHERE type = 'FAIL';

INSERT INTO mail_template (type, subject, body)
SELECT 'FINAL_FAIL', subject, body FROM mail_template WHERE type = 'FAIL';

DELETE FROM mail_template WHERE type = 'FAIL';
