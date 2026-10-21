-- ============================================================
-- 迁移 001：用户标识统一为 user_id
--            + user.token_version（服务端令牌失效）
--            + login_attempt（登录限流持久化，多实例共享）
--
-- 适用对象：已经存在的数据库（全新安装请直接导入 backend/src/main/resources/db/schema.sql）
-- 执行方式：mysql --default-character-set=utf8mb4 -u plant_cultivation -p plant_cultivation < 本文件
--          或在 mysql 会话中先 USE 目标库再 source 本文件（脚本不含 USE，未选库会报 1046）
--
-- 注意：
--   1. MySQL 的 DDL 会隐式提交，本脚本无法整体回滚；执行前请先备份（mysqldump）。
--   2. 若历史数据中存在“账号已不存在”的孤儿记录，下面的 UPDATE 会留下 NULL，
--      随后 MODIFY ... NOT NULL 会报错并中止——请先清理孤儿数据再重跑。
--   3. 回滚脚本见同目录 001-rollback.sql。
-- ============================================================

SET NAMES utf8mb4;

-- ---------- 0) 迁移前自检：以下查询应全部返回 0 ----------
SELECT 'plant_diary 孤儿' AS check_name, COUNT(*) AS orphan_rows
FROM plant_diary d LEFT JOIN user u ON u.account = d.user_account WHERE u.id IS NULL
UNION ALL
SELECT 'post 孤儿', COUNT(*) FROM post p LEFT JOIN user u ON u.account = p.user_account WHERE u.id IS NULL
UNION ALL
SELECT 'post_comment 孤儿', COUNT(*) FROM post_comment c LEFT JOIN user u ON u.account = c.user_account WHERE u.id IS NULL
UNION ALL
SELECT 'post_like 孤儿', COUNT(*) FROM post_like l LEFT JOIN user u ON u.account = l.user_account WHERE u.id IS NULL;

-- ---------- 1) 令牌版本 ----------
ALTER TABLE `user`
  ADD COLUMN `token_version` int NOT NULL DEFAULT '0'
  COMMENT '令牌版本：登出/改密自增使旧 JWT 失效' AFTER `bio`;

-- ---------- 2) plant_diary.user_account -> user_id ----------
ALTER TABLE `plant_diary` ADD COLUMN `user_id` bigint NULL AFTER `id`;
UPDATE `plant_diary` d JOIN `user` u ON u.account = d.user_account SET d.user_id = u.id;
ALTER TABLE `plant_diary` DROP FOREIGN KEY `plant_diary_ibfk_1`;
ALTER TABLE `plant_diary` DROP INDEX `idx_user`;
ALTER TABLE `plant_diary` DROP COLUMN `user_account`;
ALTER TABLE `plant_diary` MODIFY COLUMN `user_id` bigint NOT NULL;
ALTER TABLE `plant_diary` ADD INDEX `idx_user` (`user_id`);
ALTER TABLE `plant_diary`
  ADD CONSTRAINT `plant_diary_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

-- ---------- 3) post.user_account -> user_id ----------
ALTER TABLE `post` ADD COLUMN `user_id` bigint NULL AFTER `id`;
UPDATE `post` p JOIN `user` u ON u.account = p.user_account SET p.user_id = u.id;
ALTER TABLE `post` DROP FOREIGN KEY `post_ibfk_1`;
ALTER TABLE `post` DROP INDEX `idx_user`;
ALTER TABLE `post` DROP COLUMN `user_account`;
ALTER TABLE `post` MODIFY COLUMN `user_id` bigint NOT NULL;
ALTER TABLE `post` ADD INDEX `idx_user` (`user_id`);
ALTER TABLE `post`
  ADD CONSTRAINT `post_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

-- ---------- 4) post_comment.user_account -> user_id ----------
ALTER TABLE `post_comment` ADD COLUMN `user_id` bigint NULL AFTER `post_id`;
UPDATE `post_comment` c JOIN `user` u ON u.account = c.user_account SET c.user_id = u.id;
ALTER TABLE `post_comment` DROP FOREIGN KEY `post_comment_ibfk_2`;
ALTER TABLE `post_comment` DROP INDEX `user_account`;
ALTER TABLE `post_comment` DROP COLUMN `user_account`;
ALTER TABLE `post_comment` MODIFY COLUMN `user_id` bigint NOT NULL;
ALTER TABLE `post_comment` ADD INDEX `user_id` (`user_id`);
ALTER TABLE `post_comment`
  ADD CONSTRAINT `post_comment_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

-- ---------- 5) post_like.user_account -> user_id ----------
ALTER TABLE `post_like` ADD COLUMN `user_id` bigint NULL AFTER `post_id`;
UPDATE `post_like` l JOIN `user` u ON u.account = l.user_account SET l.user_id = u.id;
ALTER TABLE `post_like` DROP FOREIGN KEY `post_like_ibfk_2`;
-- uk_post_user(post_id,user_account) 同时是 post_id 外键的支撑索引，
-- 必须先补一个 post_id 临时索引，否则 DROP INDEX 会报 1553。
ALTER TABLE `post_like` ADD INDEX `idx_post_tmp` (`post_id`);
ALTER TABLE `post_like` DROP INDEX `uk_post_user`;
ALTER TABLE `post_like` DROP INDEX `user_account`;
ALTER TABLE `post_like` DROP COLUMN `user_account`;
ALTER TABLE `post_like` MODIFY COLUMN `user_id` bigint NOT NULL;
ALTER TABLE `post_like` ADD UNIQUE KEY `uk_post_user` (`post_id`,`user_id`);
ALTER TABLE `post_like` ADD INDEX `user_id` (`user_id`);
ALTER TABLE `post_like`
  ADD CONSTRAINT `post_like_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
-- 新唯一键已覆盖 post_id，临时索引可以移除
ALTER TABLE `post_like` DROP INDEX `idx_post_tmp`;

-- ---------- 6) 登录限流表 ----------
CREATE TABLE IF NOT EXISTS `login_attempt` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `rate_key` varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '限流维度：account:xxx / ip:x.x.x.x',
  `attempted_at` datetime(3) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_key_time` (`rate_key`,`attempted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 迁移完成后：升级前签发的旧 JWT 缺少 uid/ver，会被过滤器视为未登录，用户重新登录一次即可。
