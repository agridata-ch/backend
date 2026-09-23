-- Request purposes are now parsed strictly as XHTML for PDF rendering; legacy plain-text values would
-- no longer parse and would fail fast. Wrap every existing non-HTML purpose in a <p> element (escaping
-- the XML-significant characters &, < and >) so all stored purposes are well-formed rich text. Values
-- that already start with markup ('<') and empty values are left untouched.
CREATE FUNCTION migrate_wrap_purpose_as_html(value text) RETURNS text AS
$$
BEGIN
    IF value IS NULL OR btrim(value) = '' OR left(btrim(value), 1) = '<' THEN
        RETURN value;
    END IF;
    RETURN '<p>' ||
           replace(replace(replace(value, '&', '&amp;'), '<', '&lt;'), '>', '&gt;') ||
           '</p>';
END;
$$ LANGUAGE plpgsql;

UPDATE data_request
SET purpose = jsonb_strip_nulls(jsonb_build_object(
        'de', migrate_wrap_purpose_as_html(purpose ->> 'de'),
        'fr', migrate_wrap_purpose_as_html(purpose ->> 'fr'),
        'it', migrate_wrap_purpose_as_html(purpose ->> 'it')))
WHERE purpose IS NOT NULL;

UPDATE contract_revision
SET purpose = jsonb_strip_nulls(jsonb_build_object(
        'de', migrate_wrap_purpose_as_html(purpose ->> 'de'),
        'fr', migrate_wrap_purpose_as_html(purpose ->> 'fr'),
        'it', migrate_wrap_purpose_as_html(purpose ->> 'it')))
WHERE purpose IS NOT NULL;

DROP FUNCTION migrate_wrap_purpose_as_html(text);
