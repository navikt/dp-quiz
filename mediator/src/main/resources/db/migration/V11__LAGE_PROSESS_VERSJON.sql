INSERT INTO V1_PROSESSVERSJON(versjon_id, navn)
select distinct(versjon_id), 'AvslagpåminsteInntekt'
from faktum
where versjon_id < 99
order by versjon_id asc
on conflict do nothing;

INSERT INTO V1_PROSESSVERSJON(versjon_id, navn)
select distinct(versjon_id), 'Dagpenger'
from faktum
where versjon_id > 99
order by versjon_id asc
on conflict do nothing;

