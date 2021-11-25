-- Avslag på minste inntekt
UPDATE faktum set versjon_id = versjon.id
    FROM V1_PROSESSVERSJON as versjon WHERE faktum.versjon_id = versjon.versjon_id AND faktum.versjon_id < 99 AND versjon.navn = 'AvslagPåMinsteinntekt';

UPDATE soknad set versjon_id = versjon.id
    FROM V1_PROSESSVERSJON as versjon WHERE soknad.versjon_id = versjon.versjon_id AND soknad.versjon_id < 99 AND versjon.navn = 'AvslagPåMinsteinntekt';

-- Dagpenger
UPDATE faktum set versjon_id = versjon.id
    FROM V1_PROSESSVERSJON as versjon WHERE faktum.versjon_id = versjon.versjon_id AND faktum.versjon_id > 99 AND versjon.navn = 'Dagpenger';

UPDATE soknad set versjon_id = versjon.id
    FROM V1_PROSESSVERSJON as versjon WHERE soknad.versjon_id = versjon.versjon_id AND soknad.versjon_id > 99 AND versjon.navn = 'Dagpenger';
