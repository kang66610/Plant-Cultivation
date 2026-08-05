# -*- coding: utf-8 -*-
"""从 plant_cultivation.sql（生产 mysqldump）重建 schema.sql 与 data.sql。

- schema.sql: CREATE DATABASE + USE + 9 张表的 CREATE TABLE IF NOT EXISTS
- data.sql: USE + 种子数据（category / plant / plant_category / care_guide）INSERT
"""
import re
import sys

DUMP = "plant_cultivation.sql"
SCHEMA_OUT = "backend/src/main/resources/db/schema.sql"
DATA_OUT = "backend/src/main/resources/db/data.sql"

# data.sql 只保留基础种子数据表（用户产生数据不导入）
SEED_TABLES = ["category", "plant", "plant_category", "care_guide"]

with open(DUMP, encoding="utf-8") as f:
    content = f.read()

# ---------- 提取 CREATE TABLE 块 ----------
table_blocks = {}  # name -> 原始 SQL 块（含分号）
# 匹配 CREATE TABLE `xxx` ( ... ) ENGINE=...;
pattern = re.compile(
    r"(CREATE TABLE `(?P<name>\w+)` \((?P<body>.*?)\) ENGINE=\w+[^;]*;)",
    re.S,
)
for m in pattern.finditer(content):
    table_blocks[m.group("name")] = m.group(0)

# ---------- 提取 INSERT 数据块 ----------
# mysqldump 的 INSERT 位于 LOCK TABLES ... UNLOCK TABLES 之间
insert_blocks = {}
for name in SEED_TABLES:
    m = re.search(
        rf"LOCK TABLES `{name}` WRITE;(.*?)UNLOCK TABLES;", content, re.S
    )
    if m:
        block = m.group(1)
        # 去掉 mysqldump 辅助语句，只保留 INSERT 语句
        block = re.sub(r"/\*!40000 ALTER TABLE `\w+` DISABLE KEYS \*/;?", "", block)
        block = re.sub(r"/\*!40000 ALTER TABLE `\w+` ENABLE KEYS \*/;?", "", block)
        block = block.strip()
        insert_blocks[name] = block

# Keep tables in dependency order so a fresh MySQL import succeeds with
# foreign-key checks enabled. User-owned tables reference `user`.
expected = ["category", "plant", "user", "plant_category", "care_guide", "plant_diary",
            "post", "post_comment", "post_like", "user_plant_collection"]
missing = [t for t in expected if t not in table_blocks]
if missing:
    sys.exit(f"缺少表定义: {missing}")
missing_data = [t for t in SEED_TABLES if t not in insert_blocks]
if missing_data:
    sys.exit(f"缺少种子数据: {missing_data}")

# ---------- 生成 schema.sql ----------
lines = []
lines.append("-- Plant Cultivation Database Schema")
lines.append("-- 由 scripts/rebuild-sql.py 从 plant_cultivation.sql（生产 dump）重建，勿手工编辑")
lines.append("")
lines.append("CREATE DATABASE IF NOT EXISTS plant_cultivation")
lines.append("    CHARACTER SET utf8mb4")
lines.append("    COLLATE utf8mb4_unicode_ci;")
lines.append("")
lines.append("USE plant_cultivation;")
lines.append("")

for name in expected:
    block = table_blocks[name]
    # DROP TABLE IF EXISTS `x`;\nCREATE TABLE `x` ( ... ) ENGINE=...
    m = re.match(r"CREATE TABLE `\w+` \(", block)
    assert m, f"无法解析表 {name}"
    body = block[len(m.group(0)) - 1:].rstrip()  # 从 ( 开始
    # 去掉 ENGINE=... 尾部的 AUTO_INCREMENT（由数据插入决定）
    body = re.sub(r" AUTO_INCREMENT=\d+", "", body)
    lines.append(f"CREATE TABLE IF NOT EXISTS `{name}` {body}")
    lines.append("")

with open(SCHEMA_OUT, "w", encoding="utf-8", newline="\n") as f:
    f.write("\n".join(lines))

# ---------- 生成 data.sql ----------
data_lines = []
data_lines.append("-- Plant Cultivation Seed Data")
data_lines.append("-- 由 scripts/rebuild-sql.py 从 plant_cultivation.sql（生产 dump）重建，勿手工编辑")
data_lines.append("")
data_lines.append("USE plant_cultivation;")
data_lines.append("")
for name in SEED_TABLES:
    data_lines.append(f"-- {name}")
    data_lines.append(insert_blocks[name])
    data_lines.append("")

with open(DATA_OUT, "w", encoding="utf-8", newline="\n") as f:
    f.write("\n".join(data_lines))

print("schema.sql 表:", list(table_blocks.keys()))
print("data.sql 种子数据表:", list(insert_blocks.keys()))
print("plant INSERT 行数:", insert_blocks["plant"].count("),(") + 1)
print("OK")
