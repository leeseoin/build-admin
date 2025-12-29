CREATE TABLE TB_USER (
    user_seq    INT AUTO_INCREMENT PRIMARY KEY COMMENT 'user unique number',
    login_id    VARCHAR(50) NOT NULL UNIQUE    COMMENT 'user login id',
    password    VARCHAR(255) NOT NULL          COMMENT 'user password',
    user_name   VARCHAR(100) NOT NULL          COMMENT 'user name',
    language    VARCHAR(10) DEFAULT 'ko'       COMMENT 'user langauge(ko, en, etc)',
    subscribe_in_service VARCHAR(1) DEFAULT 'N'   COMMENT 'user subscription(Y/N)',
    access_token VARCHAR(500)                  COMMENT 'user activate access token',
    register_date DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'register date',
    modify_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'modify date'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;