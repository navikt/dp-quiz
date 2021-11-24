INSERT INTO V1_PROSESSVERSJON(versjon_id, navn)
select distinct(versjon_id), 'AvslagpåminsteInntekt'
from soknad
where versjon_id < 99
order by versjon_id asc;

INSERT INTO V1_PROSESSVERSJON(versjon_id, navn)
select distinct(versjon_id), 'Dagpenger'
from soknad
where versjon_id > 99
order by versjon_id asc;

