-- ============================================================
-- 回滚 001：user_id -> user_account 并移除 token_version / login_attempt
-- 仅在需要退回旧版本代码时执行；同样会隐式提交，请先备份。
-- ============================================================

SET NAMES utf8mb4;

-- ---------- 1) post_like ----------
ALTER TABLE `post_like` ADD COLUMN `user_account` varchar(50) COLLATE utf8mb4_unicode_ci NULL AFTER `post_id`;
UPDATE `post_like` l JOIN `user` u ON u.id = l.user_id SET l.user_account = u.account;
ALTER TABLE `post_like` DROP FOREIGN KEY `post_like_ibfk_2`;
-- 与正向迁移同理：uk_post_user(post_id,user_id) 支撑 post_id 外键，先补临时索引
ALTER TABLE `post_like` ADD INDEX `idx_post_tmp` (`post_id`);
ALTER TABLE `post_like` DROP INDEX `uk_post_user`;
ALTER TABLE `post_like` DROP INDEX `user_id`;
ALTER TABLE `post_like` DROP COLUMN `user_id`;
ALTER TABLE `post_like` MODIFY COLUMN `user_account` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `post_like` ADD UNIQUE KEY `uk_post_user` (`post_id`,`user_account`);
ALTER TABLE `post_like` ADD INDEX `user_account` (`user_account`);
ALTER TABLE `post_like`
  ADD CONSTRAINT `post_like_ibfk_2` FOREIGN KEY (`user_account`) REFERENCES `user` (`account`) ON DELETE CASCADE;
ALTER TABLE `post_like` DROP INDEX `idx_post_tmp`;

-- ---------- 2) post_comment ----------
ALTER TABLE `post_comment` DROP FOREIGN KEY `post_comment_ibfk_2`;
ALTER TABLE `post_comment` DROP INDEX `user_id`;
ALTER TABLE `post_comment` ADD COLUMN `user_account` varchar(50) COLLATE utf8mb4_unicode_ci NULL AFTER `post_id`;
UPDATE `post_comment` c JOIN `user` u ON u.id = c.user_id SET c.user_account = u.account;
ALTER TABLE `post_comment` DROP COLUMN `user_id`;
ALTER TABLE `post_comment` MODIFY COLUMN `user_account` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `post_comment` ADD INDEX `user_account` (`user_account`);
ALTER TABLE `post_comment`
  ADD CONSTRAINT `post_comment_ibfk_2` FOREIGN KEY (`user_account`) REFERENCES `user` (`account`) ON DELETE CASCADE;

-- ---------- 3) post ----------
ALTER TABLE `post` DROP FOREIGN KEY `post_ibfk_1`;
ALTER TABLE `post` DROP INDEX `idx_user`;
ALTER TABLE `post` ADD COLUMN `user_account` varchar(50) COLLATE utf8mb4_unicode_ci NULL AFTER `id`;
UPDATE `post` p JOIN `user` u ON u.id = p.user_id SET p.user_account = u.account;
ALTER TABLE `post` DROP COLUMN `user_id`;
ALTER TABLE `post` MODIFY COLUMN `user_account` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `post` ADD INDEX `idx_user` (`user_account`);
ALTER TABLE `post`
  ADD CONSTRAINT `post_ibfk_1` FOREIGN KEY (`user_account`) REFERENCES `user` (`account`) ON DELETE CASCADE;

-- ---------- 4) plant_diary ----------
ALTER TABLE `plant_diary` DROP FOREIGN KEY `plant_diary_ibfk_1`;
ALTER TABLE `plant_diary` DROP INDEX `idx_user`;
ALTER TABLE `plant_diary` ADD COLUMN `user_account` varchar(50) COLLATE utf8mb4_unicode_ci NULL AFTER `id`;
UPDATE `plant_diary` d JOIN `user` u ON u.id = d.user_id SET d.user_account = u.account;
ALTER TABLE `plant_diary` DROP COLUMN `user_id`;
ALTER TABLE `plant_diary` MODIFY COLUMN `user_account` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `plant_diary` ADD INDEX `idx_user` (`user_account`);
ALTER TABLE `plant_diary`
  ADD CONSTRAINT `plant_diary_ibfk_1` FOREIGN KEY (`user_account`) REFERENCES `user` (`account`) ON DELETE CASCADE;

-- ---------- 5) 移除新增结构 ----------
DROP TABLE IF EXISTS `login_attempt`;
ALTER TABLE `user` DROP COLUMN `token_version`;
