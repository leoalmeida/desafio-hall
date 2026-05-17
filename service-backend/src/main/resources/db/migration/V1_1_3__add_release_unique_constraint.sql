DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_release_application_version_env'
    ) THEN
        ALTER TABLE RELEASE
            ADD CONSTRAINT uk_release_application_version_env
            UNIQUE (application_id, version, env);
    END IF;
END $$;
