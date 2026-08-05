USE `plant_cultivation`;

INSERT INTO `care_guide` (`plant_id`, `season`, `care_type`, `title`, `content`, `tips`, `common_mistakes`, `display_order`)
SELECT
  p.`id`,
  'all',
  'watering',
  CONCAT(p.`common_name`, '浇水指南'),
  COALESCE(NULLIF(p.`water_description`, ''), NULLIF(p.`water_principle`, ''), '根据盆土干湿度调整浇水，保持根系透气，避免长期积水。'),
  JSON_ARRAY(
    COALESCE(NULLIF(p.`water_principle`, ''), '浇水前先确认表土状态'),
    COALESCE(NULLIF(p.`water_quality`, ''), '使用室温清水'),
    '浇透后倒掉托盘积水'
  ),
  JSON_ARRAY(
    COALESCE(NULLIF(p.`water_taboo`, ''), '固定频率浇水而不检查土壤'),
    '花盆长期泡水'
  ),
  10
FROM `plant` p
WHERE NOT EXISTS (
  SELECT 1 FROM `care_guide` cg WHERE cg.`plant_id` = p.`id` AND cg.`care_type` = 'watering'
);

INSERT INTO `care_guide` (`plant_id`, `season`, `care_type`, `title`, `content`, `tips`, `common_mistakes`, `display_order`)
SELECT
  p.`id`,
  'all',
  'light',
  CONCAT(p.`common_name`, '光照指南'),
  COALESCE(NULLIF(p.`light_description`, ''), '放在适合其光照等级的位置，避免突然暴晒或长期阴暗。'),
  JSON_ARRAY(
    CONCAT('光照等级：', COALESCE(NULLIF(p.`light_level`, ''), 'medium')),
    CONCAT('建议光照时长：', COALESCE(p.`light_hours_min`, 0), '-', COALESCE(p.`light_hours_max`, 0), ' 小时'),
    '定期转动花盆，让株型更均匀'
  ),
  JSON_ARRAY('强光植物突然移入阴暗角落', '耐阴植物长期接受正午直晒'),
  20
FROM `plant` p
WHERE NOT EXISTS (
  SELECT 1 FROM `care_guide` cg WHERE cg.`plant_id` = p.`id` AND cg.`care_type` = 'light'
);

INSERT INTO `care_guide` (`plant_id`, `season`, `care_type`, `title`, `content`, `tips`, `common_mistakes`, `display_order`)
SELECT
  p.`id`,
  'all',
  'fertilizer',
  CONCAT(p.`common_name`, '施肥指南'),
  COALESCE(NULLIF(p.`fertilizer_description`, ''), NULLIF(p.`fertilizer_grow`, ''), '生长期薄肥勤施，休眠或状态差时暂停施肥。'),
  JSON_ARRAY(
    COALESCE(NULLIF(p.`fertilizer_best_season`, ''), '春夏生长期'),
    COALESCE(NULLIF(p.`fertilizer_grow`, ''), '薄肥勤施'),
    COALESCE(NULLIF(p.`fertilizer_bloom`, ''), '观花植物花期前可补充磷钾肥')
  ),
  JSON_ARRAY(COALESCE(NULLIF(p.`fertilizer_taboo`, ''), '浓肥、生肥或刚换盆后立即施肥')),
  30
FROM `plant` p
WHERE NOT EXISTS (
  SELECT 1 FROM `care_guide` cg WHERE cg.`plant_id` = p.`id` AND cg.`care_type` = 'fertilizer'
);

INSERT INTO `care_guide` (`plant_id`, `season`, `care_type`, `title`, `content`, `tips`, `common_mistakes`, `display_order`)
SELECT
  p.`id`,
  'all',
  'pruning',
  CONCAT(p.`common_name`, '修剪指南'),
  COALESCE(NULLIF(p.`prune_method`, ''), NULLIF(p.`prune_parts`, ''), '及时剪除黄叶、枯枝和过密枝叶，保持通风和株型。'),
  JSON_ARRAY(
    COALESCE(NULLIF(p.`prune_best_time`, ''), '春季或生长旺盛期'),
    COALESCE(NULLIF(p.`prune_parts`, ''), '枯黄叶、病弱枝、过密枝')
  ),
  JSON_ARRAY(COALESCE(NULLIF(p.`prune_taboo`, ''), '一次性重剪过多或使用未消毒工具')),
  40
FROM `plant` p
WHERE NOT EXISTS (
  SELECT 1 FROM `care_guide` cg WHERE cg.`plant_id` = p.`id` AND cg.`care_type` = 'pruning'
);

INSERT INTO `care_guide` (`plant_id`, `season`, `care_type`, `title`, `content`, `tips`, `common_mistakes`, `display_order`)
SELECT
  p.`id`,
  'all',
  'general',
  CONCAT(p.`common_name`, '通用养护'),
  COALESCE(NULLIF(p.`growth_habit`, ''), NULLIF(p.`temp_description`, ''), '观察叶片、盆土和新芽状态，按季节调整水肥光照。'),
  JSON_ARRAY(
    COALESCE(NULLIF(p.`suitable_position`, ''), '放在通风明亮处'),
    COALESCE(NULLIF(p.`soil_recipe`, ''), '使用疏松透气介质'),
    COALESCE(NULLIF(p.`pot_size_suggestion`, ''), '花盆大小与根系匹配')
  ),
  JSON_ARRAY('频繁挪动位置', '忽略病虫害早期迹象', '环境骤变后立刻重肥重水'),
  50
FROM `plant` p
WHERE NOT EXISTS (
  SELECT 1 FROM `care_guide` cg WHERE cg.`plant_id` = p.`id` AND cg.`care_type` = 'general'
);
