
replacements = [
    ('BLOB(16 M)', 'MEDIUMBLOB'),
    ('BLOB(32 M)', 'LONGBLOB'),
    ('CLOB(64 K)', 'TEXT'),
    ('SMALLINT default 0', 'TINYINT default 0'),
    ('VARCHAR(32672)', 'VARCHAR(16383)'),

    (' comment  ', ' `comment`'),
    (' time  ', ' `time`'),
    (' value  ', ' `value`'),
]


def main():
    schema_dir = '../resources/org/openlca/core/database/internal'

    in_file = schema_dir + '/current_schema_derby.sql'
    lines = [
        '-- generated with make_mysql_schema.py; do not edit\n'
        '\n',
        '-- CREATE DATABASE openlca;\n',
        '-- USE openlca',
    ]
    with open(in_file, 'r', encoding='utf-8', newline='\n') as inp:
        for r in inp:
            if r.strip().startswith('--'):
                continue
            line = r
            for (a, b) in replacements:
                line = line.replace(a, b)
            lines.append(line)

    out_file = schema_dir + '/current_schema_mysql.sql'
    with open(out_file, 'w', encoding='utf-8', newline='\n') as out:
        out.writelines(lines)


if __name__ == '__main__':
    main()
