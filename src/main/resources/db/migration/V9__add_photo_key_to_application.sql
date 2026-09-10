ALTER TABLE application
    ADD COLUMN photo_key VARCHAR(1024) NULL,
    MODIFY COLUMN photo VARCHAR(3000) NULL;

UPDATE application
SET photo_key = SUBSTRING(photo, LOCATE('store-image/', photo))
WHERE photo_key IS NULL
  AND photo LIKE '%/store-image/%';
