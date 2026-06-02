-- Plant Cultivation Database Schema

CREATE DATABASE IF NOT EXISTS plant_cultivation
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE plant_cultivation;

-- Categories table
CREATE TABLE IF NOT EXISTS category (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(50) NOT NULL,
    slug            VARCHAR(60) NOT NULL UNIQUE,
    description     TEXT,
    icon_svg        TEXT,
    display_order   INT DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Plants table
CREATE TABLE IF NOT EXISTS plant (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    common_name         VARCHAR(100) NOT NULL,
    scientific_name     VARCHAR(150) NOT NULL,
    slug                VARCHAR(120) NOT NULL UNIQUE,
    family              VARCHAR(80),
    origin              VARCHAR(200),
    description         TEXT,
    short_description   VARCHAR(300),

    -- Care parameters
    light_level         ENUM('low','medium','high','bright_direct') NOT NULL,
    light_hours_min     TINYINT,
    light_hours_max     TINYINT,
    light_description   VARCHAR(300),

    water_frequency     ENUM('rarely','weekly','biweekly','frequent') NOT NULL,
    water_interval_days_min SMALLINT,
    water_interval_days_max SMALLINT,
    water_description   VARCHAR(300),

    humidity_level      ENUM('low','medium','high') DEFAULT 'medium',
    humidity_description VARCHAR(300),

    temp_min_celsius    TINYINT,
    temp_max_celsius    TINYINT,
    temp_description    VARCHAR(300),

    fertilizer_type     VARCHAR(100),
    fertilizer_frequency ENUM('none','monthly','biweekly','weekly') DEFAULT 'monthly',
    fertilizer_description VARCHAR(300),

    soil_type           VARCHAR(200),
    soil_ph_min         DECIMAL(3,1),
    soil_ph_max         DECIMAL(3,1),

    difficulty          ENUM('easy','medium','hard') NOT NULL,
    growth_rate         ENUM('slow','moderate','fast') DEFAULT 'moderate',
    max_height_cm       SMALLINT,
    is_indoor           BOOLEAN DEFAULT TRUE,
    is_outdoor          BOOLEAN DEFAULT FALSE,
    is_pet_safe         BOOLEAN DEFAULT FALSE,
    is_air_purifying    BOOLEAN DEFAULT FALSE,

    -- Media
    image_url           VARCHAR(500),
    image_alt           VARCHAR(200),
    gallery_urls        JSON,
    model_3d_url        VARCHAR(500),
    lottie_url          VARCHAR(500),

    -- SEO
    meta_title          VARCHAR(100),
    meta_description    VARCHAR(200),

    -- Status
    is_featured         BOOLEAN DEFAULT FALSE,
    view_count          INT DEFAULT 0,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_difficulty (difficulty),
    INDEX idx_light (light_level),
    INDEX idx_featured (is_featured),
    FULLTEXT INDEX ft_search (common_name, scientific_name, description)
);

-- Plant-Category join table
CREATE TABLE IF NOT EXISTS plant_category (
    plant_id    BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (plant_id, category_id),
    FOREIGN KEY (plant_id) REFERENCES plant(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE
);

-- Care guides table
CREATE TABLE IF NOT EXISTS care_guide (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    plant_id        BIGINT NOT NULL,
    season          ENUM('spring','summer','fall','winter','all') DEFAULT 'all',
    care_type       ENUM('watering','light','fertilizer','pruning','repotting',
                         'propagation','pest_control','general') NOT NULL,
    title           VARCHAR(150) NOT NULL,
    content         TEXT NOT NULL,
    tips            JSON,
    common_mistakes JSON,
    display_order   INT DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (plant_id) REFERENCES plant(id) ON DELETE CASCADE,
    INDEX idx_plant_season (plant_id, season),
    INDEX idx_care_type (care_type)
);

-- Users table
CREATE TABLE IF NOT EXISTS user (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    username        VARCHAR(50) NOT NULL,
    account         VARCHAR(50) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    avatar_url      VARCHAR(500),
    bio             VARCHAR(300),
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Community posts table
CREATE TABLE IF NOT EXISTS post (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_account    VARCHAR(50) NOT NULL,
    content         TEXT NOT NULL,
    images          JSON,
    plant_slug      VARCHAR(120),
    category_slug   VARCHAR(60),
    like_count      INT DEFAULT 0,
    comment_count   INT DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_account) REFERENCES user(account) ON DELETE CASCADE,
    INDEX idx_user (user_account),
    INDEX idx_plant (plant_slug),
    INDEX idx_created (created_at DESC)
);

-- Post likes table
CREATE TABLE IF NOT EXISTS post_like (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id         BIGINT NOT NULL,
    user_account    VARCHAR(50) NOT NULL,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    FOREIGN KEY (user_account) REFERENCES user(account) ON DELETE CASCADE,
    UNIQUE KEY uk_post_user (post_id, user_account)
);

-- Post comments table
CREATE TABLE IF NOT EXISTS post_comment (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id         BIGINT NOT NULL,
    user_account    VARCHAR(50) NOT NULL,
    content         TEXT NOT NULL,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    FOREIGN KEY (user_account) REFERENCES user(account) ON DELETE CASCADE,
    INDEX idx_post (post_id)
);

-- Plant diary (growth journal)
CREATE TABLE IF NOT EXISTS plant_diary (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_account    VARCHAR(50) NOT NULL,
    plant_slug      VARCHAR(120),
    plant_name      VARCHAR(100),
    title           VARCHAR(150) NOT NULL,
    content         TEXT,
    images          JSON,
    weather         VARCHAR(50),
    mood            VARCHAR(50),
    height_cm       SMALLINT,
    leaf_count      SMALLINT,
    growth_stage    VARCHAR(30),
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_account) REFERENCES user(account) ON DELETE CASCADE,
    INDEX idx_user (user_account),
    INDEX idx_plant (plant_slug),
    INDEX idx_created (created_at DESC)
);

-- User plant collection
CREATE TABLE IF NOT EXISTS user_plant_collection (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT NOT NULL,
    plant_id        BIGINT NOT NULL,
    nickname        VARCHAR(50),
    location        VARCHAR(100),
    water_interval_days SMALLINT,
    last_watered_at DATETIME,
    next_water_at   DATETIME,
    notes           TEXT,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (plant_id) REFERENCES plant(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_plant (user_id, plant_id)
);
