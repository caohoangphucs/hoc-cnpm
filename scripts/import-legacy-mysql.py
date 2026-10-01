#!/usr/bin/env python3
"""One-time import into EMPTY PostgreSQL, with old app stopped; MySQL is kept."""
import json, re, subprocess, os
from decimal import Decimal
from pathlib import Path

def run(args, sql):
    r = subprocess.run(args, input=sql, text=True, capture_output=True)
    if r.returncode:
        p = Path('/home/phuc/deployments/hoc-cnpm/import-error.log')
        p.write_text(r.stderr); p.chmod(0o600)
        raise RuntimeError('Database error; protected details: ' + str(p))
    return r.stdout

def pg(sql):
    return run(['docker','exec','-i',os.environ.get('PG_CONTAINER','cnpm-postgres'),'psql','-U','cnpm_user','-d','cnpm_db','-v','ON_ERROR_STOP=1','-At'], sql)

def mysql(sql):
    return run(['docker','exec','-i','cnpm-mysql','sh','-c','MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot --default-character-set=utf8mb4 --batch --raw --skip-column-names cnpm_db'], sql)

def ident(s):
    assert re.fullmatch('[A-Za-z_][A-Za-z_0-9]*',s), 'Invalid identifier'
    return '"'+s.lower()+'"'

def literal(v, kind=''):
    if v is None: return 'NULL'
    if kind == 'boolean':
        assert v in (0,1,False,True), 'Invalid boolean'
        return 'TRUE' if v else 'FALSE'
    if isinstance(v,(dict,list)): v=json.dumps(v,ensure_ascii=False,default=str)
    if isinstance(v,(int,Decimal)): return str(v)
    return "'"+str(v).replace("'","''")+"'"

assert pg("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public';").strip() == '0', 'Refusing to overwrite PostgreSQL'
columns={}
for line in mysql("SELECT c.TABLE_NAME,c.COLUMN_NAME FROM information_schema.COLUMNS c JOIN information_schema.TABLES t ON t.TABLE_SCHEMA=c.TABLE_SCHEMA AND t.TABLE_NAME=c.TABLE_NAME WHERE c.TABLE_SCHEMA='cnpm_db' AND t.TABLE_TYPE='BASE TABLE' ORDER BY c.TABLE_NAME,c.ORDINAL_POSITION;").splitlines():
    t,c=line.split('\t'); ident(t);ident(c);columns.setdefault(t,[]).append(c)
queries=['START TRANSACTION WITH CONSISTENT SNAPSHOT;']
for t,cols in columns.items():
    pairs=','.join("'"+c+"',`"+c+'`' for c in cols)
    queries += ["SELECT 'TABLE:"+t+"';",'SELECT JSON_OBJECT('+pairs+') FROM `'+t+'`;']
queries += ['COMMIT;']
snapshot=mysql('\n'.join(queries))
p=Path('/home/phuc/deployments/hoc-cnpm/legacy-snapshot.jsonl');p.write_text(snapshot);p.chmod(0o600)
rows={}
for line in snapshot.splitlines():
    if line.startswith('TABLE:'): current=line[6:];rows[current.lower()]=[]
    else: rows[current.lower()].append(json.loads(line,parse_float=Decimal))
schema=Path('database/01_schema.sql').read_text()
commands=['BEGIN;','SET standard_conforming_strings=on;',schema,'CREATE SCHEMA legacy_archive;']
tables=re.findall(r'CREATE TABLE\s+(\w+)\s*\(',schema,re.I)
for t in tables:
    if t.lower() not in rows: continue
    cols=next(cols for name,cols in columns.items() if name.lower()==t.lower())
    definition=re.search(r'CREATE TABLE\s+'+re.escape(t)+r'\s*\((.*?)\n\);',schema,re.S|re.I)[1]
    kinds={m[1].lower():m[2].lower() for m in re.finditer(r'^\s*(\w+)\s+(\w+)',definition,re.M) if m[1].upper() not in ('CONSTRAINT','PRIMARY','UNIQUE','FOREIGN','CHECK')}
    assert all(c.lower() in kinds for c in cols), 'Unmapped column in '+t
    for row in rows[t.lower()]:
        values=','.join(literal(row[c],kinds[c.lower()]) for c in cols)
        commands.append('INSERT INTO '+ident(t)+' ('+','.join(map(ident,cols))+') VALUES ('+values+');')
    commands.append(f"DO $$ BEGIN IF (SELECT COUNT(*) FROM {ident(t)}) <> {len(rows[t.lower()])} THEN RAISE EXCEPTION 'Count mismatch'; END IF; END $$;")
for t in rows.keys()-{t.lower() for t in tables}:
    commands.append('CREATE TABLE legacy_archive.'+ident(t)+' (payload JSONB NOT NULL);')
    for row in rows[t]: commands.append('INSERT INTO legacy_archive.'+ident(t)+' VALUES ('+literal(json.dumps(row,ensure_ascii=False,default=str))+');')
    commands.append(f"DO $$ BEGIN IF (SELECT COUNT(*) FROM legacy_archive.{ident(t)}) <> {len(rows[t])} THEN RAISE EXCEPTION 'Archive count mismatch'; END IF; END $$;")
commands.append("""DO $$ DECLARE r record; maximum bigint; BEGIN
 FOR r IN SELECT table_name,column_name FROM information_schema.columns
 WHERE table_schema='public' AND column_default LIKE 'nextval(%' LOOP
 EXECUTE format('SELECT MAX(%I) FROM %I',r.column_name,r.table_name) INTO maximum;
 PERFORM setval(pg_get_serial_sequence(r.table_name,r.column_name),COALESCE(maximum,1),maximum IS NOT NULL);
 END LOOP; END $$;""")
commands+=['COMMIT;'];pg('\n'.join(commands))
print('Verified',len(rows),'source tables,',sum(map(len,rows.values())),'rows. MySQL preserved.')
